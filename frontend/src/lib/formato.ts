import type { NivelRisco, StatusOcorrencia, TipoAcao, TipoEPI, TipoEventoAuditoria } from '../api/tipos';

/**
 * A planta da Metaindústria opera no horário de Brasília: a API devolve datas sem fuso nesse horário
 * e a interface mostra tudo nele, qualquer que seja o fuso do computador de quem acessa.
 */
export const FUSO_PLANTA = 'America/Sao_Paulo';
const OFFSET_PLANTA = '-03:00';

const dataHora = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit', timeZone: FUSO_PLANTA });
const dataCurta = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', timeZone: FUSO_PLANTA });
const dataCompleta = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', timeZone: FUSO_PLANTA });
const hora = new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: FUSO_PLANTA });
const isoDia = new Intl.DateTimeFormat('en-CA', { year: 'numeric', month: '2-digit', day: '2-digit', timeZone: FUSO_PLANTA });

/** Converte uma data da API (LocalDate ou LocalDateTime no horário da planta) em Date. */
export const paraData = (iso: string) => {
  if (iso.length === 10) return new Date(`${iso}T12:00:00${OFFSET_PLANTA}`);
  const temFuso = /([zZ]|[+-]\d{2}:\d{2})$/.test(iso);
  return new Date(temFuso ? iso : `${iso.slice(0, 23)}${OFFSET_PLANTA}`);
};

export const fmtDataHora = (iso: string | null | undefined) => (iso ? dataHora.format(paraData(iso)) : '—');
export const fmtData = (iso: string | null | undefined) => (iso ? dataCompleta.format(paraData(iso)) : '—');
export const fmtDiaMes = (iso: string) => dataCurta.format(paraData(iso));
export const fmtHora = (iso: string | null | undefined) => (iso ? hora.format(paraData(iso)) : '—');

export function fmtRelativo(iso: string | null | undefined) {
  if (!iso) return '—';
  const diff = Date.now() - paraData(iso).getTime();
  const min = Math.round(diff / 60000);
  if (min < 1) return 'agora';
  if (min < 60) return `há ${min} min`;
  const h = Math.round(min / 60);
  if (h < 24) return `há ${h} h`;
  const d = Math.round(h / 24);
  return d === 1 ? 'ontem' : `há ${d} dias`;
}

export const fmtPct = (v: number | null | undefined) =>
  v === null || v === undefined ? '—' : `${v.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;

export const fmtMin = (v: number | null | undefined) =>
  v === null || v === undefined ? '—' : `${v.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} min`;

/** Data de hoje (AAAA-MM-DD) no horário da planta. */
export const hojeISO = () => isoDia.format(new Date());

export const diasAtrasISO = (dias: number) => isoDia.format(new Date(Date.now() - dias * 86_400_000));

export const ROTULO_RISCO: Record<NivelRisco, string> = {
  BAIXO: 'Baixo',
  MEDIO: 'Médio',
  ALTO: 'Alto',
  CRITICO: 'Crítico',
};

export const ROTULO_STATUS: Record<StatusOcorrencia, string> = {
  ABERTA: 'Aguardando regularização',
  ESCALADA: 'Escalada',
  AGUARDANDO_VALIDACAO: 'Em validação',
  ENCERRADA: 'Regularizada',
};

export const ROTULO_EPI: Record<TipoEPI, string> = {
  CAPACETE: 'Capacete',
  OCULOS: 'Óculos de proteção',
  LUVAS: 'Luvas',
  PROTETOR_AURICULAR: 'Protetor auricular',
  BOTA: 'Bota de segurança',
  COLETE: 'Colete refletivo',
  MASCARA_SOLDA: 'Máscara de solda',
  RESPIRADOR: 'Respirador',
  AVENTAL: 'Avental',
  CINTO: 'Cinto paraquedista',
};

export const ROTULO_ACAO: Record<TipoAcao, string> = {
  ABERTURA: 'Não conformidade identificada',
  ALERTA_EMITIDO: 'Alerta emitido',
  REGULARIZACAO: 'Regularização registrada',
  VALIDACAO_APROVADA: 'Regularização validada',
  VALIDACAO_REJEITADA: 'Regularização não validada',
  ESCALONAMENTO: 'Ocorrência escalada',
  COMENTARIO: 'Comentário',
  ENCERRAMENTO: 'Ocorrência encerrada',
};

export const ROTULO_EVENTO: Record<TipoEventoAuditoria, string> = {
  OCORRENCIA_CRIADA: 'Ocorrência criada',
  ALERTA_EMITIDO: 'Alerta emitido',
  REGULARIZACAO_REGISTRADA: 'Regularização registrada',
  OCORRENCIA_ESCALADA: 'Ocorrência escalada',
  VALIDACAO_APROVADA: 'Validação aprovada',
  VALIDACAO_REJEITADA: 'Validação rejeitada',
  OCORRENCIA_ENCERRADA: 'Ocorrência encerrada',
  COMENTARIO: 'Comentário',
  AREA_CRIADA: 'Área criada',
  AREA_ATUALIZADA: 'Área atualizada',
  REGRA_CRIADA: 'Regra criada',
  REGRA_REMOVIDA: 'Regra removida',
  EPI_CADASTRADO: 'EPI cadastrado',
  EPI_ENTREGUE: 'EPI entregue',
  USUARIO_CRIADO: 'Usuário criado',
};

export const ROTULO_PERFIL: Record<string, string> = {
  OPERADOR: 'Operador',
  SUPERVISOR: 'Supervisor',
  GESTOR: 'Gestor',
  SISTEMA: 'Sistema',
};

export const ROTULO_TURNO: Record<string, string> = {
  A: 'Turno A · 06h–14h',
  B: 'Turno B · 14h–22h',
  C: 'Turno C · 22h–06h',
};

export const TIPOS_EPI = Object.keys(ROTULO_EPI) as TipoEPI[];
export const NIVEIS: NivelRisco[] = ['BAIXO', 'MEDIO', 'ALTO', 'CRITICO'];
