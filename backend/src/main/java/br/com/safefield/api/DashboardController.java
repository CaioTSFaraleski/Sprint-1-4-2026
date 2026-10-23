package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.DashboardDTO;
import br.com.safefield.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
@PreAuthorize("hasAnyRole('SUPERVISOR','GESTOR')")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping
    @Operation(summary = "Indicadores do painel: alertas ativos, conformidade, tempo de regularização, reincidência")
    public DashboardDTO resumo() {
        return dashboard.resumo();
    }
}
