package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.AuditoriaDTO;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.domain.*;
import br.com.safefield.repository.EventoAuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/** Trilha de auditoria imutável: só há operações de inclusão e consulta (RF15 / RNF03). */
@Service
public class AuditoriaService {

    private final EventoAuditoriaRepository repo;
    private final Clock clock;

    public AuditoriaService(EventoAuditoriaRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    public void registrar(TipoEventoAuditoria tipo, Usuario autor, AreaIndustrial area, OcorrenciaRisco ocorrencia,
                          NivelRisco severidade, String descricao) {
        registrar(LocalDateTime.now(clock), tipo, autor, area, ocorrencia, severidade, descricao);
    }

    public void registrar(LocalDateTime quando, TipoEventoAuditoria tipo, Usuario autor, AreaIndustrial area,
                          OcorrenciaRisco ocorrencia, NivelRisco severidade, String descricao) {
        repo.save(new EventoAuditoria(quando, tipo, autor, area, ocorrencia, severidade, descricao));
    }

    @Transactional(readOnly = true)
    public List<AuditoriaDTO> consultar(LocalDate inicio, LocalDate fim, Long areaId, TipoEventoAuditoria tipo,
                                        String autor, Long ocorrenciaId) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate de = inicio == null ? hoje.minusDays(7) : inicio;
        LocalDate ate = fim == null ? hoje : fim;
        String autorFiltro = autor == null ? null : autor.toLowerCase(Locale.ROOT).trim();
        return repo.findByDataHoraBetweenOrderByDataHoraDesc(de.atStartOfDay(), ate.plusDays(1).atStartOfDay())
                .stream()
                .filter(e -> areaId == null || areaId.equals(e.getAreaId()))
                .filter(e -> tipo == null || tipo == e.getTipo())
                .filter(e -> ocorrenciaId == null || ocorrenciaId.equals(e.getOcorrenciaId()))
                .filter(e -> autorFiltro == null || autorFiltro.isEmpty()
                        || e.getAutorNome().toLowerCase(Locale.ROOT).contains(autorFiltro)
                        || e.getAutorPerfil().toLowerCase(Locale.ROOT).equals(autorFiltro))
                .limit(500)
                .map(Mapper::auditoria)
                .toList();
    }
}
