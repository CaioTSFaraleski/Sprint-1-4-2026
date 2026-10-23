package br.com.safefield.api.dto;

import br.com.safefield.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Objetos de resposta da API (JSON). As entidades JPA nunca são expostas diretamente. */
public final class Dtos {

    private Dtos() {}

    public record UsuarioDTO(Long id, String nome, String matricula, String email, Perfil perfil, boolean ativo,
                             Turno turno, String funcao, Long areaId, String areaNome, String unidadeIndustrial) {}

    public record LoginResponse(String token, LocalDateTime expiraEm, UsuarioDTO usuario) {}

    public record RegraDTO(Long id, Long epiId, String epiNome, TipoEPI tipoEpi, String tipoEpiDescricao,
                           String descricao, boolean obrigatoria, NivelRisco severidadePadrao, Turno turno,
                           String atividade) {}

    public record AreaDTO(Long id, String nome, String setor, NivelRisco nivelRisco, List<Turno> turnos,
                          String descricao, boolean ativa, Long supervisorId, String supervisorNome,
                          List<RegraDTO> regras, long totalOperadores, long ocorrenciasAtivas) {}

    public record EpiDTO(Long id, String nome, TipoEPI tipo, String tipoDescricao, String codigoCA,
                         LocalDate validadeCA, boolean certificadoValido, String observacoes, boolean ativo) {}

    public record EntregaDTO(Long id, Long epiId, String epiNome, TipoEPI tipoEpi, String codigoCA,
                             LocalDate dataEntrega, LocalDate dataValidade, StatusEntrega status) {}

    public record DeteccaoDTO(Long id, LocalDateTime dataHora, Long operadorId, String operadorNome,
                              Long areaId, String areaNome, List<TipoEPI> episDetectados, BigDecimal confianca,
                              String evidenciaUrl, OrigemDeteccao origem, String dispositivo, boolean conforme,
                              Long ocorrenciaId, String ocorrenciaCodigo) {}

    public record EpiAusenteDTO(Long epiId, String nome, TipoEPI tipo, String tipoDescricao) {}

    public record OcorrenciaResumoDTO(Long id, String codigo, LocalDateTime dataAbertura, Long operadorId,
                                      String operadorNome, String operadorMatricula, Long areaId, String areaNome,
                                      String setor, List<EpiAusenteDTO> episAusentes, NivelRisco severidade,
                                      StatusOcorrencia status, OrigemDeteccao origem, String dispositivo,
                                      boolean reincidente, LocalDateTime dataRegularizacao,
                                      LocalDateTime dataEncerramento) {}

    public record AcaoDTO(Long id, LocalDateTime dataHora, TipoAcao tipo, String autorNome, String autorPerfil,
                          String observacao) {}

    public record OcorrenciaDetalheDTO(OcorrenciaResumoDTO resumo, String descricao, String supervisorNome,
                                       DeteccaoDTO deteccao, List<AcaoDTO> acoes, List<RegraDTO> orientacoes,
                                       List<AlertaDTO> alertas) {}

    public record AlertaDTO(Long id, Long ocorrenciaId, String ocorrenciaCodigo, String destinatarioNome,
                            CanalAlerta canal, String mensagem, LocalDateTime dataEnvio, StatusAlerta status,
                            NivelRisco severidade, String areaNome, StatusOcorrencia statusOcorrencia) {}

    public record EpiObrigatorioDTO(TipoEPI tipo, String tipoDescricao, String epiNome, NivelRisco severidade,
                                    boolean detectado, boolean entregue) {}

    public record ColaboradorResumoDTO(Long id, String nome, String matricula, String funcao, Turno turno,
                                       Long areaId, String areaNome, String setor, String situacao,
                                       int episPendentes, long ocorrenciasAtivas, LocalDateTime ultimaDeteccao) {}

    public record ColaboradorDetalheDTO(ColaboradorResumoDTO resumo, String email,
                                        List<EpiObrigatorioDTO> episObrigatorios, List<EntregaDTO> entregas,
                                        DeteccaoDTO ultimaDeteccao, List<OcorrenciaResumoDTO> ocorrencias,
                                        long totalOcorrencias, boolean reincidente) {}

    public record AuditoriaDTO(Long id, LocalDateTime dataHora, TipoEventoAuditoria tipo, String autorNome,
                               String autorPerfil, Long areaId, String areaNome, Long ocorrenciaId,
                               String ocorrenciaCodigo, NivelRisco severidade, String descricao) {}

    public record SerieDiaDTO(LocalDate data, long deteccoes, long conformes, double taxaConformidade,
                              long ocorrencias, long criticas) {}

    public record AreaRiscoDTO(Long id, String nome, String setor, NivelRisco nivelRisco, long ocorrenciasAtivas,
                               long ocorrencias7d, double taxaConformidade7d) {}

    public record DashboardDTO(long alertasAtivos, long criticasAtivas, long aguardandoValidacao,
                               long escaladas, long deteccoesHoje, double taxaConformidadeHoje,
                               double taxaConformidade7d, double variacaoConformidade, Double tempoMedioRegularizacaoMin,
                               double taxaReincidencia, List<SerieDiaDTO> serie7d, List<AreaRiscoDTO> areas,
                               List<OcorrenciaResumoDTO> ocorrenciasAtivas, List<DeteccaoDTO> ultimasDeteccoes,
                               boolean simuladorAtivo) {}

    public record LinhaAreaDTO(Long areaId, String area, String setor, long deteccoes, double taxaConformidade,
                               long ocorrencias, long criticas, Double tempoMedioRegularizacaoMin) {}

    public record LinhaEpiDTO(TipoEPI tipo, String descricao, long ocorrencias, double percentual) {}

    public record LinhaTurnoDTO(Turno turno, long deteccoes, double taxaConformidade, long ocorrencias) {}

    public record ReincidenteDTO(Long operadorId, String nome, String matricula, String area, long ocorrencias) {}

    public record RelatorioDTO(LocalDate inicio, LocalDate fim, Long areaId, String setor, TipoEPI tipoEpi,
                               NivelRisco severidade, long totalDeteccoes, long deteccoesConformes,
                               double indiceConformidade, long totalOcorrencias, long ocorrenciasCriticas,
                               Double tempoMedioRegularizacaoMin, double taxaReincidencia,
                               List<SerieDiaDTO> serieDiaria, List<LinhaAreaDTO> porArea, List<LinhaEpiDTO> porEpi,
                               List<LinhaTurnoDTO> porTurno, List<ReincidenteDTO> reincidentes,
                               LocalDateTime geradoEm) {}

    public record SimuladorDTO(boolean ativo, long intervaloMs) {}

    public record ErroDTO(int status, String erro, String mensagem, List<String> detalhes) {}
}
