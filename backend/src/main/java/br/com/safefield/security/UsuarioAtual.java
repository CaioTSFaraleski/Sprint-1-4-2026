package br.com.safefield.security;

import br.com.safefield.domain.Usuario;
import br.com.safefield.repository.UsuarioRepository;
import br.com.safefield.service.AcessoNegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Resolve o usuário logado a partir do token JWT da requisição atual. */
@Component
public class UsuarioAtual {

    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Optional<Usuario> talvez() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return Optional.empty();
        Object uid = jwt.getClaim("uid");
        if (uid == null) return Optional.empty();
        return usuarios.findById(((Number) uid).longValue());
    }

    public Usuario obrigatorio() {
        return talvez().orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado."));
    }
}
