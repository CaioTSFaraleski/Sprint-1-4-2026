package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.LoginResponse;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.domain.Usuario;
import br.com.safefield.repository.UsuarioRepository;
import br.com.safefield.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    public LoginResponse login(String email, String senha) {
        Usuario u = usuarios.findByEmailIgnoreCase(email.trim())
                .filter(x -> encoder.matches(senha, x.getSenhaHash()))
                .orElseThrow(() -> new CredenciaisInvalidasException());
        if (!u.isAtivo()) throw new NegocioException("Usuário inativo. Procure o gestor da unidade.");
        TokenService.Token token = tokens.gerar(u);
        return new LoginResponse(token.valor(), LocalDateTime.ofInstant(token.expiraEm(), ZoneId.of("America/Sao_Paulo")),
                Mapper.usuario(u));
    }

    public static class CredenciaisInvalidasException extends RuntimeException {
        public CredenciaisInvalidasException() {
            super("E-mail ou senha inválidos.");
        }
    }
}
