package br.com.safefield.repository;

import br.com.safefield.domain.EPI;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EPIRepository extends JpaRepository<EPI, Long> {
    List<EPI> findAllByOrderByNomeAsc();
}
