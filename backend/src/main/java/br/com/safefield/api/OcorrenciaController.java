package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.OcorrenciaDetalheDTO;
import br.com.safefield.api.dto.Dtos.OcorrenciaResumoDTO;
import br.com.safefield.api.dto.Requests.ComentarioRequest;
import br.com.safefield.api.dto.Requests.ObservacaoRequest;
import br.com.safefield.api.dto.Requests.ValidacaoRequest;
import br.com.safefield.domain.NivelRisco;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.OcorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ocorrencias")
@Tag(name = "Ocorrências de risco")
public class OcorrenciaController {

    private final OcorrenciaService ocorrencias;
    private final UsuarioAtual usuarioAtual;

    public OcorrenciaController(OcorrenciaService ocorrencias, UsuarioAtual usuarioAtual) {
        this.ocorrencias = ocorrencias;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    @Operation(summary = "Lista ocorrências. categoria: todos | ativos | criticos | em_validacao | escalados | regularizados. "
            + "O operador vê apenas as próprias.")
    public List<OcorrenciaResumoDTO> listar(@RequestParam(required = false) String categoria,
                                            @RequestParam(required = false) Long areaId,
                                            @RequestParam(required = false) Long operadorId,
                                            @RequestParam(required = false) NivelRisco severidade,
                                            @RequestParam(required = false) String busca,
                                            @RequestParam(required = false) Integer dias) {
        return ocorrencias.listar(new OcorrenciaService.Filtro(categoria, areaId, operadorId, severidade, busca, dias),
                usuarioAtual.obrigatorio());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da ocorrência com evidência, linha do tempo de ações e orientações da área")
    public OcorrenciaDetalheDTO detalhe(@PathVariable Long id) {
        return ocorrencias.detalhe(id, usuarioAtual.obrigatorio());
    }

    @PostMapping("/{id}/regularizacao")
    @PreAuthorize("hasAnyRole('OPERADOR','SUPERVISOR')")
    @Operation(summary = "Operador confirma que regularizou o uso do EPI (RF10)")
    public OcorrenciaDetalheDTO regularizar(@PathVariable Long id, @Valid @RequestBody(required = false) ObservacaoRequest req) {
        return ocorrencias.regularizar(id, usuarioAtual.obrigatorio(), req == null ? null : req.observacao());
    }

    @PostMapping("/{id}/validacao")
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Operation(summary = "Supervisor valida (encerra) ou rejeita a regularização (RF11, RN04)")
    public OcorrenciaDetalheDTO validar(@PathVariable Long id, @Valid @RequestBody ValidacaoRequest req) {
        return ocorrencias.validar(id, usuarioAtual.obrigatorio(), req.aprovada(), req.observacao());
    }

    @PostMapping("/{id}/escalonamento")
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Operation(summary = "Supervisor escala um risco crítico ou reincidente; gestores são alertados (RF11)")
    public OcorrenciaDetalheDTO escalar(@PathVariable Long id, @Valid @RequestBody(required = false) ObservacaoRequest req) {
        return ocorrencias.escalar(id, usuarioAtual.obrigatorio(), req == null ? null : req.observacao());
    }

    @PostMapping("/{id}/comentarios")
    @Operation(summary = "Adiciona um comentário ao histórico da ocorrência (RF11)")
    public OcorrenciaDetalheDTO comentar(@PathVariable Long id, @Valid @RequestBody ComentarioRequest req) {
        return ocorrencias.comentar(id, usuarioAtual.obrigatorio(), req.observacao());
    }
}
