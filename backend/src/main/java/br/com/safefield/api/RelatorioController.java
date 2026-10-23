package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.RelatorioDTO;
import br.com.safefield.domain.NivelRisco;
import br.com.safefield.domain.TipoEPI;
import br.com.safefield.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatórios de conformidade")
@PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
public class RelatorioController {

    private final RelatorioService relatorios;

    public RelatorioController(RelatorioService relatorios) {
        this.relatorios = relatorios;
    }

    @GetMapping("/conformidade")
    @Operation(summary = "Relatório de conformidade por área, setor, período, turno e tipo de EPI (RF13). Padrão: 30 dias.")
    public RelatorioDTO conformidade(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) TipoEPI tipoEpi,
            @RequestParam(required = false) NivelRisco severidade) {
        return relatorios.gerar(inicio, fim, areaId, setor, tipoEpi, severidade);
    }

    @GetMapping("/conformidade.csv")
    @Operation(summary = "Mesmo relatório exportado em CSV")
    public ResponseEntity<byte[]> conformidadeCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) TipoEPI tipoEpi,
            @RequestParam(required = false) NivelRisco severidade) {
        RelatorioDTO r = relatorios.gerar(inicio, fim, areaId, setor, tipoEpi, severidade);
        String nome = "relatorio-conformidade-" + r.inicio() + "-a-" + r.fim() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(relatorios.csv(r).getBytes(StandardCharsets.UTF_8));
    }
}
