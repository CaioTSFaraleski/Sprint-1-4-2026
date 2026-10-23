package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.ColaboradorDetalheDTO;
import br.com.safefield.api.dto.Dtos.ColaboradorResumoDTO;
import br.com.safefield.api.dto.Dtos.EntregaDTO;
import br.com.safefield.api.dto.Requests.EntregaRequest;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.ColaboradorService;
import br.com.safefield.service.EpiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/colaboradores")
@Tag(name = "Colaboradores e EPIs")
public class ColaboradorController {

    private final ColaboradorService colaboradores;
    private final EpiService epis;
    private final UsuarioAtual usuarioAtual;

    public ColaboradorController(ColaboradorService colaboradores, EpiService epis, UsuarioAtual usuarioAtual) {
        this.colaboradores = colaboradores;
        this.epis = epis;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    @Operation(summary = "Busca colaboradores por nome, matrícula, área ou setor")
    public List<ColaboradorResumoDTO> listar(@RequestParam(required = false) String busca,
                                             @RequestParam(required = false) Long areaId,
                                             @RequestParam(required = false) String situacao) {
        return colaboradores.listar(busca, areaId, situacao);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ficha do colaborador: EPIs obrigatórios, pendentes, entregas e histórico")
    public ColaboradorDetalheDTO detalhe(@PathVariable Long id) {
        return colaboradores.detalhe(id, usuarioAtual.obrigatorio());
    }

    @PostMapping("/{id}/entregas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    @Operation(summary = "Registra a entrega de um EPI ao colaborador")
    public EntregaDTO entregar(@PathVariable Long id, @Valid @RequestBody EntregaRequest req) {
        return epis.entregar(id, req, usuarioAtual.obrigatorio());
    }
}
