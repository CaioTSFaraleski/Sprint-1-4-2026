package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.AuditoriaDTO;
import br.com.safefield.domain.TipoEventoAuditoria;
import br.com.safefield.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Trilha de auditoria")
@PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
public class AuditoriaController {

    private final AuditoriaService auditoria;

    public AuditoriaController(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    @Operation(summary = "Eventos críticos em ordem cronológica, com filtros (RF15). Padrão: últimos 7 dias.")
    public List<AuditoriaDTO> consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) TipoEventoAuditoria tipo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) Long ocorrenciaId) {
        return auditoria.consultar(inicio, fim, areaId, tipo, autor, ocorrenciaId);
    }
}
