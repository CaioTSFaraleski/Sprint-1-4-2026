package br.com.safefield.api.dto;

import br.com.safefield.api.dto.Dtos.*;
import br.com.safefield.domain.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Conversão de entidades para DTOs. Deve ser chamado dentro de uma transação (associações lazy). */
public final class Mapper {

    private Mapper() {}

    public static UsuarioDTO usuario(Usuario u) {
        Turno turno = null;
        String funcao = null;
        Long areaId = null;
        String areaNome = null;
        String unidade = null;
        if (u instanceof Operador o) {
            turno = o.getTurno();
            funcao = o.getFuncao();
            if (o.getArea() != null) {
                areaId = o.getArea().getId();
                areaNome = o.getArea().getNome();
            }
        } else if (u instanceof GestorIndustrial g) {
            unidade = g.getUnidadeIndustrial();
        }
        return new UsuarioDTO(u.getId(), u.getNome(), u.getMatricula(), u.getEmail(), u.getPerfil(), u.isAtivo(),
                turno, funcao, areaId, areaNome, unidade);
    }

    public static RegraDTO regra(RegraSeguranca r) {
        EPI e = r.getEpi();
        return new RegraDTO(r.getId(), e.getId(), e.getNome(), e.getTipo(), e.getTipo().getDescricao(),
                r.getDescricao(), r.isObrigatoria(), r.getSeveridadePadrao(), r.getTurno(), r.getAtividade());
    }

    public static AreaDTO area(AreaIndustrial a, long totalOperadores, long ocorrenciasAtivas) {
        List<Turno> turnos = Arrays.stream(a.getTurnos().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Turno::valueOf).toList();
        List<RegraDTO> regras = a.getRegras().stream()
                .sorted(Comparator.comparing((RegraSeguranca r) -> r.getSeveridadePadrao()).reversed())
                .map(Mapper::regra).toList();
        SupervisorSeguranca s = a.getSupervisor();
        return new AreaDTO(a.getId(), a.getNome(), a.getSetor(), a.getNivelRisco(), turnos, a.getDescricao(),
                a.isAtiva(), s == null ? null : s.getId(), s == null ? null : s.getNome(), regras,
                totalOperadores, ocorrenciasAtivas);
    }

    public static EpiDTO epi(EPI e, LocalDate hoje) {
        return new EpiDTO(e.getId(), e.getNome(), e.getTipo(), e.getTipo().getDescricao(), e.getCodigoCA(),
                e.getValidadeCA(), e.validarCertificacao(hoje), e.getObservacoes(), e.isAtivo());
    }

    public static EntregaDTO entrega(EntregaEPI en, LocalDate hoje) {
        EPI e = en.getEpi();
        return new EntregaDTO(en.getId(), e.getId(), e.getNome(), e.getTipo(), e.getCodigoCA(),
                en.getDataEntrega(), en.getDataValidade(), en.statusEm(hoje));
    }

    public static DeteccaoDTO deteccao(DeteccaoEPI d, OcorrenciaRisco ocorrencia) {
        List<TipoEPI> detectados = d.getEpisDetectados().stream().sorted().toList();
        return new DeteccaoDTO(d.getId(), d.getDataHora(), d.getOperador().getId(), d.getOperador().getNome(),
                d.getArea().getId(), d.getArea().getNome(), detectados, d.getConfianca(), d.getEvidenciaUrl(),
                d.getOrigem(), d.getDispositivo(), d.isConforme(),
                ocorrencia == null ? null : ocorrencia.getId(), ocorrencia == null ? null : ocorrencia.getCodigo());
    }

    public static EpiAusenteDTO epiAusente(EPI e) {
        return new EpiAusenteDTO(e.getId(), e.getNome(), e.getTipo(), e.getTipo().getDescricao());
    }

    public static OcorrenciaResumoDTO ocorrencia(OcorrenciaRisco o) {
        List<EpiAusenteDTO> ausentes = o.getEpisAusentes().stream()
                .sorted(Comparator.comparing(EPI::getTipo)).map(Mapper::epiAusente).toList();
        DeteccaoEPI d = o.getDeteccao();
        return new OcorrenciaResumoDTO(o.getId(), o.getCodigo(), o.getDataAbertura(), o.getOperador().getId(),
                o.getOperador().getNome(), o.getOperador().getMatricula(), o.getArea().getId(), o.getArea().getNome(),
                o.getArea().getSetor(), ausentes, o.getSeveridade(), o.getStatus(), d.getOrigem(), d.getDispositivo(),
                o.isReincidente(), o.getDataRegularizacao(), o.getDataEncerramento());
    }

    public static AcaoDTO acao(AcaoOcorrencia a) {
        Usuario autor = a.getAutor();
        return new AcaoDTO(a.getId(), a.getDataHora(), a.getTipo(),
                autor == null ? "SafeField AI" : autor.getNome(),
                autor == null ? "SISTEMA" : autor.getPerfil().name(), a.getObservacao());
    }

    public static AlertaDTO alerta(Alerta a) {
        OcorrenciaRisco o = a.getOcorrencia();
        return new AlertaDTO(a.getId(), o.getId(), o.getCodigo(), a.getDestinatario().getNome(), a.getCanal(),
                a.getMensagem(), a.getDataEnvio(), a.getStatus(), o.getSeveridade(), o.getArea().getNome(),
                o.getStatus());
    }

    public static AuditoriaDTO auditoria(EventoAuditoria e) {
        return new AuditoriaDTO(e.getId(), e.getDataHora(), e.getTipo(), e.getAutorNome(), e.getAutorPerfil(),
                e.getAreaId(), e.getAreaNome(), e.getOcorrenciaId(), e.getOcorrenciaCodigo(), e.getSeveridade(),
                e.getDescricao());
    }
}
