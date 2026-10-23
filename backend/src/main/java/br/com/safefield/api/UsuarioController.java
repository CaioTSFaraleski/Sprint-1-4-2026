package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.UsuarioDTO;
import br.com.safefield.api.dto.Requests.UsuarioRequest;
import br.com.safefield.domain.Perfil;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {

    private final UsuarioService usuarios;
    private final UsuarioAtual usuarioAtual;

    public UsuarioController(UsuarioService usuarios, UsuarioAtual usuarioAtual) {
        this.usuarios = usuarios;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
    @Operation(summary = "Lista usuários, opcionalmente por perfil")
    public List<UsuarioDTO> listar(@RequestParam(required = false) Perfil perfil) {
        return usuarios.listar(perfil);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Cadastra operador, supervisor ou gestor (RF01)")
    public UsuarioDTO criar(@Valid @RequestBody UsuarioRequest req) {
        return usuarios.criar(req, usuarioAtual.obrigatorio());
    }
}
