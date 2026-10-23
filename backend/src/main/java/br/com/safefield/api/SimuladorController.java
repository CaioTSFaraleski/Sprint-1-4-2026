package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.DeteccaoDTO;
import br.com.safefield.api.dto.Dtos.SimuladorDTO;
import br.com.safefield.api.dto.Requests.SimularDeteccaoRequest;
import br.com.safefield.api.dto.Requests.SimuladorRequest;
import br.com.safefield.service.SimuladorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/simulador")
@Tag(name = "Simulador do edge gateway")
@PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
public class SimuladorController {

    private final SimuladorService simulador;

    public SimuladorController(SimuladorService simulador) {
        this.simulador = simulador;
    }

    @GetMapping
    public SimuladorDTO estado() {
        return new SimuladorDTO(simulador.isAtivo(), simulador.intervaloMs());
    }

    @PutMapping
    @Operation(summary = "Liga ou desliga a geração automática de detecções")
    public SimuladorDTO alterar(@Valid @RequestBody SimuladorRequest req) {
        simulador.setAtivo(req.ativo());
        return estado();
    }

    @PostMapping("/deteccoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gera uma detecção agora (naoConforme=true força EPI faltando)")
    public DeteccaoDTO gerar(@RequestBody(required = false) SimularDeteccaoRequest req) {
        return simulador.gerar(req == null ? null : req.naoConforme(), req == null ? null : req.operadorId());
    }
}
