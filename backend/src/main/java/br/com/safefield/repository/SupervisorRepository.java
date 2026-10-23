package br.com.safefield.repository;

import br.com.safefield.domain.SupervisorSeguranca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupervisorRepository extends JpaRepository<SupervisorSeguranca, Long> {
}
