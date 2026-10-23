package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.DeteccaoDTO;
import br.com.safefield.api.dto.Requests.DeteccaoRequest;
import br.com.safefield.config.SafeFieldProperties;
import br.com.safefield.domain.Perfil;
import br.com.safefield.domain.Usuario;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.AcessoNegadoException;
import br.com.safefield.service.DeteccaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/deteccoes")
@Tag(name = "Detecções (edge gateway)")
public class DeteccaoController {

    private final DeteccaoService deteccoes;
    private final SafeFieldProperties props;
    private final UsuarioAtual usuarioAtual;

    public DeteccaoController(DeteccaoService deteccoes, SafeFieldProperties props, UsuarioAtual usuarioAtual) {
        this.deteccoes = deteccoes;
        this.props = props;
        this.usuarioAtual = usuarioAtual;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Recebe uma detecção do edge gateway (RF05). Autenticação: cabeçalho X-Gateway-Key "
            + "ou token de supervisor/gestor.")
    public DeteccaoDTO receber(@RequestHeader(value = "X-Gateway-Key", required = false) String chave,
                               @Valid @RequestBody DeteccaoRequest req) {
        if (!chaveValida(chave)) {
            Usuario u = usuarioAtual.talvez()
                    .orElseThrow(() -> new AcessoNegadoException("Envie o cabeçalho X-Gateway-Key do edge gateway."));
            if (u.getPerfil() == Perfil.OPERADOR) {
                throw new AcessoNegadoException("Operadores não enviam detecções.");
            }
        }
        return deteccoes.processar(req);
    }

    private boolean chaveValida(String chave) {
        if (chave == null) return false;
        return MessageDigest.isEqual(chave.getBytes(StandardCharsets.UTF_8),
                props.gateway().apiKey().getBytes(StandardCharsets.UTF_8));
    }
}
