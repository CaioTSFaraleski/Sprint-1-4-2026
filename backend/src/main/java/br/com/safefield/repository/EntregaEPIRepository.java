package br.com.safefield.repository;

import br.com.safefield.domain.EntregaEPI;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntregaEPIRepository extends JpaRepository<EntregaEPI, Long> {

    @EntityGraph(attributePaths = "epi")
    List<EntregaEPI> findByOperadorIdOrderByDataEntregaDesc(Long operadorId);
}
