package br.com.safefield.service;

import br.com.safefield.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class MotorRegrasServiceTest {

    private final MotorRegrasService motor = new MotorRegrasService();
    private AreaIndustrial soldagem;

    @BeforeEach
    void setUp() {
        soldagem = new AreaIndustrial("Soldagem", "Produção", NivelRisco.CRITICO, "A,B,C", null, null);
        regra(TipoEPI.CAPACETE, NivelRisco.ALTO, null);
        regra(TipoEPI.MASCARA_SOLDA, NivelRisco.CRITICO, null);
        regra(TipoEPI.LUVAS, NivelRisco.MEDIO, null);
        regra(TipoEPI.PROTETOR_AURICULAR, NivelRisco.MEDIO, Turno.B);
    }

    private void regra(TipoEPI tipo, NivelRisco severidade, Turno turno) {
        EPI epi = new EPI(tipo.getDescricao(), tipo, "12345", LocalDate.now().plusYears(1), null);
        soldagem.getRegras().add(new RegraSeguranca(soldagem, epi, "Uso de " + tipo, severidade, turno, null));
    }

    @Test
    void deteccaoComTodosOsEpisEhConforme() {
        var r = motor.avaliar(soldagem, Turno.A,
                EnumSet.of(TipoEPI.CAPACETE, TipoEPI.MASCARA_SOLDA, TipoEPI.LUVAS), false);
        assertThat(r.conforme()).isTrue();
        assertThat(r.severidade()).isNull();
    }

    @Test
    void epiAusenteGeraNaoConformidadeComSeveridadeDaRegra() {
        var r = motor.avaliar(soldagem, Turno.A, EnumSet.of(TipoEPI.CAPACETE, TipoEPI.MASCARA_SOLDA), false);
        assertThat(r.conforme()).isFalse();
        assertThat(r.episAusentes()).extracting(EPI::getTipo).containsExactly(TipoEPI.LUVAS);
        assertThat(r.severidade()).isEqualTo(NivelRisco.MEDIO);
    }

    @Test
    void regraDeOutroTurnoNaoEhCobrada() {
        var semProtetorNoTurnoA = motor.avaliar(soldagem, Turno.A,
                EnumSet.of(TipoEPI.CAPACETE, TipoEPI.MASCARA_SOLDA, TipoEPI.LUVAS), false);
        var semProtetorNoTurnoB = motor.avaliar(soldagem, Turno.B,
                EnumSet.of(TipoEPI.CAPACETE, TipoEPI.MASCARA_SOLDA, TipoEPI.LUVAS), false);
        assertThat(semProtetorNoTurnoA.conforme()).isTrue();
        assertThat(semProtetorNoTurnoB.conforme()).isFalse();
    }

    @Test
    void doisEpisAusentesSobemUmNivelDeSeveridade() {
        var r = motor.avaliar(soldagem, Turno.A, EnumSet.of(TipoEPI.MASCARA_SOLDA), false);
        // maior regra violada = ALTO (capacete); faltam 2 EPIs -> CRITICO
        assertThat(r.episAusentes()).hasSize(2);
        assertThat(r.severidade()).isEqualTo(NivelRisco.CRITICO);
    }

    @Test
    void reincidenciaSobeUmNivelDeSeveridade() {
        var r = motor.avaliar(soldagem, Turno.A, EnumSet.of(TipoEPI.CAPACETE, TipoEPI.MASCARA_SOLDA), true);
        assertThat(r.severidade()).isEqualTo(NivelRisco.ALTO);
    }

    @Test
    void turnoEhDefinidoPeloHorario() {
        assertThat(Turno.doHorario(java.time.LocalTime.of(6, 0))).isEqualTo(Turno.A);
        assertThat(Turno.doHorario(java.time.LocalTime.of(13, 59))).isEqualTo(Turno.A);
        assertThat(Turno.doHorario(java.time.LocalTime.of(14, 0))).isEqualTo(Turno.B);
        assertThat(Turno.doHorario(java.time.LocalTime.of(23, 30))).isEqualTo(Turno.C);
        assertThat(Turno.doHorario(java.time.LocalTime.of(2, 0))).isEqualTo(Turno.C);
    }
}
