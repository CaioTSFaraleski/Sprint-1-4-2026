package br.com.safefield.repository;

import br.com.safefield.domain.EventoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {
    List<EventoAuditoria> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);
}
