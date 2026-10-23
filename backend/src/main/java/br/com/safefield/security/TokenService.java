package br.com.safefield.security;

import br.com.safefield.config.SafeFieldProperties;
import br.com.safefield.domain.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final SafeFieldProperties props;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, SafeFieldProperties props, Clock clock) {
        this.encoder = encoder;
        this.props = props;
        this.clock = clock;
    }

    public record Token(String valor, Instant expiraEm) {}

    public Token gerar(Usuario usuario) {
        Instant agora = clock.instant();
        Instant expira = agora.plus(props.jwt().validadeHoras(), ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("safefield-ai")
                .issuedAt(agora)
                .expiresAt(expira)
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("perfil", List.of(usuario.getPerfil().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new Token(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), expira);
    }
}
