import { Camera, Radio, Search, ShieldCheck } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { OcorrenciaResumo } from '../api/tipos';
import { BadgeRisco, BadgeStatus, CabecalhoPagina, Carregando, Chip, MensagemErro, PONTO_RISCO, Vazio } from '../components/ui';
import { fmtDataHora, fmtRelativo } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';
import { useEventos } from '../realtime/TempoReal';

const CATEGORIAS = [
  { id: 'ativos', rotulo: 'Ativos' },
  { id: 'criticos', rotulo: 'Críticos' },
  { id: 'em_validacao', rotulo: 'Em validação' },
  { id: 'escalados', rotulo: 'Escalados' },
  { id: 'regularizados', rotulo: 'Regularizados' },
  { id: 'todos', rotulo: 'Todos' },
];

const PERIODOS = [
  { dias: 1, rotulo: 'Últimas 24 h' },
  { dias: 7, rotulo: 'Últimos 7 dias' },
  { dias: 30, rotulo: 'Últimos 30 dias' },
  { dias: 0, rotulo: 'Todo o período' },
];

export default function Ocorrencias() {
  const [params, setParams] = useSearchParams();
  const categoria = params.get('categoria') ?? 'ativos';
  const [busca, setBusca] = useState('');
  const [areaId, setAreaId] = useState<number | null>(null);
  const [dias, setDias] = useState(7);
  const [novas, setNovas] = useState<Set<number>>(new Set());

  const { dados: areas } = useCarregar(api.areas);
  const { dados, erro, carregando, recarregar } = useCarregar(
    () => api.ocorrencias({ categoria: 'todos', areaId, dias: dias || null }),
    [areaId, dias],
  );

  useEventos((e) => {
    if (!e.ocorrencia) return;
    if (e.tipo === 'OCORRENCIA_ABERTA') setNovas((s) => new Set(s).add(e.ocorrencia!.id));
    recarregar(true);
  });

  const contagem = useMemo(() => {
    const l = dados ?? [];
    return {
      ativos: l.filter((o) => o.status !== 'ENCERRADA').length,
      criticos: l.filter((o) => o.severidade === 'CRITICO').length,
      em_validacao: l.filter((o) => o.status === 'AGUARDANDO_VALIDACAO').length,
      escalados: l.filter((o) => o.status === 'ESCALADA').length,
      regularizados: l.filter((o) => o.status === 'ENCERRADA').length,
      todos: l.length,
    } as Record<string, number>;
  }, [dados]);

  const filtradas = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    return (dados ?? []).filter((o) => {
      const cat =
        categoria === 'ativos' ? o.status !== 'ENCERRADA'
        : categoria === 'criticos' ? o.severidade === 'CRITICO'
        : categoria === 'em_validacao' ? o.status === 'AGUARDANDO_VALIDACAO'
        : categoria === 'escalados' ? o.status === 'ESCALADA'
        : categoria === 'regularizados' ? o.status === 'ENCERRADA'
        : true;
      if (!cat) return false;
      if (!termo) return true;
      return [o.operadorNome, o.operadorMatricula, o.codigo, o.areaNome, ...o.episAusentes.map((e) => e.tipoDescricao)]
        .some((t) => t.toLowerCase().includes(termo));
    });
  }, [dados, categoria, busca]);

  return (
    <>
      <CabecalhoPagina titulo="Alertas e Ocorrências"
        subtitulo="Não conformidades detectadas pelas câmeras e sensores, com o andamento de cada uma." />

      <div className="nao-imprimir mb-4 flex flex-wrap gap-2">
        {CATEGORIAS.map((c) => (
          <Chip key={c.id} ativo={categoria === c.id} contador={dados ? contagem[c.id] : undefined}
            onClick={() => setParams(c.id === 'ativos' ? {} : { categoria: c.id })}>
            {c.rotulo}
          </Chip>
        ))}
      </div>

      <div className="nao-imprimir mb-4 grid gap-3 md:grid-cols-[1fr_220px_200px]">
        <div className="relative">
          <Search size={18} className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input className="campo min-h-[46px] pl-10" placeholder="Buscar por colaborador, matrícula, código, área ou EPI"
            value={busca} onChange={(e) => setBusca(e.target.value)} />
        </div>
        <select className="campo min-h-[46px]" value={areaId ?? ''} onChange={(e) => setAreaId(e.target.value ? Number(e.target.value) : null)}>
          <option value="">Todas as áreas</option>
          {areas?.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
        </select>
        <select className="campo min-h-[46px]" value={dias} onChange={(e) => setDias(Number(e.target.value))}>
          {PERIODOS.map((p) => <option key={p.dias} value={p.dias}>{p.rotulo}</option>)}
        </select>
      </div>

      {carregando && !dados ? <Carregando /> : erro && !dados ? <MensagemErro texto={erro} onTentar={() => recarregar()} /> : (
        <div className="cartao overflow-hidden">
          {filtradas.length === 0 ? (
            <Vazio icone={<ShieldCheck />} titulo="Nenhuma ocorrência neste filtro" texto="Ajuste a categoria, a área ou o período." />
          ) : (
            <>
              <div className="hidden overflow-x-auto md:block">
                <table className="tabela w-full">
                  <thead className="bg-slate-50">
                    <tr><th>Ocorrência</th><th>Colaborador</th><th>Área</th><th>EPI ausente</th><th>Origem</th><th>Severidade</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {filtradas.map((o) => <Linha key={o.id} o={o} nova={novas.has(o.id)} />)}
                  </tbody>
                </table>
              </div>
              <ul className="divide-y divide-slate-100 md:hidden">
                {filtradas.map((o) => (
                  <li key={o.id}>
                    <Link to={`/ocorrencias/${o.id}`} className={`flex gap-3 p-4 ${novas.has(o.id) ? 'destaque-novo' : ''}`}>
                      <span className={`w-1.5 shrink-0 rounded-full ${PONTO_RISCO[o.severidade]}`} />
                      <div className="min-w-0 flex-1 space-y-1.5">
                        <div className="flex items-center justify-between gap-2">
                          <p className="font-bold">{o.operadorNome}</p>
                          <span className="text-xs text-slate-500">{fmtRelativo(o.dataAbertura)}</span>
                        </div>
                        <p className="text-sm text-slate-600">{o.areaNome} · sem {o.episAusentes.map((e) => e.tipoDescricao.toLowerCase()).join(', ')}</p>
                        <div className="flex flex-wrap gap-2"><BadgeRisco nivel={o.severidade} /><BadgeStatus status={o.status} /></div>
                      </div>
                    </Link>
                  </li>
                ))}
              </ul>
              <p className="border-t border-slate-100 px-4 py-3 text-xs text-slate-500">
                {filtradas.length} ocorrência{filtradas.length === 1 ? '' : 's'} · a lista se atualiza sozinha quando chega uma nova detecção
              </p>
            </>
          )}
        </div>
      )}
    </>
  );
}

function Linha({ o, nova }: { o: OcorrenciaResumo; nova: boolean }) {
  const navigate = useNavigate();
  const ir = () => navigate(`/ocorrencias/${o.id}`);
  return (
    <tr className={`cursor-pointer hover:bg-slate-50 ${nova ? 'destaque-novo' : ''}`} onClick={ir}>
      <td>
        <Link to={`/ocorrencias/${o.id}`} className="font-mono text-sm font-bold text-slate-900 hover:underline" onClick={(e) => e.stopPropagation()}>
          {o.codigo}
        </Link>
        <p className="text-xs text-slate-500">{fmtDataHora(o.dataAbertura)}</p>
      </td>
      <td>
        <p className="font-semibold">{o.operadorNome}</p>
        <p className="text-xs text-slate-500">{o.operadorMatricula}{o.reincidente ? ' · reincidente' : ''}</p>
      </td>
      <td><p>{o.areaNome}</p><p className="text-xs text-slate-500">{o.setor}</p></td>
      <td>
        <div className="flex max-w-[240px] flex-wrap gap-1">
          {o.episAusentes.map((e) => (
            <span key={e.tipo} className="rounded-md bg-red-50 px-2 py-0.5 text-xs font-semibold text-red-700 ring-1 ring-red-100">{e.tipoDescricao}</span>
          ))}
        </div>
      </td>
      <td className="text-xs text-slate-600">
        <span className="flex items-center gap-1.5">{o.origem === 'SENSOR' ? <Radio size={14} /> : <Camera size={14} />}{o.dispositivo}</span>
      </td>
      <td><BadgeRisco nivel={o.severidade} /></td>
      <td><BadgeStatus status={o.status} /></td>
    </tr>
  );
}
