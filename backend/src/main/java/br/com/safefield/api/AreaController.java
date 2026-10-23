package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.AreaDTO;
import br.com.safefield.api.dto.Dtos.RegraDTO;
import br.com.safefield.api.dto.Requests.AreaRequest;
import br.com.safefield.api.dto.Requests.RegraRequest;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/areas")
@Tag(name = "Áreas industriais e regras de segurança")
public class AreaController {

    private final AreaService areas;
    private final UsuarioAtual usuarioAtual;

    public AreaController(AreaService areas, UsuarioAtual usuarioAtual) {
        this.areas = areas;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    @Operation(summary = "Lista as áreas com suas regras")
    public List<AreaDTO> listar() {
        return areas.listar();
    }

    @GetMapping("/{id}")
    public AreaDTO detalhe(@PathVariable Long id) {
        return areas.detalhe(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    @Operation(summary = "Cadastra uma área industrial (RF02)")
    public AreaDTO criar(@Valid @RequestBody AreaRequest req) {
        return areas.criar(req, usuarioAtual.obrigatorio());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    public AreaDTO atualizar(@PathVariable Long id, @Valid @RequestBody AreaRequest req) {
        return areas.atualizar(id, req, usuarioAtual.obrigatorio());
    }

    @GetMapping("/{id}/regras")
    @Operation(summary = "Regras de segurança da área (RF04)")
    public List<RegraDTO> regras(@PathVariable Long id) {
        return areas.detalhe(id).regras();
    }

    @PostMapping("/{id}/regras")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    @Operation(summary = "Adiciona uma regra: EPI obrigatório na área, por turno/atividade (RF03, RF04)")
    public AreaDTO adicionarRegra(@PathVariable Long id, @Valid @RequestBody RegraRequest req) {
        return areas.adicionarRegra(id, req, usuarioAtual.obrigatorio());
    }

    @DeleteMapping("/{id}/regras/{regraId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    public AreaDTO removerRegra(@PathVariable Long id, @PathVariable Long regraId) {
        return areas.removerRegra(id, regraId, usuarioAtual.obrigatorio());
    }
}
