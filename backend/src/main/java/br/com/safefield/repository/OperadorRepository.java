package br.com.safefield.repository;

import br.com.safefield.domain.Operador;
import br.com.safefield.domain.Turno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperadorRepository extends JpaRepository<Operador, Long> {

    @EntityGraph(attributePaths = "area")
    List<Operador> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = "area")
    List<Operador> findByAtivoTrueAndTurnoAndAreaIsNotNull(Turno turno);

    @EntityGraph(attributePaths = "area")
    List<Operador> findByAtivoTrueAndAreaIsNotNull();

    long countByAreaId(Long areaId);
}
