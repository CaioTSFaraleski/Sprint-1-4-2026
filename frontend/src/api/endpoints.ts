import { baixar, requisicao } from './cliente';
import type {
  Alerta, Area, ColaboradorDetalhe, ColaboradorResumo, Dashboard, Deteccao, Entrega, Epi, EventoAuditoria,
  LoginResponse, NivelRisco, OcorrenciaDetalhe, OcorrenciaResumo, Perfil, Relatorio, Simulador, TipoEPI,
  TipoEventoAuditoria, Turno, Usuario,
} from './tipos';

function qs(params: object) {
  const p = new URLSearchParams();
  Object.entries(params as Record<string, unknown>).forEach(([k, v]) => {
    if (v !== null && v !== undefined && v !== '') p.set(k, String(v));
  });
  const s = p.toString();
  return s ? `?${s}` : '';
}

export const api = {
  login: (email: string, senha: string) => requisicao<LoginResponse>('POST', '/api/v1/auth/login', { email, senha }),
  eu: () => requisicao<Usuario>('GET', '/api/v1/auth/me'),

  dashboard: () => requisicao<Dashboard>('GET', '/api/v1/dashboard'),

  ocorrencias: (f: { categoria?: string; areaId?: number | null; operadorId?: number | null; severidade?: NivelRisco | ''; busca?: string; dias?: number | null }) =>
    requisicao<OcorrenciaResumo[]>('GET', `/api/v1/ocorrencias${qs(f)}`),
  ocorrencia: (id: number) => requisicao<OcorrenciaDetalhe>('GET', `/api/v1/ocorrencias/${id}`),
  regularizar: (id: number, observacao?: string) =>
    requisicao<OcorrenciaDetalhe>('POST', `/api/v1/ocorrencias/${id}/regularizacao`, { observacao }),
  validar: (id: number, aprovada: boolean, observacao?: string) =>
    requisicao<OcorrenciaDetalhe>('POST', `/api/v1/ocorrencias/${id}/validacao`, { aprovada, observacao }),
  escalar: (id: number, observacao?: string) =>
    requisicao<OcorrenciaDetalhe>('POST', `/api/v1/ocorrencias/${id}/escalonamento`, { observacao }),
  comentar: (id: number, observacao: string) =>
    requisicao<OcorrenciaDetalhe>('POST', `/api/v1/ocorrencias/${id}/comentarios`, { observacao }),

  alertas: (incluirLidos = false) => requisicao<Alerta[]>('GET', `/api/v1/alertas${qs({ incluirLidos })}`),
  marcarAlertaLido: (id: number) => requisicao<Alerta>('PATCH', `/api/v1/alertas/${id}/lido`),

  colaboradores: (f: { busca?: string; areaId?: number | null; situacao?: string }) =>
    requisicao<ColaboradorResumo[]>('GET', `/api/v1/colaboradores${qs(f)}`),
  colaborador: (id: number) => requisicao<ColaboradorDetalhe>('GET', `/api/v1/colaboradores/${id}`),
  entregarEpi: (operadorId: number, epiId: number) =>
    requisicao<Entrega>('POST', `/api/v1/colaboradores/${operadorId}/entregas`, { epiId }),

  epis: () => requisicao<Epi[]>('GET', '/api/v1/epis'),
  criarEpi: (dados: {
    nome: string; tipo: TipoEPI; codigoCA: string; validadeCA: string; observacoes?: string;
    operadorId?: number | null; areaObrigatoriaId?: number | null; severidadeObrigatoria?: NivelRisco | null;
  }) => requisicao<Epi>('POST', '/api/v1/epis', dados),

  areas: () => requisicao<Area[]>('GET', '/api/v1/areas'),
  area: (id: number) => requisicao<Area>('GET', `/api/v1/areas/${id}`),
  criarArea: (dados: AreaForm) => requisicao<Area>('POST', '/api/v1/areas', dados),
  atualizarArea: (id: number, dados: AreaForm) => requisicao<Area>('PUT', `/api/v1/areas/${id}`, dados),
  adicionarRegra: (areaId: number, dados: { epiId: number; descricao: string; severidadePadrao: NivelRisco; turno: Turno | null; atividade?: string }) =>
    requisicao<Area>('POST', `/api/v1/areas/${areaId}/regras`, dados),
  removerRegra: (areaId: number, regraId: number) => requisicao<Area>('DELETE', `/api/v1/areas/${areaId}/regras/${regraId}`),

  auditoria: (f: { inicio?: string; fim?: string; areaId?: number | null; tipo?: TipoEventoAuditoria | ''; autor?: string; ocorrenciaId?: number | null }) =>
    requisicao<EventoAuditoria[]>('GET', `/api/v1/auditoria${qs(f)}`),

  relatorio: (f: FiltroRelatorio) => requisicao<Relatorio>('GET', `/api/v1/relatorios/conformidade${qs(f)}`),
  baixarRelatorioCsv: (f: FiltroRelatorio) => baixar(`/api/v1/relatorios/conformidade.csv${qs(f)}`, 'relatorio-conformidade.csv'),

  usuarios: (perfil?: Perfil) => requisicao<Usuario[]>('GET', `/api/v1/usuarios${qs({ perfil })}`),
  criarUsuario: (dados: {
    nome: string; matricula: string; email: string; senha: string; perfil: Perfil; turno?: Turno | null;
    funcao?: string; areaId?: number | null; unidadeIndustrial?: string;
  }) => requisicao<Usuario>('POST', '/api/v1/usuarios', dados),

  simulador: () => requisicao<Simulador>('GET', '/api/v1/simulador'),
  alterarSimulador: (ativo: boolean) => requisicao<Simulador>('PUT', '/api/v1/simulador', { ativo }),
  simularDeteccao: (naoConforme: boolean | null) =>
    requisicao<Deteccao>('POST', '/api/v1/simulador/deteccoes', { naoConforme }),
};

export interface AreaForm {
  nome: string;
  setor: string;
  nivelRisco: NivelRisco;
  turnos: Turno[];
  descricao?: string;
  supervisorId?: number | null;
  ativa?: boolean;
}

export interface FiltroRelatorio {
  inicio?: string;
  fim?: string;
  areaId?: number | null;
  setor?: string;
  tipoEpi?: TipoEPI | '';
  severidade?: NivelRisco | '';
}
