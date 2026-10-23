package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.*;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.domain.*;
import br.com.safefield.realtime.EventoTempoReal;
import br.com.safefield.repository.GestorRepository;
import br.com.safefield.repository.OcorrenciaRiscoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ciclo de vida da ocorrência de risco, seguindo o diagrama de atividades da Sprint 1:
 * abertura com alerta ao operador e ao supervisor, regularização pelo operador, escalonamento
 * opcional e validação do supervisor (RN04: só o supervisor encerra).
 * Toda transição gera ação no histórico da ocorrência e evento na trilha de auditoria.
 */
@Service
@Transactional
public class OcorrenciaService {

    private final OcorrenciaRiscoRepository repo;
    private final GestorRepository gestores;
    private final AuditoriaService auditoria;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public OcorrenciaService(OcorrenciaRiscoRepository repo, GestorRepository gestores, AuditoriaService auditoria,
                             ApplicationEventPublisher eventos, Clock clock) {
        this.repo = repo;
        this.gestores = gestores;
        this.auditoria = auditoria;
        this.eventos = eventos;
        this.clock = clock;
    }

    /* ------------------------------------------------------------------ */
    /* Abertura (chamada pelo processamento de detecções)                  */
    /* ------------------------------------------------------------------ */

    public OcorrenciaRisco abrir(DeteccaoEPI deteccao, MotorRegrasService.Avaliacao avaliacao, boolean reincidente) {
        LocalDateTime quando = deteccao.getDataHora();
        AreaIndustrial area = deteccao.getArea();
        Operador operador = deteccao.getOperador();
        String nomesAusentes = avaliacao.episAusentes().stream()
                .map(e -> e.getTipo().getDescricao()).collect(Collectors.joining(", "));

        String descricao = "EPI obrigatório não detectado: " + nomesAusentes + ". Área " + area.getNome()
                + (deteccao.getDispositivo() == null ? "" : ", dispositivo " + deteccao.getDispositivo()) + ".";
        OcorrenciaRisco o = new OcorrenciaRisco("PENDENTE-" + UUID.randomUUID(), deteccao, avaliacao.episAusentes(),
                avaliacao.severidade(), reincidente, descricao);
        o = repo.save(o);
        o.setCodigoSequencial();

        String regras = avaliacao.regrasVioladas().stream().map(RegraSeguranca::getDescricao).distinct()
                .collect(Collectors.joining("; "));
        o.registrarAcao(TipoAcao.ABERTURA, null, "Regra violada: " + regras
                + (reincidente ? " Reincidência: o mesmo EPI já faltou nos últimos 7 dias." : ""), quando);

        // RN03: o alerta vai ao operador e ao supervisor da área ao mesmo tempo
        o.emitirAlerta(operador, CanalAlerta.PWA_PUSH,
                "Atenção: coloque " + nomesAusentes + " antes de continuar em " + area.getNome() + ".", quando);
        SupervisorSeguranca supervisor = area.getSupervisor();
        if (supervisor != null) {
            o.emitirAlerta(supervisor, CanalAlerta.PAINEL,
                    operador.getNome() + " sem " + nomesAusentes + " em " + area.getNome()
                            + " (severidade " + avaliacao.severidade().name().toLowerCase() + ").", quando);
        }
        o.registrarAcao(TipoAcao.ALERTA_EMITIDO, null, "Alerta enviado a " + operador.getNome()
                + (supervisor == null ? "" : " e a " + supervisor.getNome()) + ".", quando);

        auditoria.registrar(quando, TipoEventoAuditoria.OCORRENCIA_CRIADA, null, area, o, o.getSeveridade(),
                "Ocorrência aberta: " + operador.getNome() + " sem " + nomesAusentes + ".");
        auditoria.registrar(quando, TipoEventoAuditoria.ALERTA_EMITIDO, null, area, o, o.getSeveridade(),
                "Alerta em tempo real enviado ao operador" + (supervisor == null ? "." : " e ao supervisor."));

        publicar(EventoTempoReal.OCORRENCIA_ABERTA, o, "Nova ocorrência " + o.getCodigo());
        return o;
    }

    /* ------------------------------------------------------------------ */
    /* Transições                                                          */
    /* ------------------------------------------------------------------ */

    public OcorrenciaDetalheDTO regularizar(Long id, Usuario autor, String observacao) {
        return regularizar(id, autor, observacao, agora());
    }

    public OcorrenciaDetalheDTO regularizar(Long id, Usuario autor, String observacao, LocalDateTime quando) {
        OcorrenciaRisco o = carregar(id);
        if (autor instanceof GestorIndustrial) {
            throw new AcessoNegadoException("A regularização é registrada pelo operador ou pelo supervisor.");
        }
        exigirAcessoDoOperador(o, autor);
        if (o.getStatus() != StatusOcorrencia.ABERTA && o.getStatus() != StatusOcorrencia.ESCALADA) {
            throw new NegocioException("Só é possível regularizar uma ocorrência aberta ou escalada.");
        }
        o.setStatus(StatusOcorrencia.AGUARDANDO_VALIDACAO);
        o.setDataRegularizacao(quando);
        o.registrarAcao(TipoAcao.REGULARIZACAO, autor, textoOuPadrao(observacao, "EPI colocado e uso regularizado."),
                quando);
        o.getAlertas().stream().filter(a -> a.getDestinatario().getId().equals(o.getOperador().getId()))
                .forEach(Alerta::marcarComoLido);
        auditoria.registrar(quando, TipoEventoAuditoria.REGULARIZACAO_REGISTRADA, autor, o.getArea(), o,
                o.getSeveridade(), "Regularização registrada; aguardando validação do supervisor.");
        publicar(EventoTempoReal.OCORRENCIA_ATUALIZADA, o, o.getCodigo() + " aguardando validação");
        return detalhe(o);
    }

    public OcorrenciaDetalheDTO validar(Long id, Usuario autor, boolean aprovada, String observacao) {
        return validar(id, autor, aprovada, observacao, agora());
    }

    public OcorrenciaDetalheDTO validar(Long id, Usuario autor, boolean aprovada, String observacao,
                                        LocalDateTime quando) {
        OcorrenciaRisco o = carregar(id);
        if (!(autor instanceof SupervisorSeguranca)) {
            throw new AcessoNegadoException("Somente o supervisor de segurança valida ocorrências (RN04).");
        }
        if (o.getStatus() != StatusOcorrencia.AGUARDANDO_VALIDACAO) {
            throw new NegocioException("A ocorrência precisa estar aguardando validação.");
        }
        if (aprovada) {
            o.setStatus(StatusOcorrencia.ENCERRADA);
            o.setDataEncerramento(quando);
            o.registrarAcao(TipoAcao.VALIDACAO_APROVADA, autor,
                    textoOuPadrao(observacao, "Regularização conferida em campo."), quando);
            o.registrarAcao(TipoAcao.ENCERRAMENTO, autor, "Ocorrência encerrada.", quando);
            o.getAlertas().forEach(a -> a.setStatus(StatusAlerta.RESOLVIDO));
            auditoria.registrar(quando, TipoEventoAuditoria.VALIDACAO_APROVADA, autor, o.getArea(), o,
                    o.getSeveridade(), "Regularização validada pelo supervisor.");
            auditoria.registrar(quando, TipoEventoAuditoria.OCORRENCIA_ENCERRADA, autor, o.getArea(), o,
                    o.getSeveridade(), "Ocorrência encerrada.");
            publicar(EventoTempoReal.OCORRENCIA_ATUALIZADA, o, o.getCodigo() + " encerrada");
        } else {
            if (observacao == null || observacao.isBlank()) {
                throw new NegocioException("Informe o motivo para não validar a regularização.");
            }
            o.setStatus(StatusOcorrencia.ABERTA);
            o.setDataRegularizacao(null);
            o.registrarAcao(TipoAcao.VALIDACAO_REJEITADA, autor, observacao, quando);
            o.emitirAlerta(o.getOperador(), CanalAlerta.PWA_PUSH,
                    "Regularização não validada: " + observacao, quando);
            auditoria.registrar(quando, TipoEventoAuditoria.VALIDACAO_REJEITADA, autor, o.getArea(), o,
                    o.getSeveridade(), "Regularização não validada: " + observacao);
            publicar(EventoTempoReal.OCORRENCIA_ATUALIZADA, o, o.getCodigo() + " reaberta");
        }
        return detalhe(o);
    }

    public OcorrenciaDetalheDTO escalar(Long id, Usuario autor, String observacao) {
        return escalar(id, autor, observacao, agora());
    }

    public OcorrenciaDetalheDTO escalar(Long id, Usuario autor, String observacao, LocalDateTime quando) {
        OcorrenciaRisco o = carregar(id);
        if (!(autor instanceof SupervisorSeguranca)) {
            throw new AcessoNegadoException("Somente o supervisor de segurança escala ocorrências.");
        }
        if (o.getStatus() != StatusOcorrencia.ABERTA) {
            throw new NegocioException("Só é possível escalar uma ocorrência aberta.");
        }
        o.setStatus(StatusOcorrencia.ESCALADA);
        o.setSeveridade(NivelRisco.maior(o.getSeveridade(), NivelRisco.ALTO));
        String motivo = textoOuPadrao(observacao, "Risco crítico ou reincidente: procedimento de segurança acionado.");
        o.registrarAcao(TipoAcao.ESCALONAMENTO, autor, motivo, quando);
        for (GestorIndustrial g : gestores.findAll()) {
            o.emitirAlerta(g, CanalAlerta.PAINEL, "Ocorrência " + o.getCodigo() + " escalada em "
                    + o.getArea().getNome() + ": " + motivo, quando);
        }
        auditoria.registrar(quando, TipoEventoAuditoria.OCORRENCIA_ESCALADA, autor, o.getArea(), o,
                o.getSeveridade(), "Ocorrência escalada: " + motivo);
        publicar(EventoTempoReal.OCORRENCIA_ATUALIZADA, o, o.getCodigo() + " escalada");
        return detalhe(o);
    }

    public OcorrenciaDetalheDTO comentar(Long id, Usuario autor, String observacao) {
        OcorrenciaRisco o = carregar(id);
        exigirAcessoDoOperador(o, autor);
        LocalDateTime quando = agora();
        o.registrarAcao(TipoAcao.COMENTARIO, autor, observacao, quando);
        auditoria.registrar(quando, TipoEventoAuditoria.COMENTARIO, autor, o.getArea(), o, o.getSeveridade(),
                "Comentário: " + observacao);
        return detalhe(o);
    }

    /* ------------------------------------------------------------------ */
    /* Consultas                                                           */
    /* ------------------------------------------------------------------ */

    public record Filtro(String categoria, Long areaId, Long operadorId, NivelRisco severidade, String busca,
                         Integer dias) {}

    @Transactional(readOnly = true)
    public List<OcorrenciaResumoDTO> listar(Filtro f, Usuario usuario) {
        LocalDateTime desde = f.dias() == null ? null : agora().minusDays(f.dias());
        String busca = f.busca() == null ? "" : f.busca().toLowerCase(Locale.ROOT).trim();
        Long operadorId = usuario instanceof Operador ? usuario.getId() : f.operadorId();
        return repo.findAllByOrderByDataAberturaDesc().stream()
                .filter(o -> operadorId == null || o.getOperador().getId().equals(operadorId))
                .filter(o -> f.areaId() == null || o.getArea().getId().equals(f.areaId()))
                .filter(o -> f.severidade() == null || o.getSeveridade() == f.severidade())
                .filter(o -> desde == null || o.getDataAbertura().isAfter(desde))
                .filter(o -> categoria(o, f.categoria()))
                .filter(o -> busca.isEmpty() || o.getOperador().getNome().toLowerCase(Locale.ROOT).contains(busca)
                        || o.getOperador().getMatricula().toLowerCase(Locale.ROOT).contains(busca)
                        || o.getCodigo().toLowerCase(Locale.ROOT).contains(busca)
                        || o.getArea().getNome().toLowerCase(Locale.ROOT).contains(busca))
                .limit(300)
                .map(Mapper::ocorrencia)
                .toList();
    }

    @Transactional(readOnly = true)
    public OcorrenciaDetalheDTO detalhe(Long id, Usuario usuario) {
        OcorrenciaRisco o = carregar(id);
        exigirAcessoDoOperador(o, usuario);
        return detalhe(o);
    }

    private OcorrenciaDetalheDTO detalhe(OcorrenciaRisco o) {
        repo.flush(); // garante ids nas ações e alertas recém-criados antes de montar o DTO
        Turno turno = Turno.doHorario(o.getDataAbertura().toLocalTime());
        List<RegraDTO> orientacoes = o.getArea().obterRegrasAtivas(turno).stream().map(Mapper::regra).toList();
        List<AcaoDTO> acoes = o.getAcoes().stream()
                .sorted(Comparator.comparing(AcaoOcorrencia::getDataHora).thenComparing(a -> a.getTipo().ordinal()))
                .map(Mapper::acao).toList();
        return new OcorrenciaDetalheDTO(Mapper.ocorrencia(o), o.getDescricao(),
                o.getSupervisor() == null ? null : o.getSupervisor().getNome(),
                Mapper.deteccao(o.getDeteccao(), o), acoes, orientacoes,
                o.getAlertas().stream().map(Mapper::alerta).toList());
    }

    private boolean categoria(OcorrenciaRisco o, String categoria) {
        if (categoria == null || categoria.isBlank() || categoria.equals("todos")) return true;
        return switch (categoria) {
            case "ativos" -> o.getStatus().ativa();
            case "criticos" -> o.getSeveridade() == NivelRisco.CRITICO;
            case "em_validacao" -> o.getStatus() == StatusOcorrencia.AGUARDANDO_VALIDACAO;
            case "escalados" -> o.getStatus() == StatusOcorrencia.ESCALADA;
            case "regularizados" -> o.getStatus() == StatusOcorrencia.ENCERRADA;
            default -> true;
        };
    }

    private void exigirAcessoDoOperador(OcorrenciaRisco o, Usuario usuario) {
        if (usuario instanceof Operador && !o.getOperador().getId().equals(usuario.getId())) {
            throw new AcessoNegadoException("O operador só acessa as próprias ocorrências.");
        }
    }

    private OcorrenciaRisco carregar(Long id) {
        return repo.findDetalhadaById(id).orElseThrow(() -> new NaoEncontradoException("Ocorrência", id));
    }

    private void publicar(String tipo, OcorrenciaRisco o, String mensagem) {
        eventos.publishEvent(new EventoTempoReal(tipo, Mapper.ocorrencia(o), null, mensagem));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    private static String textoOuPadrao(String texto, String padrao) {
        return texto == null || texto.isBlank() ? padrao : texto.trim();
    }
}
