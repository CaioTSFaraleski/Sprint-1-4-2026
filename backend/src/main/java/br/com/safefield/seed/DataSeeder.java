package br.com.safefield.seed;

import br.com.safefield.config.SafeFieldProperties;
import br.com.safefield.domain.*;
import br.com.safefield.repository.*;
import br.com.safefield.service.AuditoriaService;
import br.com.safefield.service.MotorRegrasService;
import br.com.safefield.service.OcorrenciaService;
import br.com.safefield.service.PerfilEsquecimento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Popula o banco com o contexto simulado da Metaindústria: usuários (incluindo as personas da
 * Sprint 1), áreas, catálogo de EPIs com CA, regras por área/turno, fichas de entrega e 30 dias de
 * histórico de detecções, ocorrências, alertas e auditoria. Os dados são gerados com semente fixa,
 * relativos à data atual, para que o painel sempre pareça "vivo" na hora da avaliação.
 *
 * O histórico passa pelos mesmos serviços usados em produção (motor de regras e ciclo da ocorrência),
 * então os números do dashboard e dos relatórios são coerentes com as regras cadastradas.
 */
@Component
@Order(1)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    public static final String SENHA_PADRAO = "safefield123";

    private final SafeFieldProperties props;
    private final UsuarioRepository usuarios;
    private final AreaIndustrialRepository areas;
    private final EPIRepository epis;
    private final RegraSegurancaRepository regras;
    private final EntregaEPIRepository entregas;
    private final DeteccaoEPIRepository deteccoes;
    private final MotorRegrasService motor;
    private final OcorrenciaService ocorrencias;
    private final AuditoriaService auditoria;
    private final PasswordEncoder encoder;
    private final TransactionTemplate tx;
    private final Clock clock;

    private final Random random = new Random(2026);
    /** Última vez que faltou cada tipo de EPI para cada operador (critério de reincidência). */
    private final Map<Long, Map<TipoEPI, LocalDateTime>> ultimaFalta = new HashMap<>();

    public DataSeeder(SafeFieldProperties props, UsuarioRepository usuarios, AreaIndustrialRepository areas,
                      EPIRepository epis, RegraSegurancaRepository regras, EntregaEPIRepository entregas,
                      DeteccaoEPIRepository deteccoes, MotorRegrasService motor, OcorrenciaService ocorrencias,
                      AuditoriaService auditoria, PasswordEncoder encoder, TransactionTemplate tx, Clock clock) {
        this.props = props;
        this.usuarios = usuarios;
        this.areas = areas;
        this.epis = epis;
        this.regras = regras;
        this.entregas = entregas;
        this.deteccoes = deteccoes;
        this.motor = motor;
        this.ocorrencias = ocorrencias;
        this.auditoria = auditoria;
        this.encoder = encoder;
        this.tx = tx;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!props.seed().ativo() || usuarios.count() > 0) return;
        long inicio = System.currentTimeMillis();
        tx.executeWithoutResult(status -> popular());
        log.info("Dados de demonstração da Metaindústria criados em {} ms", System.currentTimeMillis() - inicio);
    }

    /* ------------------------------------------------------------------ */

    private record Contexto(LocalDateTime agora, GestorIndustrial gestor, List<SupervisorSeguranca> supervisores,
                            Map<String, AreaIndustrial> areas, Map<TipoEPI, EPI> catalogo,
                            Map<Turno, List<Operador>> operadoresPorTurno, Map<String, Operador> personas) {}

    private void popular() {
        LocalDateTime agora = LocalDateTime.now(clock).withSecond(0).withNano(0);
        LocalDateTime inicioHistorico = agora.minusDays(props.seed().diasHistorico());
        String hash = encoder.encode(SENHA_PADRAO);

        // ---------- usuários de gestão (personas da Sprint 1) ----------
        GestorIndustrial gestor = usuarios.save(new GestorIndustrial("Marcelo Torres", "GES-0001",
                "gestor@metaindustria.com.br", hash, "Planta Metaindústria - Unidade Sorocaba"));
        SupervisorSeguranca renata = usuarios.save(new SupervisorSeguranca("Renata Oliveira", "SUP-0101",
                "supervisor@metaindustria.com.br", hash));
        SupervisorSeguranca carlos = usuarios.save(new SupervisorSeguranca("Carlos Mendes", "SUP-0102",
                "carlos.mendes@metaindustria.com.br", hash));

        // ---------- catálogo de EPIs (CA fictícios) ----------
        LocalDate hoje = agora.toLocalDate();
        Map<TipoEPI, EPI> catalogo = new EnumMap<>(TipoEPI.class);
        catalogo.put(TipoEPI.CAPACETE, epis.save(new EPI("Capacete de segurança classe B", TipoEPI.CAPACETE, "31469",
                hoje.plusYears(2), "Com jugular; obrigatório em todas as áreas produtivas.")));
        catalogo.put(TipoEPI.OCULOS, epis.save(new EPI("Óculos de proteção incolor antiembaçante", TipoEPI.OCULOS,
                "10346", hoje.plusMonths(14), null)));
        catalogo.put(TipoEPI.LUVAS, epis.save(new EPI("Luva de vaqueta", TipoEPI.LUVAS, "15532",
                hoje.plusMonths(9), "Para manuseio de peças metálicas.")));
        catalogo.put(TipoEPI.PROTETOR_AURICULAR, epis.save(new EPI("Protetor auricular tipo concha",
                TipoEPI.PROTETOR_AURICULAR, "29176", hoje.plusMonths(20), "Atenuação de 21 dB.")));
        catalogo.put(TipoEPI.BOTA, epis.save(new EPI("Botina de segurança com biqueira de composite", TipoEPI.BOTA,
                "42157", hoje.plusYears(3), null)));
        catalogo.put(TipoEPI.COLETE, epis.save(new EPI("Colete refletivo classe 2", TipoEPI.COLETE, "38810",
                hoje.plusYears(2), "Obrigatório em áreas com circulação de empilhadeiras.")));
        catalogo.put(TipoEPI.MASCARA_SOLDA, epis.save(new EPI("Máscara de solda com escurecimento automático",
                TipoEPI.MASCARA_SOLDA, "25814", hoje.plusMonths(16), null)));
        catalogo.put(TipoEPI.RESPIRADOR, epis.save(new EPI("Respirador semifacial com filtro para vapores orgânicos",
                TipoEPI.RESPIRADOR, "11240", hoje.plusMonths(7), null)));
        catalogo.put(TipoEPI.AVENTAL, epis.save(new EPI("Avental de raspa de couro", TipoEPI.AVENTAL, "36041",
                hoje.plusMonths(11), null)));
        // CA vencido de propósito: aparece em vermelho no catálogo e bloqueia novas entregas
        epis.save(new EPI("Luva nitrílica (lote antigo)", TipoEPI.LUVAS, "18765", hoje.minusDays(12),
                "CA vencido: substituir pelo modelo 15532."));

        // ---------- áreas e regras ----------
        Map<String, AreaIndustrial> mapaAreas = new LinkedHashMap<>();
        mapaAreas.put("SOL", area("Soldagem", "Produção", NivelRisco.CRITICO, "A,B,C",
                "Células de solda MIG/TIG da linha de estruturas metálicas.", renata));
        mapaAreas.put("USI", area("Usinagem CNC", "Produção", NivelRisco.ALTO, "A,B,C",
                "Centros de usinagem e tornos CNC.", renata));
        mapaAreas.put("CAL", area("Caldeiraria", "Produção", NivelRisco.ALTO, "A,B",
                "Corte, dobra e calandragem de chapas.", renata));
        mapaAreas.put("PIN", area("Pintura Industrial", "Acabamento", NivelRisco.ALTO, "A,B",
                "Cabines de pintura eletrostática e líquida.", carlos));
        mapaAreas.put("MON", area("Montagem Final", "Produção", NivelRisco.MEDIO, "A,B,C",
                "Linha de montagem final e testes funcionais.", carlos));
        mapaAreas.put("ALM", area("Almoxarifado", "Logística", NivelRisco.BAIXO, "A,B",
                "Recebimento, armazenagem e expedição com empilhadeiras.", carlos));

        AreaIndustrial sol = mapaAreas.get("SOL"), usi = mapaAreas.get("USI"), cal = mapaAreas.get("CAL"),
                pin = mapaAreas.get("PIN"), mon = mapaAreas.get("MON"), alm = mapaAreas.get("ALM");
        regra(sol, catalogo.get(TipoEPI.CAPACETE), "Capacete obrigatório em toda a área de solda", NivelRisco.ALTO, null, null);
        regra(sol, catalogo.get(TipoEPI.MASCARA_SOLDA), "Máscara de solda durante qualquer operação de solda", NivelRisco.CRITICO, null, "Solda MIG/TIG");
        regra(sol, catalogo.get(TipoEPI.LUVAS), "Luvas de vaqueta no manuseio de peças quentes", NivelRisco.ALTO, null, null);
        regra(sol, catalogo.get(TipoEPI.AVENTAL), "Avental de raspa contra respingos", NivelRisco.MEDIO, null, null);
        regra(sol, catalogo.get(TipoEPI.BOTA), "Botina de segurança", NivelRisco.MEDIO, null, null);
        regra(sol, catalogo.get(TipoEPI.OCULOS), "Óculos sob a máscara no esmerilhamento", NivelRisco.ALTO, null, "Esmerilhamento");
        regra(sol, catalogo.get(TipoEPI.PROTETOR_AURICULAR), "Protetor auricular (ruído acima de 85 dB)", NivelRisco.ALTO, null, null);

        regra(usi, catalogo.get(TipoEPI.OCULOS), "Óculos de proteção contra cavacos", NivelRisco.ALTO, null, null);
        regra(usi, catalogo.get(TipoEPI.PROTETOR_AURICULAR), "Protetor auricular junto aos centros de usinagem", NivelRisco.ALTO, null, null);
        regra(usi, catalogo.get(TipoEPI.BOTA), "Botina de segurança", NivelRisco.MEDIO, null, null);
        regra(usi, catalogo.get(TipoEPI.CAPACETE), "Capacete na troca de ferramentas com ponte rolante", NivelRisco.MEDIO, Turno.A, "Troca de ferramenta");

        regra(cal, catalogo.get(TipoEPI.CAPACETE), "Capacete obrigatório (movimentação de chapas)", NivelRisco.ALTO, null, null);
        regra(cal, catalogo.get(TipoEPI.LUVAS), "Luvas de vaqueta contra cortes", NivelRisco.ALTO, null, null);
        regra(cal, catalogo.get(TipoEPI.OCULOS), "Óculos de proteção", NivelRisco.MEDIO, null, null);
        regra(cal, catalogo.get(TipoEPI.PROTETOR_AURICULAR), "Protetor auricular na prensa dobradeira", NivelRisco.ALTO, null, null);
        regra(cal, catalogo.get(TipoEPI.BOTA), "Botina de segurança", NivelRisco.MEDIO, null, null);

        regra(pin, catalogo.get(TipoEPI.RESPIRADOR), "Respirador com filtro para vapores orgânicos", NivelRisco.CRITICO, null, null);
        regra(pin, catalogo.get(TipoEPI.OCULOS), "Óculos de proteção", NivelRisco.MEDIO, null, null);
        regra(pin, catalogo.get(TipoEPI.LUVAS), "Luvas no preparo de tintas e solventes", NivelRisco.MEDIO, null, null);
        regra(pin, catalogo.get(TipoEPI.BOTA), "Botina de segurança", NivelRisco.BAIXO, null, null);

        regra(mon, catalogo.get(TipoEPI.LUVAS), "Luvas na montagem de componentes", NivelRisco.MEDIO, null, null);
        regra(mon, catalogo.get(TipoEPI.OCULOS), "Óculos de proteção no aperto pneumático", NivelRisco.MEDIO, null, null);
        regra(mon, catalogo.get(TipoEPI.BOTA), "Botina de segurança", NivelRisco.BAIXO, null, null);

        regra(alm, catalogo.get(TipoEPI.COLETE), "Colete refletivo (circulação de empilhadeiras)", NivelRisco.ALTO, null, null);
        regra(alm, catalogo.get(TipoEPI.BOTA), "Botina de segurança", NivelRisco.MEDIO, null, null);
        regra(alm, catalogo.get(TipoEPI.LUVAS), "Luvas na movimentação manual de cargas", NivelRisco.BAIXO, Turno.A, "Recebimento");

        // ---------- operadores ----------
        Turno turnoAtual = Turno.doHorario(agora.toLocalTime());
        Map<String, Operador> personas = new HashMap<>();
        List<Operador> todos = new ArrayList<>();
        // persona da Sprint 1 (Marcos Lima) e colaborador usado no protótipo da Sprint 2 (João Almeida):
        // ficam no turno atual para que a demonstração tenha ocorrências deles "agora"
        personas.put("marcos", operador(todos, "Marcos Lima", "OP-1001", "operador@metaindustria.com.br",
                hash, turnoAtual, "Montador", mon));
        personas.put("joao", operador(todos, "João Almeida", "OP-1002", "joao.almeida@metaindustria.com.br",
                hash, turnoAtual, "Soldador", sol));
        String[][] equipe = {
                {"Ana Paula Ribeiro", "Soldadora", "SOL"}, {"Diego Santos", "Soldador", "SOL"},
                {"Felipe Nogueira", "Soldador", "SOL"}, {"Bruno Carvalho", "Operador CNC", "USI"},
                {"Juliana Freitas", "Operadora CNC", "USI"}, {"Ricardo Prado", "Ferramenteiro", "USI"},
                {"Tatiane Moura", "Operadora CNC", "USI"}, {"Eduardo Lins", "Caldeireiro", "CAL"},
                {"Sérgio Batista", "Caldeireiro", "CAL"}, {"Vanessa Teles", "Operadora de dobradeira", "CAL"},
                {"Lucas Martins", "Pintor industrial", "PIN"}, {"Patrícia Gomes", "Pintora industrial", "PIN"},
                {"Rafael Duarte", "Preparador de tintas", "PIN"}, {"Camila Rocha", "Montadora", "MON"},
                {"Gabriel Pires", "Montador", "MON"}, {"Larissa Queiroz", "Inspetora de qualidade", "MON"},
                {"Henrique Alves", "Montador", "MON"}, {"Otávio Ramos", "Operador de empilhadeira", "ALM"},
                {"Beatriz Cunha", "Almoxarife", "ALM"}, {"Marcelo Vieira", "Conferente", "ALM"},
                {"Natália Barros", "Soldadora", "SOL"}, {"Igor Fontes", "Operador CNC", "USI"},
        };
        Turno[] ordemTurnos = {Turno.A, Turno.B, Turno.C};
        int contador = 1003;
        Map<String, Integer> porArea = new HashMap<>();
        for (String[] e : equipe) {
            AreaIndustrial area = mapaAreas.get(e[2]);
            List<Turno> turnosArea = turnosDa(area);
            int n = porArea.merge(e[2], 1, Integer::sum);
            Turno turno = turnosArea.get((n - 1) % turnosArea.size());
            if (!turnosArea.contains(turno)) turno = ordemTurnos[0];
            String login = normalizar(e[0]);
            operador(todos, e[0], "OP-" + contador++, login + "@metaindustria.com.br", hash, turno, e[1], area);
        }

        // ---------- fichas de entrega ----------
        for (Operador op : todos) {
            for (RegraSeguranca r : op.getArea().getRegras()) {
                if (entregas.findByOperadorIdOrderByDataEntregaDesc(op.getId()).stream()
                        .anyMatch(x -> x.getEpi().getTipo() == r.getEpi().getTipo())) continue;
                LocalDate entrega = hoje.minusDays(20 + random.nextInt(170));
                LocalDate validade = entrega.plusMonths(6);
                if (validade.isAfter(r.getEpi().getValidadeCA())) validade = r.getEpi().getValidadeCA();
                entregas.save(new EntregaEPI(op, r.getEpi(), entrega, validade));
            }
        }

        // ---------- trilha de auditoria da configuração inicial ----------
        LocalDateTime implantacao = inicioHistorico.minusDays(2).withHour(9).withMinute(0);
        for (AreaIndustrial a : mapaAreas.values()) {
            auditoria.registrar(implantacao, TipoEventoAuditoria.AREA_CRIADA, gestor, a, null, a.getNivelRisco(),
                    "Área " + a.getNome() + " cadastrada na implantação do SafeField AI.");
            implantacao = implantacao.plusMinutes(7);
            auditoria.registrar(implantacao, TipoEventoAuditoria.REGRA_CRIADA, a.getSupervisor(), a, null, null,
                    a.getRegras().size() + " regras de segurança configuradas para " + a.getNome() + ".");
            implantacao = implantacao.plusMinutes(5);
        }

        Map<Turno, List<Operador>> porTurno = new EnumMap<>(Turno.class);
        for (Operador op : todos) porTurno.computeIfAbsent(op.getTurno(), t -> new ArrayList<>()).add(op);

        Contexto ctx = new Contexto(agora, gestor, List.of(renata, carlos), mapaAreas, catalogo, porTurno, personas);
        gerarHistorico(ctx, inicioHistorico);
        gerarCenariosDemonstracao(ctx);
    }

    /* ------------------------------------------------------------------ */
    /* Histórico de detecções e ocorrências                                */
    /* ------------------------------------------------------------------ */

    private void gerarHistorico(Contexto ctx, LocalDateTime inicio) {
        LocalDateTime limite = ctx.agora().minusMinutes(50);
        int dias = props.seed().diasHistorico();
        for (int d = dias; d >= 0; d--) {
            LocalDate dia = ctx.agora().toLocalDate().minusDays(d);
            boolean fimDeSemana = dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY;
            int quantidade = fimDeSemana ? 12 + random.nextInt(8) : 34 + random.nextInt(14);
            // a conformidade melhora ao longo do período: efeito esperado da orientação proativa
            double probFalha = 0.05 + 0.12 * ((double) d / dias);
            List<LocalDateTime> horarios = new ArrayList<>();
            for (int i = 0; i < quantidade; i++) horarios.add(horarioDeTrabalho(dia));
            Collections.sort(horarios);
            for (LocalDateTime quando : horarios) {
                if (quando.isAfter(limite) || quando.isBefore(inicio)) continue;
                Turno turno = Turno.doHorario(quando.toLocalTime());
                List<Operador> candidatos = ctx.operadoresPorTurno().getOrDefault(turno, List.of());
                if (candidatos.isEmpty()) continue;
                Operador op = candidatos.get(random.nextInt(candidatos.size()));
                if (!turnosDa(op.getArea()).contains(turno)) continue;
                // operadores reincidentes no histórico falham com mais frequência
                double prob = probFalha * (op.getNome().startsWith("Diego") || op.getNome().startsWith("Lucas") ? 2.2 : 1.0);
                boolean falha = random.nextDouble() < prob;
                OcorrenciaRisco o = registrarDeteccao(op, quando, falha, null, false);
                if (o != null) resolverHistorico(o, ctx.agora());
            }
        }
    }

    /** Leva a ocorrência histórica até um estado plausível para a idade dela. */
    private void resolverHistorico(OcorrenciaRisco o, LocalDateTime agora) {
        LocalDateTime abertura = o.getDataAbertura();
        long idadeMin = Duration.between(abertura, agora).toMinutes();
        SupervisorSeguranca sup = o.getArea().getSupervisor();
        Usuario operador = o.getOperador();

        LocalDateTime t = abertura;
        if (o.getSeveridade() == NivelRisco.CRITICO && random.nextDouble() < 0.35) {
            t = t.plusMinutes(2 + random.nextInt(8));
            if (t.isAfter(agora)) return;
            ocorrencias.escalar(o.getId(), sup, "Risco crítico na área: equipe de segurança acionada no local.", t);
        }
        // as ocorrências mais recentes ficam em andamento para o painel ter o que acompanhar
        if (idadeMin < 6 * 60 && random.nextDouble() < 0.45) return;

        t = t.plusMinutes(3 + random.nextInt(35));
        if (t.isAfter(agora.minusMinutes(1))) return;
        ocorrencias.regularizar(o.getId(), random.nextDouble() < 0.8 ? operador : sup, observacaoRegularizacao(), t);

        if (idadeMin < 6 * 60 && random.nextDouble() < 0.6) return;
        t = t.plusMinutes(5 + random.nextInt(70));
        if (t.isAfter(agora.minusMinutes(1))) return;
        if (random.nextDouble() < 0.07) {
            ocorrencias.validar(o.getId(), sup, false, "EPI colocado de forma incorreta; orientar o operador.", t);
            t = t.plusMinutes(4 + random.nextInt(15));
            if (t.isAfter(agora.minusMinutes(1))) return;
            ocorrencias.regularizar(o.getId(), operador, "Ajustado conforme orientação do supervisor.", t);
            t = t.plusMinutes(3 + random.nextInt(20));
            if (t.isAfter(agora.minusMinutes(1))) return;
        }
        ocorrencias.validar(o.getId(), sup, true, null, t);
    }

    /* ------------------------------------------------------------------ */
    /* Cenários fixos para a demonstração                                  */
    /* ------------------------------------------------------------------ */

    private void gerarCenariosDemonstracao(Contexto ctx) {
        LocalDateTime agora = ctx.agora();
        Operador joao = ctx.personas().get("joao");
        Operador marcos = ctx.personas().get("marcos");
        SupervisorSeguranca renata = ctx.supervisores().get(0);

        // aguardando validação: base para demonstrar a validação do supervisor
        Operador ana = operadorPorNome(ctx, "Ana Paula Ribeiro");
        OcorrenciaRisco anaOc = registrarDeteccao(ana, agora.minusMinutes(41), true, EnumSet.of(TipoEPI.AVENTAL), false);
        if (anaOc != null) ocorrencias.regularizar(anaOc.getId(), ana, "Avental recolocado.", agora.minusMinutes(33));

        // escalada: risco crítico em pintura sem respirador
        Operador lucas = operadorPorNome(ctx, "Lucas Martins");
        OcorrenciaRisco lucasOc = registrarDeteccao(lucas, agora.minusMinutes(27), true, EnumSet.of(TipoEPI.RESPIRADOR), true);
        if (lucasOc != null) {
            ocorrencias.escalar(lucasOc.getId(), lucas.getArea().getSupervisor(),
                    "Reincidente sem respirador na cabine de pintura; segurança acionada.", agora.minusMinutes(24));
        }

        // protótipo da Sprint 2: João Almeida sem óculos e sem protetor auricular
        registrarDeteccao(joao, agora.minusMinutes(14), true, EnumSet.of(TipoEPI.OCULOS, TipoEPI.PROTETOR_AURICULAR), false);

        // persona operador: Marcos Lima sem luvas (aparece em "Meus alertas" ao entrar como operador)
        registrarDeteccao(marcos, agora.minusMinutes(6), true, EnumSet.of(TipoEPI.LUVAS), false);

        // algumas detecções conformes recentes para o feed em tempo real
        for (int i = 9; i >= 1; i--) {
            List<Operador> candidatos = ctx.operadoresPorTurno().getOrDefault(Turno.doHorario(agora.toLocalTime()),
                    List.of());
            if (candidatos.isEmpty()) break;
            Operador op = candidatos.get(random.nextInt(candidatos.size()));
            if (op == marcos || op == joao) continue;
            registrarDeteccao(op, agora.minusMinutes(i * 5L + 1), false, null, false);
        }
        log.debug("Cenários de demonstração criados (supervisora {}).", renata.getNome());
    }

    /* ------------------------------------------------------------------ */
    /* Auxiliares                                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Registra uma detecção e, se houver EPI faltando, abre a ocorrência pelo serviço real.
     * @param remover           tipos a remover dos exigidos (null = sorteia um quando falha=true)
     * @param forcarReincidente usado em cenário fixo de demonstração
     */
    private OcorrenciaRisco registrarDeteccao(Operador op, LocalDateTime quando, boolean falha, Set<TipoEPI> remover,
                                              boolean forcarReincidente) {
        Map<TipoEPI, LocalDateTime> faltas = ultimaFalta.computeIfAbsent(op.getId(), k -> new EnumMap<>(TipoEPI.class));
        AreaIndustrial area = op.getArea();
        Turno turno = Turno.doHorario(quando.toLocalTime());
        Set<TipoEPI> exigidos = motor.tiposExigidos(area, turno);
        Set<TipoEPI> detectados = exigidos.isEmpty() ? EnumSet.noneOf(TipoEPI.class) : EnumSet.copyOf(exigidos);
        if (falha && !detectados.isEmpty()) {
            if (remover != null) {
                detectados.removeAll(remover);
            } else {
                detectados.remove(PerfilEsquecimento.sortear(detectados, random));
                if (detectados.size() > 2 && random.nextDouble() < 0.12) {
                    detectados.remove(PerfilEsquecimento.sortear(detectados, random));
                }
            }
        }
        String dispositivo = "CAM-" + sigla(area) + "-0" + (1 + random.nextInt(3));
        DeteccaoEPI d = new DeteccaoEPI(quando, op, area, detectados,
                BigDecimal.valueOf(0.82 + random.nextDouble() * 0.16).setScale(3, RoundingMode.HALF_UP),
                "evidencias/" + dispositivo.toLowerCase() + "/" + quando.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".jpg",
                random.nextDouble() < 0.85 ? OrigemDeteccao.CAMERA : OrigemDeteccao.SENSOR, dispositivo);
        MotorRegrasService.Avaliacao avaliacao = motor.avaliar(area, turno, detectados, false);
        boolean reincidente = forcarReincidente || (!avaliacao.conforme() && avaliacao.episAusentes().stream()
                .anyMatch(e -> faltas.containsKey(e.getTipo()) && faltas.get(e.getTipo()).isAfter(quando.minusDays(7))));
        if (reincidente) avaliacao = motor.avaliar(area, turno, detectados, true);
        d.setConforme(avaliacao.conforme());
        d = deteccoes.save(d);
        if (avaliacao.conforme()) return null;
        avaliacao.episAusentes().forEach(e -> faltas.put(e.getTipo(), quando));
        return ocorrencias.abrir(d, avaliacao, reincidente);
    }

    private AreaIndustrial area(String nome, String setor, NivelRisco risco, String turnos, String descricao,
                                SupervisorSeguranca supervisor) {
        return areas.save(new AreaIndustrial(nome, setor, risco, turnos, descricao, supervisor));
    }

    private void regra(AreaIndustrial area, EPI epi, String descricao, NivelRisco severidade, Turno turno,
                       String atividade) {
        area.getRegras().add(regras.save(new RegraSeguranca(area, epi, descricao, severidade, turno, atividade)));
    }

    private Operador operador(List<Operador> todos, String nome, String matricula, String email, String hash,
                              Turno turno, String funcao, AreaIndustrial area) {
        Operador o = usuarios.save(new Operador(nome, matricula, email, hash, turno, funcao, area));
        todos.add(o);
        return o;
    }

    private Operador operadorPorNome(Contexto ctx, String nome) {
        return ctx.operadoresPorTurno().values().stream().flatMap(List::stream)
                .filter(o -> o.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private LocalDateTime horarioDeTrabalho(LocalDate dia) {
        // distribuição concentrada nos turnos A e B, com menos leituras de madrugada
        double r = random.nextDouble();
        int minutoDoDia;
        if (r < 0.48) minutoDoDia = 6 * 60 + random.nextInt(8 * 60);
        else if (r < 0.88) minutoDoDia = 14 * 60 + random.nextInt(8 * 60);
        else minutoDoDia = (22 * 60 + random.nextInt(8 * 60)) % (24 * 60);
        return dia.atStartOfDay().plusMinutes(minutoDoDia);
    }

    private static List<Turno> turnosDa(AreaIndustrial area) {
        return Arrays.stream(area.getTurnos().split(",")).map(String::trim).map(Turno::valueOf).toList();
    }

    private static String sigla(AreaIndustrial area) {
        String n = normalizar(area.getNome()).toUpperCase(Locale.ROOT);
        return n.substring(0, Math.min(3, n.length()));
    }

    private static String normalizar(String nome) {
        return java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", "").trim().replaceAll("\\s+", ".");
    }

    private String observacaoRegularizacao() {
        String[] opcoes = {"EPI colocado e uso regularizado.", "Retirado só para descanso; recolocado.",
                "Troquei o EPI danificado no almoxarifado.", "Esqueci ao voltar do intervalo, já ajustei.",
                "EPI recolocado após orientação do líder."};
        return opcoes[random.nextInt(opcoes.length)];
    }
}
