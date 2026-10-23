import { Loader2, X } from 'lucide-react';
import { useEffect, type ButtonHTMLAttributes, type ReactNode } from 'react';
import type { NivelRisco, StatusOcorrencia } from '../api/tipos';
import { ROTULO_RISCO, ROTULO_STATUS } from '../lib/formato';

type Variante = 'primario' | 'secundario' | 'perigo' | 'sucesso' | 'fantasma';

const VARIANTES: Record<Variante, string> = {
  primario: 'bg-painel-900 text-white hover:bg-painel-700',
  secundario: 'bg-white text-slate-800 ring-1 ring-slate-300 hover:bg-slate-50',
  perigo: 'bg-red-600 text-white hover:bg-red-700',
  sucesso: 'bg-emerald-600 text-white hover:bg-emerald-700',
  fantasma: 'text-slate-600 hover:bg-slate-100',
};

export function Botao({
  variante = 'primario', carregando, grande, className = '', children, disabled, ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variante?: Variante; carregando?: boolean; grande?: boolean }) {
  return (
    <button
      {...props}
      disabled={disabled || carregando}
      className={`inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${
        grande ? 'min-h-[52px] px-6 text-base' : 'min-h-[42px] px-4 text-sm'
      } ${VARIANTES[variante]} ${className}`}
    >
      {carregando && <Loader2 size={16} className="animate-spin" />}
      {children}
    </button>
  );
}

const COR_RISCO: Record<NivelRisco, string> = {
  BAIXO: 'bg-emerald-100 text-emerald-800 ring-emerald-200',
  MEDIO: 'bg-amber-100 text-amber-900 ring-amber-200',
  ALTO: 'bg-orange-100 text-orange-800 ring-orange-200',
  CRITICO: 'bg-red-600 text-white ring-red-700',
};

export const PONTO_RISCO: Record<NivelRisco, string> = {
  BAIXO: 'bg-emerald-500',
  MEDIO: 'bg-amber-500',
  ALTO: 'bg-orange-500',
  CRITICO: 'bg-red-600',
};

export function BadgeRisco({ nivel, compacto }: { nivel: NivelRisco; compacto?: boolean }) {
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-bold ring-1 ${COR_RISCO[nivel]}`}>
      {!compacto && nivel !== 'CRITICO' && <span className={`h-1.5 w-1.5 rounded-full ${PONTO_RISCO[nivel]}`} />}
      {ROTULO_RISCO[nivel]}
    </span>
  );
}

const COR_STATUS: Record<StatusOcorrencia, string> = {
  ABERTA: 'bg-red-50 text-red-700 ring-red-200',
  ESCALADA: 'bg-purple-50 text-purple-700 ring-purple-200',
  AGUARDANDO_VALIDACAO: 'bg-sky-50 text-sky-700 ring-sky-200',
  ENCERRADA: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
};

export function BadgeStatus({ status }: { status: StatusOcorrencia }) {
  return (
    <span className={`inline-flex items-center whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${COR_STATUS[status]}`}>
      {ROTULO_STATUS[status]}
    </span>
  );
}

export function Chip({ ativo, onClick, children, contador }: { ativo: boolean; onClick: () => void; children: ReactNode; contador?: number }) {
  return (
    <button
      onClick={onClick}
      className={`inline-flex min-h-[38px] items-center gap-2 rounded-full px-4 text-sm font-semibold transition ${
        ativo ? 'bg-painel-900 text-white' : 'bg-white text-slate-700 ring-1 ring-slate-300 hover:bg-slate-50'
      }`}
    >
      {children}
      {contador !== undefined && (
        <span className={`rounded-full px-2 text-xs ${ativo ? 'bg-white/20' : 'bg-slate-100 text-slate-600'}`}>{contador}</span>
      )}
    </button>
  );
}

export function CabecalhoPagina({ titulo, subtitulo, acoes }: { titulo: string; subtitulo?: string; acoes?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-extrabold tracking-tight text-slate-900 md:text-3xl">{titulo}</h1>
        {subtitulo && <p className="mt-1 text-sm text-slate-500">{subtitulo}</p>}
      </div>
      {acoes && <div className="nao-imprimir flex flex-wrap gap-2">{acoes}</div>}
    </div>
  );
}

export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <div className="flex items-center justify-center gap-3 py-16 text-slate-500">
      <Loader2 className="animate-spin" size={20} /> {texto}
    </div>
  );
}

export function MensagemErro({ texto, onTentar }: { texto: string; onTentar?: () => void }) {
  return (
    <div className="cartao flex flex-col items-center gap-3 p-8 text-center">
      <p className="font-semibold text-red-700">{texto}</p>
      {onTentar && <Botao variante="secundario" onClick={onTentar}>Tentar de novo</Botao>}
    </div>
  );
}

export function Vazio({ icone, titulo, texto }: { icone: ReactNode; titulo: string; texto?: string }) {
  return (
    <div className="flex flex-col items-center gap-2 px-6 py-12 text-center">
      <div className="mb-1 rounded-2xl bg-slate-100 p-3 text-slate-500">{icone}</div>
      <p className="font-semibold text-slate-800">{titulo}</p>
      {texto && <p className="max-w-sm text-sm text-slate-500">{texto}</p>}
    </div>
  );
}

export function Modal({ aberto, titulo, onFechar, children, largura = 'max-w-lg' }: {
  aberto: boolean; titulo: string; onFechar: () => void; children: ReactNode; largura?: string;
}) {
  useEffect(() => {
    if (!aberto) return;
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && onFechar();
    window.addEventListener('keydown', esc);
    return () => window.removeEventListener('keydown', esc);
  }, [aberto, onFechar]);
  if (!aberto) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-slate-900/50 p-0 sm:items-center sm:p-4" onClick={onFechar}>
      <div
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        className={`animar-entrada max-h-[92vh] w-full overflow-y-auto rounded-t-3xl bg-white p-6 shadow-2xl sm:rounded-3xl ${largura}`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-4 flex items-start justify-between gap-4">
          <h2 className="text-lg font-bold">{titulo}</h2>
          <button onClick={onFechar} className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100" aria-label="Fechar">
            <X size={18} />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

export function Campo({ rotulo, children, ajuda, erro }: { rotulo: string; children: ReactNode; ajuda?: string; erro?: string }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-semibold text-slate-700">{rotulo}</span>
      {children}
      {ajuda && !erro && <span className="mt-1 block text-xs text-slate-500">{ajuda}</span>}
      {erro && <span className="mt-1 block text-xs font-medium text-red-600">{erro}</span>}
    </label>
  );
}

export function KPI({ titulo, valor, detalhe, icone, tom = 'neutro', onClick }: {
  titulo: string; valor: ReactNode; detalhe?: ReactNode; icone: ReactNode;
  tom?: 'neutro' | 'alerta' | 'critico' | 'ok'; onClick?: () => void;
}) {
  const tons = {
    neutro: 'bg-slate-100 text-slate-600',
    alerta: 'bg-amber-100 text-amber-700',
    critico: 'bg-red-100 text-red-700',
    ok: 'bg-emerald-100 text-emerald-700',
  };
  const Tag = onClick ? 'button' : 'div';
  return (
    <Tag onClick={onClick} className={`cartao flex flex-col gap-3 p-5 text-left ${onClick ? 'transition hover:ring-marca-500' : ''}`}>
      <div className="flex items-center justify-between gap-2">
        <span className="text-sm font-semibold text-slate-600">{titulo}</span>
        <span className={`rounded-xl p-2 ${tons[tom]}`}>{icone}</span>
      </div>
      <div className="text-3xl font-extrabold tracking-tight">{valor}</div>
      {detalhe && <div className="text-xs text-slate-500">{detalhe}</div>}
    </Tag>
  );
}
