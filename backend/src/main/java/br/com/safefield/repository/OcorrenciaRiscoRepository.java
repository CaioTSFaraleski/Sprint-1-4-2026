package br.com.safefield.repository;

import br.com.safefield.domain.OcorrenciaRisco;
import br.com.safefield.domain.StatusOcorrencia;
import br.com.safefield.domain.TipoEPI;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OcorrenciaRiscoRepository extends JpaRepository<OcorrenciaRisco, Long> {


    @EntityGraph(attributePaths = {"operador", "area", "supervisor", "episAusentes", "deteccao"})
    List<OcorrenciaRisco> findAllByOrderByDataAberturaDesc();

    @EntityGraph(attributePaths = {"operador", "area", "supervisor", "episAusentes", "deteccao"})
    List<OcorrenciaRisco> findByDataAberturaBetween(LocalDateTime inicio, LocalDateTime fim);

    @EntityGraph(attributePaths = {"operador", "area", "supervisor", "episAusentes", "deteccao"})
    List<OcorrenciaRisco> findByStatusIn(Collection<StatusOcorrencia> status);

    @EntityGraph(attributePaths = {"operador", "area", "supervisor", "episAusentes", "deteccao"})
    List<OcorrenciaRisco> findByOperadorIdOrderByDataAberturaDesc(Long operadorId);

    @EntityGraph(attributePaths = {"operador", "area", "supervisor", "episAusentes", "deteccao", "acoes", "acoes.autor"})
    Optional<OcorrenciaRisco> findDetalhadaById(Long id);

    @EntityGraph(attributePaths = {"deteccao"})
    List<OcorrenciaRisco> findByDeteccaoIdIn(Collection<Long> deteccaoIds);

    boolean existsByOperadorIdAndDataAberturaAfter(Long operadorId, LocalDateTime desde);

    /** O operador já teve ocorrência por falta de algum destes tipos de EPI desde a data informada? */
    @Query("""
            select count(o) > 0 from OcorrenciaRisco o join o.episAusentes e
            where o.operador.id = :operadorId and o.dataAbertura > :desde and e.tipo in :tipos""")
    boolean existsReincidencia(@Param("operadorId") Long operadorId, @Param("desde") LocalDateTime desde,
                               @Param("tipos") Collection<TipoEPI> tipos);
}
