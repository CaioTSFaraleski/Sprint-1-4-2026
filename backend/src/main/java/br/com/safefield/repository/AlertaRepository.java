package br.com.safefield.repository;

import br.com.safefield.domain.Alerta;
import br.com.safefield.domain.StatusAlerta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    @EntityGraph(attributePaths = {"ocorrencia", "ocorrencia.area", "ocorrencia.operador", "destinatario"})
    List<Alerta> findByDestinatarioIdAndStatusInOrderByDataEnvioDesc(Long destinatarioId, Collection<StatusAlerta> status);

    @EntityGraph(attributePaths = {"ocorrencia", "ocorrencia.area", "ocorrencia.operador", "destinatario"})
    List<Alerta> findByStatusInOrderByDataEnvioDesc(Collection<StatusAlerta> status);
}
