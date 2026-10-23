package br.com.safefield.repository;

import br.com.safefield.domain.DeteccaoEPI;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeteccaoEPIRepository extends JpaRepository<DeteccaoEPI, Long> {

    @EntityGraph(attributePaths = {"operador", "area"})
    List<DeteccaoEPI> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    @EntityGraph(attributePaths = {"operador", "area"})
    List<DeteccaoEPI> findTop15ByOrderByDataHoraDesc();

    Optional<DeteccaoEPI> findTopByOperadorIdOrderByDataHoraDesc(Long operadorId);
}
