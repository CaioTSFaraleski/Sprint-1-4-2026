package br.com.safefield.api.dto;

import br.com.safefield.domain.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** Corpos de requisição aceitos pela API, com validação (Bean Validation). */
public final class Requests {

    private Requests() {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {}

    public record DeteccaoRequest(@NotNull Long operadorId, @NotNull Long areaId,
                                  @NotNull Set<TipoEPI> episDetectados,
                                  @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confianca,
                                  String evidenciaUrl, OrigemDeteccao origem, String dispositivo,
                                  LocalDateTime dataHora) {}

    public record AreaRequest(@NotBlank @Size(max = 80) String nome, @NotBlank @Size(max = 60) String setor,
                              @NotNull NivelRisco nivelRisco, @NotEmpty List<Turno> turnos,
                              @Size(max = 500) String descricao, Long supervisorId, Boolean ativa) {}

    public record RegraRequest(@NotNull Long epiId, @NotBlank @Size(max = 300) String descricao,
                               @NotNull NivelRisco severidadePadrao, Turno turno,
                               @Size(max = 100) String atividade) {}

    public record EpiRequest(@NotBlank @Size(max = 100) String nome, @NotNull TipoEPI tipo,
                             @NotBlank @Pattern(regexp = "\\d{3,6}", message = "deve ter de 3 a 6 dígitos") String codigoCA,
                             @NotNull LocalDate validadeCA, @Size(max = 500) String observacoes,
                             Long operadorId, Long areaObrigatoriaId, NivelRisco severidadeObrigatoria) {}

    public record EntregaRequest(@NotNull Long epiId, LocalDate dataEntrega, LocalDate dataValidade) {}

    public record ObservacaoRequest(@Size(max = 500) String observacao) {}

    public record ComentarioRequest(@NotBlank @Size(max = 500) String observacao) {}

    public record ValidacaoRequest(@NotNull Boolean aprovada, @Size(max = 500) String observacao) {}

    public record UsuarioRequest(@NotBlank @Size(max = 100) String nome, @NotBlank @Size(max = 20) String matricula,
                                 @NotBlank @Email String email, @NotBlank @Size(min = 6, max = 60) String senha,
                                 @NotNull Perfil perfil, Turno turno, @Size(max = 60) String funcao, Long areaId,
                                 @Size(max = 80) String unidadeIndustrial) {}

    public record SimuladorRequest(@NotNull Boolean ativo) {}

    public record SimularDeteccaoRequest(Boolean naoConforme, Long operadorId) {}
}
