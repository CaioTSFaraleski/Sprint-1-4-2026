import type { Deteccao, TipoEPI } from '../api/tipos';
import { fmtDataHora, ROTULO_EPI } from '../lib/formato';

/** Região do corpo (em coordenadas do quadro 320x240) onde cada EPI é procurado. */
const REGIOES: Record<TipoEPI, { x: number; y: number; w: number; h: number }> = {
  CAPACETE: { x: 133, y: 20, w: 54, h: 26 },
  OCULOS: { x: 141, y: 50, w: 38, h: 13 },
  MASCARA_SOLDA: { x: 136, y: 44, w: 48, h: 34 },
  RESPIRADOR: { x: 145, y: 64, w: 30, h: 16 },
  PROTETOR_AURICULAR: { x: 127, y: 48, w: 66, h: 20 },
  COLETE: { x: 124, y: 90, w: 72, h: 62 },
  AVENTAL: { x: 128, y: 104, w: 64, h: 74 },
  LUVAS: { x: 86, y: 146, w: 148, h: 26 },
  CINTO: { x: 122, y: 146, w: 76, h: 14 },
  BOTA: { x: 128, y: 210, w: 64, h: 24 },
};

/**
 * Quadro simulado da câmera com as caixas da visão computacional: verde = EPI detectado,
 * vermelho tracejado = EPI obrigatório não detectado. As imagens reais ficam fora do escopo do MVP
 * (sem câmeras físicas); o caminho da evidência é registrado na detecção.
 */
export default function EvidenciaCamera({ deteccao, ausentes }: { deteccao: Deteccao; ausentes: TipoEPI[] }) {
  const detectados = deteccao.episDetectados;
  return (
    <figure className="overflow-hidden rounded-2xl bg-painel-900 ring-1 ring-painel-700">
      <svg viewBox="0 0 320 240" className="block w-full" role="img"
        aria-label={`Evidência: detectados ${detectados.map((t) => ROTULO_EPI[t]).join(', ') || 'nenhum'}; ausentes ${ausentes.map((t) => ROTULO_EPI[t]).join(', ') || 'nenhum'}`}>
        <defs>
          <pattern id="grade" width="20" height="20" patternUnits="userSpaceOnUse">
            <path d="M20 0H0V20" fill="none" stroke="#1B2640" strokeWidth="1" />
          </pattern>
          <linearGradient id="piso" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stopColor="#111A2E" />
            <stop offset="1" stopColor="#26334F" />
          </linearGradient>
        </defs>
        <rect width="320" height="240" fill="url(#grade)" />
        <rect y="186" width="320" height="54" fill="url(#piso)" />
        {/* máquina ao fundo */}
        <rect x="18" y="70" width="70" height="116" rx="6" fill="#1B2640" stroke="#26334F" />
        <rect x="28" y="84" width="50" height="30" rx="3" fill="#0B1220" />
        <circle cx="53" cy="140" r="14" fill="#0B1220" stroke="#F59E0B" strokeOpacity="0.4" />
        <rect x="244" y="96" width="56" height="90" rx="6" fill="#1B2640" stroke="#26334F" />
        {/* silhueta do operador */}
        <g fill="#64748B">
          <circle cx="160" cy="56" r="18" />
          <rect x="128" y="80" width="64" height="80" rx="18" />
          <rect x="100" y="88" width="22" height="70" rx="11" transform="rotate(12 111 88)" />
          <rect x="198" y="88" width="22" height="70" rx="11" transform="rotate(-12 209 88)" />
          <rect x="134" y="150" width="22" height="72" rx="10" />
          <rect x="164" y="150" width="22" height="72" rx="10" />
        </g>
        {detectados.map((t) => {
          const r = REGIOES[t];
          return (
            <g key={`d-${t}`}>
              <rect x={r.x} y={r.y} width={r.w} height={r.h} rx="3" fill="#10B981" fillOpacity="0.12" stroke="#34D399" strokeWidth="1.6" />
              <rect x={r.x} y={r.y - 9} width={Math.max(r.w, ROTULO_EPI[t].length * 4.6)} height="9" fill="#059669" />
              <text x={r.x + 2} y={r.y - 2} fontSize="7" fill="white" fontFamily="Inter, sans-serif">{ROTULO_EPI[t]}</text>
            </g>
          );
        })}
        {ausentes.map((t, i) => {
          const r = REGIOES[t];
          return (
            <g key={`a-${t}`}>
              <rect x={r.x - 3} y={r.y - 3} width={r.w + 6} height={r.h + 6} rx="4" fill="#EF4444" fillOpacity="0.15" stroke="#F87171" strokeWidth="2" strokeDasharray="5 3" />
              <circle cx={r.x + r.w + 3} cy={r.y - 3} r="6.5" fill="#DC2626" stroke="#0B1220" strokeWidth="1.5" />
              <text x={r.x + r.w + 3} y={r.y - 0.4} fontSize="7.5" fontWeight="700" fill="white" textAnchor="middle" fontFamily="Inter, sans-serif">{i + 1}</text>
            </g>
          );
        })}
        {/* lista numerada dos EPIs ausentes, à direita do quadro, para não sobrepor as caixas */}
        {ausentes.map((t, i) => {
          const rotulo = `${i + 1} · SEM ${ROTULO_EPI[t].toUpperCase()}`;
          const w = rotulo.length * 4.15 + 8;
          return (
            <g key={`l-${t}`}>
              <rect x={312 - w} y={30 + i * 15} width={w} height="12" rx="2" fill="#DC2626" />
              <text x={316 - w} y={38.5 + i * 15} fontSize="7" fontWeight="700" fill="white" fontFamily="Inter, sans-serif">{rotulo}</text>
            </g>
          );
        })}
        {/* sobreposição da câmera */}
        <rect x="8" y="8" width="94" height="16" rx="3" fill="#0B1220" fillOpacity="0.85" />
        <circle cx="17" cy="16" r="3.5" fill="#EF4444" className="pulsar" />
        <text x="25" y="19.5" fontSize="8" fill="#E2E8F0" fontFamily="ui-monospace, monospace">{deteccao.dispositivo ?? 'CAMERA'}</text>
        <rect x="198" y="8" width="114" height="16" rx="3" fill="#0B1220" fillOpacity="0.85" />
        <text x="204" y="19.5" fontSize="8" fill="#E2E8F0" fontFamily="ui-monospace, monospace">{fmtDataHora(deteccao.dataHora)}</text>
        <rect x="8" y="216" width="150" height="16" rx="3" fill="#0B1220" fillOpacity="0.85" />
        <text x="14" y="227.5" fontSize="8" fill="#E2E8F0" fontFamily="ui-monospace, monospace">
          confiança {(deteccao.confianca * 100).toFixed(1)}% · {deteccao.origem.toLowerCase()}
        </text>
      </svg>
      <figcaption className="flex flex-wrap items-center gap-x-4 gap-y-1 border-t border-painel-700 px-4 py-2.5 text-xs text-slate-300">
        <span className="flex items-center gap-1.5"><span className="h-2.5 w-2.5 rounded-sm bg-emerald-400" /> EPI detectado</span>
        <span className="flex items-center gap-1.5"><span className="h-2.5 w-2.5 rounded-sm border-2 border-dashed border-red-400" /> EPI obrigatório ausente</span>
        {deteccao.evidenciaUrl && <span className="ml-auto truncate font-mono text-slate-500">{deteccao.evidenciaUrl}</span>}
      </figcaption>
    </figure>
  );
}
