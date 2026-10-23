package br.com.safefield.domain;

import java.time.LocalTime;

/** Turnos produtivos da planta: A (06h-14h), B (14h-22h) e C (22h-06h). */
public enum Turno {
    A, B, C;

    public static Turno doHorario(LocalTime hora) {
        int h = hora.getHour();
        if (h >= 6 && h < 14) return A;
        if (h >= 14 && h < 22) return B;
        return C;
    }
}
