package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.DeteccaoDTO;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.api.dto.Requests.DeteccaoRequest;
import br.com.safefield.domain.*;
import br.com.safefield.realtime.EventoTempoReal;
import br.com.safefield.repository.AreaIndustrialRepository;
import br.com.safefield.repository.DeteccaoEPIRepository;
import br.com.safefield.repository.OcorrenciaRiscoRepository;
import br.com.safefield.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Recebe as detecções do edge gateway (RF05), aplica as regras da área (RF06) e, havendo
 * não conformidade, abre a ocorrência e dispara os alertas (RF07, RF08, RF09).
 */
@Service
@Transactional
public class DeteccaoService {

    private final DeteccaoEPIRepository deteccoes;
    private final UsuarioRepository usuarios;
    private final AreaIndustrialRepository areas;
    private final OcorrenciaRiscoRepository ocorrencias;
    private final MotorRegrasService motor;
    private final OcorrenciaService ocorrenciaService;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public DeteccaoService(DeteccaoEPIRepository deteccoes, UsuarioRepository usuarios, AreaIndustrialRepository areas,
                           OcorrenciaRiscoRepository ocorrencias, MotorRegrasService motor,
                           OcorrenciaService ocorrenciaService, ApplicationEventPublisher eventos, Clock clock) {
        this.deteccoes = deteccoes;
        this.usuarios = usuarios;
        this.areas = areas;
        this.ocorrencias = ocorrencias;
        this.motor = motor;
        this.ocorrenciaService = ocorrenciaService;
        this.eventos = eventos;
        this.clock = clock;
    }

    public DeteccaoDTO processar(DeteccaoRequest req) {
        Operador operador = usuarios.findById(req.operadorId())
                .filter(Operador.class::isInstance).map(Operador.class::cast)
                .orElseThrow(() -> new NaoEncontradoException("Operador", req.operadorId()));
        AreaIndustrial area = areas.findWithRegrasById(req.areaId())
                .orElseThrow(() -> new NaoEncontradoException("Área", req.areaId()));
        if (!area.isAtiva()) {
            throw new NegocioException("A área " + area.getNome() + " está inativa e não recebe detecções.");
        }

        LocalDateTime quando = req.dataHora() == null ? LocalDateTime.now(clock) : req.dataHora();
        Set<TipoEPI> detectados = req.episDetectados().isEmpty()
                ? EnumSet.noneOf(TipoEPI.class) : EnumSet.copyOf(req.episDetectados());
        BigDecimal confianca = req.confianca() == null ? new BigDecimal("0.900") : req.confianca();
        OrigemDeteccao origem = req.origem() == null ? OrigemDeteccao.GATEWAY : req.origem();

        DeteccaoEPI deteccao = new DeteccaoEPI(quando, operador, area, detectados, confianca, req.evidenciaUrl(),
                origem, req.dispositivo());

        Turno turno = Turno.doHorario(quando.toLocalTime());
        MotorRegrasService.Avaliacao avaliacao = motor.avaliar(area, turno, detectados, false);
        // reincidência: o mesmo EPI já tinha faltado para este operador nos 7 dias anteriores
        boolean reincidente = !avaliacao.conforme() && ocorrencias.existsReincidencia(operador.getId(),
                quando.minusDays(7), avaliacao.episAusentes().stream().map(EPI::getTipo).toList());
        if (reincidente) avaliacao = motor.avaliar(area, turno, detectados, true);
        deteccao.setConforme(avaliacao.conforme());
        deteccao = deteccoes.save(deteccao);

        if (avaliacao.conforme()) {
            DeteccaoDTO dto = Mapper.deteccao(deteccao, null);
            eventos.publishEvent(new EventoTempoReal(EventoTempoReal.DETECCAO_CONFORME, null, dto,
                    operador.getNome() + " conforme em " + area.getNome()));
            return dto;
        }
        OcorrenciaRisco ocorrencia = ocorrenciaService.abrir(deteccao, avaliacao, reincidente);
        return Mapper.deteccao(deteccao, ocorrencia);
    }
}
