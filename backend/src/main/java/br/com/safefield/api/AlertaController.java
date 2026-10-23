package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.AlertaDTO;
import br.com.safefield.security.UsuarioAtual;
import br.com.safefield.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas")
@Tag(name = "Alertas")
public class AlertaController {

    private final AlertaService alertas;
    private final UsuarioAtual usuarioAtual;

    public AlertaController(AlertaService alertas, UsuarioAtual usuarioAtual) {
        this.alertas = alertas;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    @Operation(summary = "Alertas do usuário logado (RF08 / RF09)")
    public List<AlertaDTO> meus(@RequestParam(defaultValue = "false") boolean incluirLidos) {
        return alertas.meus(usuarioAtual.obrigatorio(), incluirLidos);
    }

    @PatchMapping("/{id}/lido")
    public AlertaDTO marcarComoLido(@PathVariable Long id) {
        return alertas.marcarComoLido(id, usuarioAtual.obrigatorio());
    }
}
