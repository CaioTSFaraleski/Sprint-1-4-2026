package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.DeteccaoDTO;
import br.com.safefield.api.dto.Requests.DeteccaoRequest;
import br.com.safefield.config.SafeFieldProperties;
import br.com.safefield.domain.*;
import br.com.safefield.repository.AreaIndustrialRepository;
import br.com.safefield.repository.OperadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulador do edge gateway (serviço edge-gateway-sim da arquitetura da Sprint 3).
 * Como câmeras e sensores reais estão fora do escopo do MVP, ele gera periodicamente
 * detecções de operadores do turno atual, ora conformes, ora com EPI faltando,
 * e as envia pelo mesmo fluxo de uma detecção real (DeteccaoService).
 */
@Service
public class SimuladorService {

    private static final Logger log = LoggerFactory.getLogger(SimuladorService.class);

    private final AtomicBoolean ativo;
    private final SafeFieldProperties props;
    private final OperadorRepository operadores;
    private final AreaIndustrialRepository areas;
    private final MotorRegrasService motor;
    private final DeteccaoService deteccaoService;
    private final Clock clock;
    private final Random random = new Random();

    public SimuladorService(SafeFieldProperties props, OperadorRepository operadores, AreaIndustrialRepository areas,
                            MotorRegrasService motor, DeteccaoService deteccaoService, Clock clock) {
        this.props = props;
        this.ativo = new AtomicBoolean(props.simulador().ativo());
        this.operadores = operadores;
        this.areas = areas;
        this.motor = motor;
        this.deteccaoService = deteccaoService;
        this.clock = clock;
    }

    public boolean isAtivo() {
        return ativo.get();
    }

    public void setAtivo(boolean valor) {
        ativo.set(valor);
    }

    public long intervaloMs() {
        return props.simulador().intervaloMs();
    }

    @Scheduled(initialDelayString = "${safefield.simulador.intervalo-ms}",
            fixedDelayString = "${safefield.simulador.intervalo-ms}")
    public void tick() {
        if (!ativo.get()) return;
        try {
            gerar(null, null);
        } catch (Exception e) {
            log.warn("Simulador não conseguiu gerar detecção: {}", e.getMessage());
        }
    }

    /**
     * Gera uma detecção agora.
     * @param naoConforme true força EPI faltando, false força conformidade, null sorteia
     * @param operadorId  operador específico ou null para sortear entre os do turno atual
     */
    @Transactional
    public DeteccaoDTO gerar(Boolean naoConforme, Long operadorId) {
        LocalDateTime agora = LocalDateTime.now(clock);
        Turno turno = Turno.doHorario(agora.toLocalTime());
        Operador operador = escolherOperador(operadorId, turno);
        AreaIndustrial area = areas.findWithRegrasById(operador.getArea().getId()).orElseThrow();

        Set<TipoEPI> exigidos = motor.tiposExigidos(area, turno);
        Set<TipoEPI> detectados = exigidos.isEmpty() ? EnumSet.noneOf(TipoEPI.class) : EnumSet.copyOf(exigidos);
        boolean falhar = naoConforme != null ? naoConforme
                : random.nextDouble() < props.simulador().probabilidadeNaoConformidade();
        if (falhar && !detectados.isEmpty()) {
            detectados.remove(PerfilEsquecimento.sortear(detectados, random));
            if (detectados.size() > 1 && random.nextDouble() < 0.2) {
                detectados.remove(PerfilEsquecimento.sortear(detectados, random));
            }
        }

        String codigoArea = sigla(area.getNome());
        String dispositivo = "CAM-" + codigoArea + "-0" + (1 + random.nextInt(3));
        String evidencia = "evidencias/" + dispositivo.toLowerCase() + "/"
                + agora.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".jpg";
        BigDecimal confianca = BigDecimal.valueOf(0.82 + random.nextDouble() * 0.16).setScale(3, RoundingMode.HALF_UP);

        return deteccaoService.processar(new DeteccaoRequest(operador.getId(), area.getId(), detectados, confianca,
                evidencia, OrigemDeteccao.SIMULADOR, dispositivo, agora));
    }

    private Operador escolherOperador(Long operadorId, Turno turno) {
        if (operadorId != null) {
            Operador op = operadores.findById(operadorId)
                    .orElseThrow(() -> new NaoEncontradoException("Operador", operadorId));
            if (op.getArea() == null) throw new NegocioException("Operador sem área definida.");
            return op;
        }
        List<Operador> candidatos = operadores.findByAtivoTrueAndTurnoAndAreaIsNotNull(turno).stream()
                .filter(o -> o.getArea().isAtiva()).toList();
        if (candidatos.isEmpty()) {
            candidatos = operadores.findByAtivoTrueAndAreaIsNotNull().stream()
                    .filter(o -> o.getArea().isAtiva()).toList();
        }
        if (candidatos.isEmpty()) throw new NegocioException("Nenhum operador ativo com área para simular.");
        return candidatos.get(random.nextInt(candidatos.size()));
    }

    static String sigla(String nome) {
        String limpo = java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT).replaceAll("[^A-Z ]", "");
        String[] partes = limpo.trim().split("\\s+");
        return partes[0].substring(0, Math.min(3, partes[0].length()));
    }
}
