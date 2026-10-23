import { Check } from 'lucide-react';
import type { StatusOcorrencia } from '../api/tipos';

/** As 7 etapas do fluxo da ocorrência (diagrama de atividades da Sprint 1, tela de detalhe da Sprint 2). */
const ETAPAS = [
  'Detecção automática recebida',
  'Regra de segurança verificada',
  'Não conformidade identificada',
  'Alerta emitido ao operador',
  'Aguardando regularização',
  'Validação do supervisor',
  'Ocorrência encerrada',
];

function etapaAtual(status: StatusOcorrencia) {
  switch (status) {
    case 'ABERTA':
    case 'ESCALADA':
      return 4;
    case 'AGUARDANDO_VALIDACAO':
      return 5;
    case 'ENCERRADA':
      return 7;
  }
}

export default function LinhaDoTempo({ status }: { status: StatusOcorrencia }) {
  const atual = etapaAtual(status);
  return (
    <ol className="grid grid-cols-1 gap-2 sm:grid-cols-7 sm:gap-0">
      {ETAPAS.map((rotulo, i) => {
        const concluida = i < atual;
        const ativa = i === atual;
        return (
          <li key={rotulo} className="relative flex items-center gap-3 sm:flex-col sm:gap-2 sm:text-center">
            {i > 0 && (
              <span className={`absolute left-[15px] top-[-10px] h-3 w-0.5 sm:left-auto sm:right-1/2 sm:top-[15px] sm:h-0.5 sm:w-full ${
                concluida || ativa ? 'bg-emerald-500' : 'bg-slate-200'}`} />
            )}
            <span className={`relative z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold ring-4 ring-white ${
              concluida ? 'bg-emerald-500 text-white' : ativa ? (status === 'ESCALADA' ? 'bg-purple-600 text-white' : 'bg-marca-500 text-painel-900') : 'bg-slate-200 text-slate-500'
            }`}>
              {concluida ? <Check size={16} strokeWidth={3} /> : i + 1}
            </span>
            <span className={`text-xs font-semibold leading-tight sm:px-1 ${ativa ? 'text-slate-900' : concluida ? 'text-slate-600' : 'text-slate-400'}`}>
              {rotulo}
              {ativa && status === 'ESCALADA' && <span className="block text-purple-700">(escalada)</span>}
            </span>
          </li>
        );
      })}
    </ol>
  );
}
