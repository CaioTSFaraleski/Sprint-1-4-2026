import { Filter, ScrollText, X } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { TipoEventoAuditoria } from '../api/tipos';
import { Botao, CabecalhoPagina, Campo, Carregando, MensagemErro, PONTO_RISCO, Vazio } from '../components/ui';
import { diasAtrasISO, fmtDataHora, hojeISO, ROTULO_EVENTO, ROTULO_PERFIL } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';
import { useEventos } from '../realtime/TempoReal';

const COR_PERFIL: Record<string, string> = {
  SISTEMA: 'bg-slate-100 text-slate-700',
  OPERADOR: 'bg-sky-100 text-sky-800',
  SUPERVISOR: 'bg-amber-100 text-amber-900',
  GESTOR: 'bg-violet-100 text-violet-800',
};

export default function Auditoria() {
  const [params, setParams] = useSearchParams();
  const navigate = useNavigate();
  const ocorrenciaId = params.get('ocorrenciaId') ? Number(params.get('ocorrenciaId')) : null;
  const [filtro, setFiltro] = useState({
    inicio: params.get('inicio') ?? (ocorrenciaId ? diasAtrasISO(60) : diasAtrasISO(7)),
    fim: params.get('fim') ?? hojeISO(),
    areaId: params.get('areaId') ? Number(params.get('areaId')) : null,
    tipo: (params.get('tipo') ?? '') as TipoEventoAuditoria | '',
    autor: '',
  });
  const { dados: areas } = useCarregar(api.areas);
  const { dados, erro, carregando, recarregar } = useCarregar(
    () => api.auditoria({ ...filtro, ocorrenciaId }),
    [filtro.inicio, filtro.fim, filtro.areaId, filtro.tipo, ocorrenciaId],
  );

  useEventos(() => recarregar(true));

  const autorFiltrado = (dados ?? []).filter((e) => !filtro.autor.trim()
    || e.autorNome.toLowerCase().includes(filtro.autor.trim().toLowerCase()));

  return (
    <>
      <CabecalhoPagina titulo="Trilha de Auditoria"
        subtitulo="Registro imutável dos eventos críticos: quem fez o quê, quando e em qual ocorrência (RF15)." />

      {ocorrenciaId && (
        <div className="mb-4 flex items-center gap-3 rounded-xl bg-marca-50 px-4 py-3 text-sm ring-1 ring-marca-100">
          <Filter size={16} className="text-marca-600" />
          <span>Mostrando apenas os eventos da ocorrência <b>#{ocorrenciaId}</b>.</span>
          <button onClick={() => { params.delete('ocorrenciaId'); setParams(params); }} className="ml-auto flex items-center gap-1 font-semibold text-slate-700 hover:underline">
            <X size={14} /> Limpar filtro
          </button>
        </div>
      )}

      <div className="cartao mb-4 grid gap-3 p-4 md:grid-cols-5">
        <Campo rotulo="De"><input type="date" className="campo" value={filtro.inicio} onChange={(e) => setFiltro({ ...filtro, inicio: e.target.value })} /></Campo>
        <Campo rotulo="Até"><input type="date" className="campo" value={filtro.fim} onChange={(e) => setFiltro({ ...filtro, fim: e.target.value })} /></Campo>
        <Campo rotulo="Área">
          <select className="campo" value={filtro.areaId ?? ''} onChange={(e) => setFiltro({ ...filtro, areaId: e.target.value ? Number(e.target.value) : null })}>
            <option value="">Todas</option>
            {areas?.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
          </select>
        </Campo>
        <Campo rotulo="Tipo de evento">
          <select className="campo" value={filtro.tipo} onChange={(e) => setFiltro({ ...filtro, tipo: e.target.value as TipoEventoAuditoria | '' })}>
            <option value="">Todos</option>
            {(Object.keys(ROTULO_EVENTO) as TipoEventoAuditoria[]).map((t) => <option key={t} value={t}>{ROTULO_EVENTO[t]}</option>)}
          </select>
        </Campo>
        <Campo rotulo="Autor"><input className="campo" placeholder="Nome" value={filtro.autor} onChange={(e) => setFiltro({ ...filtro, autor: e.target.value })} /></Campo>
      </div>

      {carregando && !dados ? <Carregando /> : erro && !dados ? <MensagemErro texto={erro} onTentar={() => recarregar()} /> : (
        <section className="cartao overflow-hidden">
          {autorFiltrado.length === 0 ? <Vazio icone={<ScrollText />} titulo="Nenhum evento no período" /> : (
            <div className="overflow-x-auto">
              <table className="tabela w-full">
                <thead className="bg-slate-50"><tr><th>Data e hora</th><th>Evento</th><th>Autor</th><th>Área</th><th>Ocorrência</th><th>Descrição</th></tr></thead>
                <tbody>
                  {autorFiltrado.map((e) => (
                    <tr key={e.id} className={e.ocorrenciaId ? 'cursor-pointer hover:bg-slate-50' : ''}
                      onClick={() => e.ocorrenciaId && navigate(`/ocorrencias/${e.ocorrenciaId}`)}>
                      <td className="whitespace-nowrap font-mono text-xs">{fmtDataHora(e.dataHora)}</td>
                      <td>
                        <span className="flex items-center gap-2 whitespace-nowrap font-semibold">
                          {e.severidade && <span className={`h-2.5 w-2.5 rounded-full ${PONTO_RISCO[e.severidade]}`} title={e.severidade} />}
                          {ROTULO_EVENTO[e.tipo]}
                        </span>
                      </td>
                      <td>
                        <p className="whitespace-nowrap">{e.autorNome}</p>
                        <span className={`rounded px-1.5 py-0.5 text-[11px] font-semibold ${COR_PERFIL[e.autorPerfil] ?? ''}`}>{ROTULO_PERFIL[e.autorPerfil] ?? e.autorPerfil}</span>
                      </td>
                      <td className="whitespace-nowrap">{e.areaNome ?? '—'}</td>
                      <td>
                        {e.ocorrenciaId ? (
                          <Link to={`/ocorrencias/${e.ocorrenciaId}`} onClick={(ev) => ev.stopPropagation()} className="font-mono text-xs font-bold text-marca-600 hover:underline">
                            {e.ocorrenciaCodigo}
                          </Link>
                        ) : '—'}
                      </td>
                      <td className="min-w-[260px] text-slate-600">{e.descricao}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <div className="flex items-center justify-between border-t border-slate-100 px-4 py-3 text-xs text-slate-500">
            <span>{autorFiltrado.length} evento(s){(dados?.length ?? 0) >= 500 ? ' · mostrando os 500 mais recentes' : ''}</span>
            <Botao variante="fantasma" onClick={() => window.print()} className="nao-imprimir">Imprimir trilha</Botao>
          </div>
        </section>
      )}
    </>
  );
}
