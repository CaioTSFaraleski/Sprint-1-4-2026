package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.LoginResponse;
import br.com.safefield.api.dto.Dtos.UsuarioDTO;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.api.dto.Requests.LoginRequest;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService auth;
    private final UsuarioAtual usuarioAtual;

    public AuthController(AuthService auth, UsuarioAtual usuarioAtual) {
        this.auth = auth;
        this.usuarioAtual = usuarioAtual;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica com e-mail e senha e devolve o token JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req.email(), req.senha());
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "Dados do usuário logado")
    public UsuarioDTO me() {
        return Mapper.usuario(usuarioAtual.obrigatorio());
    }
}
