/** Tipos espelhando os DTOs da API (backend/src/main/java/br/com/safefield/api/dto/Dtos.java). */

export type Perfil = 'OPERADOR' | 'SUPERVISOR' | 'GESTOR';
export type Turno = 'A' | 'B' | 'C';
export type NivelRisco = 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO';
export type StatusOcorrencia = 'ABERTA' | 'ESCALADA' | 'AGUARDANDO_VALIDACAO' | 'ENCERRADA';
export type TipoEPI =
  | 'CAPACETE' | 'OCULOS' | 'LUVAS' | 'PROTETOR_AURICULAR' | 'BOTA'
  | 'COLETE' | 'MASCARA_SOLDA' | 'RESPIRADOR' | 'AVENTAL' | 'CINTO';
export type TipoAcao =
  | 'ABERTURA' | 'ALERTA_EMITIDO' | 'REGULARIZACAO' | 'VALIDACAO_APROVADA' | 'VALIDACAO_REJEITADA'
  | 'ESCALONAMENTO' | 'COMENTARIO' | 'ENCERRAMENTO';
export type TipoEventoAuditoria =
  | 'OCORRENCIA_CRIADA' | 'ALERTA_EMITIDO' | 'REGULARIZACAO_REGISTRADA' | 'OCORRENCIA_ESCALADA'
  | 'VALIDACAO_APROVADA' | 'VALIDACAO_REJEITADA' | 'OCORRENCIA_ENCERRADA' | 'COMENTARIO'
  | 'AREA_CRIADA' | 'AREA_ATUALIZADA' | 'REGRA_CRIADA' | 'REGRA_REMOVIDA' | 'EPI_CADASTRADO'
  | 'EPI_ENTREGUE' | 'USUARIO_CRIADO';
export type OrigemDeteccao = 'CAMERA' | 'SENSOR' | 'GATEWAY' | 'SIMULADOR';
export type StatusEntrega = 'ATIVA' | 'VENCIDA' | 'DEVOLVIDA';
export type StatusAlerta = 'ENVIADO' | 'LIDO' | 'RESOLVIDO';

export interface Usuario {
  id: number;
  nome: string;
  matricula: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
  turno: Turno | null;
  funcao: string | null;
  areaId: number | null;
  areaNome: string | null;
  unidadeIndustrial: string | null;
}

export interface LoginResponse {
  token: string;
  expiraEm: string;
  usuario: Usuario;
}

export interface Regra {
  id: number;
  epiId: number;
  epiNome: string;
  tipoEpi: TipoEPI;
  tipoEpiDescricao: string;
  descricao: string;
  obrigatoria: boolean;
  severidadePadrao: NivelRisco;
  turno: Turno | null;
  atividade: string | null;
}

export interface Area {
  id: number;
  nome: string;
  setor: string;
  nivelRisco: NivelRisco;
  turnos: Turno[];
  descricao: string | null;
  ativa: boolean;
  supervisorId: number | null;
  supervisorNome: string | null;
  regras: Regra[];
  totalOperadores: number;
  ocorrenciasAtivas: number;
}

export interface Epi {
  id: number;
  nome: string;
  tipo: TipoEPI;
  tipoDescricao: string;
  codigoCA: string;
  validadeCA: string;
  certificadoValido: boolean;
  observacoes: string | null;
  ativo: boolean;
}

export interface Entrega {
  id: number;
  epiId: number;
  epiNome: string;
  tipoEpi: TipoEPI;
  codigoCA: string;
  dataEntrega: string;
  dataValidade: string;
  status: StatusEntrega;
}

export interface Deteccao {
  id: number;
  dataHora: string;
  operadorId: number;
  operadorNome: string;
  areaId: number;
  areaNome: string;
  episDetectados: TipoEPI[];
  confianca: number;
  evidenciaUrl: string | null;
  origem: OrigemDeteccao;
  dispositivo: string | null;
  conforme: boolean;
  ocorrenciaId: number | null;
  ocorrenciaCodigo: string | null;
}

export interface EpiAusente {
  epiId: number;
  nome: string;
  tipo: TipoEPI;
  tipoDescricao: string;
}

export interface OcorrenciaResumo {
  id: number;
  codigo: string;
  dataAbertura: string;
  operadorId: number;
  operadorNome: string;
  operadorMatricula: string;
  areaId: number;
  areaNome: string;
  setor: string;
  episAusentes: EpiAusente[];
  severidade: NivelRisco;
  status: StatusOcorrencia;
  origem: OrigemDeteccao;
  dispositivo: string | null;
  reincidente: boolean;
  dataRegularizacao: string | null;
  dataEncerramento: string | null;
}

export interface Acao {
  id: number;
  dataHora: string;
  tipo: TipoAcao;
  autorNome: string;
  autorPerfil: string;
  observacao: string | null;
}

export interface Alerta {
  id: number;
  ocorrenciaId: number;
  ocorrenciaCodigo: string;
  destinatarioNome: string;
  canal: 'PAINEL' | 'PWA_PUSH';
  mensagem: string;
  dataEnvio: string;
  status: StatusAlerta;
  severidade: NivelRisco;
  areaNome: string;
  statusOcorrencia: StatusOcorrencia;
}

export interface OcorrenciaDetalhe {
  resumo: OcorrenciaResumo;
  descricao: string | null;
  supervisorNome: string | null;
  deteccao: Deteccao;
  acoes: Acao[];
  orientacoes: Regra[];
  alertas: Alerta[];
}

export interface EpiObrigatorio {
  tipo: TipoEPI;
  tipoDescricao: string;
  epiNome: string;
  severidade: NivelRisco;
  detectado: boolean;
  entregue: boolean;
}

export type SituacaoColaborador = 'PENDENTE' | 'EM_VALIDACAO' | 'CONFORME' | 'SEM_LEITURA';

export interface ColaboradorResumo {
  id: number;
  nome: string;
  matricula: string;
  funcao: string | null;
  turno: Turno | null;
  areaId: number | null;
  areaNome: string | null;
  setor: string | null;
  situacao: SituacaoColaborador;
  episPendentes: number;
  ocorrenciasAtivas: number;
  ultimaDeteccao: string | null;
}

export interface ColaboradorDetalhe {
  resumo: ColaboradorResumo;
  email: string;
  episObrigatorios: EpiObrigatorio[];
  entregas: Entrega[];
  ultimaDeteccao: Deteccao | null;
  ocorrencias: OcorrenciaResumo[];
  totalOcorrencias: number;
  reincidente: boolean;
}

export interface EventoAuditoria {
  id: number;
  dataHora: string;
  tipo: TipoEventoAuditoria;
  autorNome: string;
  autorPerfil: string;
  areaId: number | null;
  areaNome: string | null;
  ocorrenciaId: number | null;
  ocorrenciaCodigo: string | null;
  severidade: NivelRisco | null;
  descricao: string;
}

export interface SerieDia {
  data: string;
  deteccoes: number;
  conformes: number;
  taxaConformidade: number;
  ocorrencias: number;
  criticas: number;
}

export interface AreaRisco {
  id: number;
  nome: string;
  setor: string;
  nivelRisco: NivelRisco;
  ocorrenciasAtivas: number;
  ocorrencias7d: number;
  taxaConformidade7d: number;
}

export interface Dashboard {
  alertasAtivos: number;
  criticasAtivas: number;
  aguardandoValidacao: number;
  escaladas: number;
  deteccoesHoje: number;
  taxaConformidadeHoje: number;
  taxaConformidade7d: number;
  variacaoConformidade: number;
  tempoMedioRegularizacaoMin: number | null;
  taxaReincidencia: number;
  serie7d: SerieDia[];
  areas: AreaRisco[];
  ocorrenciasAtivas: OcorrenciaResumo[];
  ultimasDeteccoes: Deteccao[];
  simuladorAtivo: boolean;
}

export interface LinhaArea {
  areaId: number;
  area: string;
  setor: string;
  deteccoes: number;
  taxaConformidade: number;
  ocorrencias: number;
  criticas: number;
  tempoMedioRegularizacaoMin: number | null;
}

export interface Relatorio {
  inicio: string;
  fim: string;
  areaId: number | null;
  setor: string | null;
  tipoEpi: TipoEPI | null;
  severidade: NivelRisco | null;
  totalDeteccoes: number;
  deteccoesConformes: number;
  indiceConformidade: number;
  totalOcorrencias: number;
  ocorrenciasCriticas: number;
  tempoMedioRegularizacaoMin: number | null;
  taxaReincidencia: number;
  serieDiaria: SerieDia[];
  porArea: LinhaArea[];
  porEpi: { tipo: TipoEPI; descricao: string; ocorrencias: number; percentual: number }[];
  porTurno: { turno: Turno; deteccoes: number; taxaConformidade: number; ocorrencias: number }[];
  reincidentes: { operadorId: number; nome: string; matricula: string; area: string; ocorrencias: number }[];
  geradoEm: string;
}

export interface Simulador {
  ativo: boolean;
  intervaloMs: number;
}

export interface EventoTempoReal {
  tipo: 'OCORRENCIA_ABERTA' | 'OCORRENCIA_ATUALIZADA' | 'DETECCAO_CONFORME';
  ocorrencia: OcorrenciaResumo | null;
  deteccao: Deteccao | null;
  mensagem: string;
}

export interface Erro {
  status: number;
  erro: string;
  mensagem: string;
  detalhes: string[];
}
