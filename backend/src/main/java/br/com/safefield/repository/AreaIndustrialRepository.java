package br.com.safefield.repository;

import br.com.safefield.domain.AreaIndustrial;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaIndustrialRepository extends JpaRepository<AreaIndustrial, Long> {

    @EntityGraph(attributePaths = {"supervisor", "regras", "regras.epi"})
    List<AreaIndustrial> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = {"supervisor", "regras", "regras.epi"})
    Optional<AreaIndustrial> findWithRegrasById(Long id);

    boolean existsByNomeIgnoreCase(String nome);
}
