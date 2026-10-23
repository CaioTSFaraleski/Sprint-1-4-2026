package br.com.safefield.repository;

import br.com.safefield.domain.GestorIndustrial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GestorRepository extends JpaRepository<GestorIndustrial, Long> {
}
