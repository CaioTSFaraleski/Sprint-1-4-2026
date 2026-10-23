package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.EpiDTO;
import br.com.safefield.api.dto.Requests.EpiRequest;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.EpiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/epis")
@Tag(name = "EPIs")
public class EpiController {

    private final EpiService epis;
    private final UsuarioAtual usuarioAtual;

    public EpiController(EpiService epis, UsuarioAtual usuarioAtual) {
        this.epis = epis;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    @Operation(summary = "Catálogo de EPIs com situação do CA")
    public List<EpiDTO> listar() {
        return epis.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    @Operation(summary = "Cadastra EPI; opcionalmente entrega a um colaborador e torna obrigatório em uma área")
    public EpiDTO criar(@Valid @RequestBody EpiRequest req) {
        return epis.criar(req, usuarioAtual.obrigatorio());
    }
}
