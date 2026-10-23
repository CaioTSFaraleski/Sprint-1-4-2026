import { describe, expect, it } from 'vitest';
import { fmtData, fmtDataHora, fmtMin, fmtPct, paraData, ROTULO_STATUS } from './formato';

describe('formatação', () => {
  it('formata percentuais e minutos em pt-BR', () => {
    expect(fmtPct(91.5)).toBe('91,5%');
    expect(fmtPct(null)).toBe('—');
    expect(fmtMin(25.25)).toBe('25,3 min');
  });

  it('interpreta datas da API no horário da planta (Brasília), em qualquer fuso do navegador', () => {
    expect(paraData('2026-10-23T08:30:00').toISOString()).toBe('2026-10-23T11:30:00.000Z');
    expect(paraData('2026-10-23T08:30:00.123456789').toISOString()).toBe('2026-10-23T11:30:00.123Z');
    expect(fmtDataHora('2026-10-23T08:30:00')).toBe('23/10, 08:30');
    expect(fmtData('2026-10-23')).toBe('23/10/2026');
  });

  it('tem rótulo para todos os status da ocorrência', () => {
    expect(Object.keys(ROTULO_STATUS)).toEqual(['ABERTA', 'ESCALADA', 'AGUARDANDO_VALIDACAO', 'ENCERRADA']);
  });
});
