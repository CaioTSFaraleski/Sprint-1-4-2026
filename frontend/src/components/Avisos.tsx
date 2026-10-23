import { AlertTriangle, CheckCircle2, Info, X } from 'lucide-react';
import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';

type Tipo = 'sucesso' | 'erro' | 'alerta' | 'info';
interface Aviso { id: number; tipo: Tipo; titulo: string; texto?: string; acao?: { rotulo: string; onClick: () => void } }

const Ctx = createContext<(a: Omit<Aviso, 'id'>) => void>(() => {});

export function ProvedorAvisos({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([]);
  const remover = (id: number) => setAvisos((l) => l.filter((a) => a.id !== id));
  const avisar = useCallback((a: Omit<Aviso, 'id'>) => {
    const id = Date.now() + Math.random();
    setAvisos((l) => [...l.slice(-3), { ...a, id }]);
    window.setTimeout(() => remover(id), a.tipo === 'alerta' ? 9000 : 5000);
  }, []);

  const icones = {
    sucesso: <CheckCircle2 className="text-emerald-600" size={20} />,
    erro: <AlertTriangle className="text-red-600" size={20} />,
    alerta: <AlertTriangle className="text-orange-600" size={20} />,
    info: <Info className="text-sky-600" size={20} />,
  };

  return (
    <Ctx.Provider value={avisar}>
      {children}
      <div className="nao-imprimir pointer-events-none fixed bottom-4 right-4 z-[60] flex w-[min(380px,calc(100vw-2rem))] flex-col gap-2">
        {avisos.map((a) => (
          <div
            key={a.id}
            role="status"
            className={`animar-entrada pointer-events-auto flex gap-3 rounded-2xl bg-white p-4 shadow-xl ring-1 ${
              a.tipo === 'alerta' ? 'ring-orange-300' : a.tipo === 'erro' ? 'ring-red-300' : 'ring-slate-200'
            }`}
          >
            <div className="mt-0.5">{icones[a.tipo]}</div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-bold text-slate-900">{a.titulo}</p>
              {a.texto && <p className="mt-0.5 text-sm text-slate-600">{a.texto}</p>}
              {a.acao && (
                <button
                  onClick={() => { a.acao!.onClick(); remover(a.id); }}
                  className="mt-2 text-sm font-semibold text-marca-600 hover:underline"
                >
                  {a.acao.rotulo}
                </button>
              )}
            </div>
            <button onClick={() => remover(a.id)} className="self-start text-slate-400 hover:text-slate-600" aria-label="Fechar aviso">
              <X size={16} />
            </button>
          </div>
        ))}
      </div>
    </Ctx.Provider>
  );
}

export const useAvisos = () => useContext(Ctx);
