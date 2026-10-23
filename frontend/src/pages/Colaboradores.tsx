import { AlertTriangle, CheckCircle2, CircleDashed, HardHat, PackageCheck, Plus, Search, UserRound, Users } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { ColaboradorResumo, Epi, SituacaoColaborador } from '../api/tipos';
import { useAvisos } from '../components/Avisos';
import { BadgeRisco, BadgeStatus, Botao, CabecalhoPagina, Carregando, Chip, MensagemErro, Modal, Vazio } from '../components/ui';
import { fmtData, fmtDataHora, fmtRelativo, ROTULO_TURNO } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';
import { useEventos } from '../realtime/TempoReal';

const SITUACAO: Record<SituacaoColaborador, { rotulo: string; cor: string }> = {
  PENDENTE: { rotulo: 'EPI pendente', cor: 'bg-red-50 text-red-700 ring-red-200' },
  EM_VALIDACAO: { rotulo: 'Em validação', cor: 'bg-sky-50 text-sky-700 ring-sky-200' },
  CONFORME: { rotulo: 'Conforme', cor: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  SEM_LEITURA: { rotulo: 'Sem leitura', cor: 'bg-slate-100 text-slate-600 ring-slate-200' },
};

export default function Colaboradores() {
  const [params, setParams] = useSearchParams();
  const aba = params.get('aba') === 'epis' ? 'epis' : 'colaboradores';
  return (
    <>
      <CabecalhoPagina titulo="Colaboradores e EPIs"
        subtitulo="Quem precisa de qual EPI, o que está pendente agora e o histórico de cada colaborador."
        acoes={<Link to="/epis/novo"><Botao><Plus size={16} /> Cadastrar novo EPI</Botao></Link>} />
      <div className="mb-5 flex gap-2">
        <Chip ativo={aba === 'colaboradores'} onClick={() => setParams({})}><Users size={16} /> Colaboradores</Chip>
        <Chip ativo={aba === 'epis'} onClick={() => setParams({ aba: 'epis' })}><HardHat size={16} /> Catálogo de EPIs</Chip>
      </div>
      {aba === 'colaboradores' ? <ListaColaboradores /> : <CatalogoEpis />}
    </>
  );
}

function ListaColaboradores() {
  const [busca, setBusca] = useState('');
  const [situacao, setSituacao] = useState<SituacaoColaborador | ''>('');
  const [selecionado, setSelecionado] = useState<number | null>(null);
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.colaboradores({}));

  useEventos((e) => e.ocorrencia && recarregar(true));

  const filtrados = useMemo(() => {
    const t = busca.trim().toLowerCase();
    return (dados ?? []).filter((c) =>
      (!situacao || c.situacao === situacao) &&
      (!t || [c.nome, c.matricula, c.areaNome ?? '', c.setor ?? '', c.funcao ?? ''].some((x) => x.toLowerCase().includes(t))));
  }, [dados, busca, situacao]);

  useEffect(() => {
    if (selecionado === null && filtrados.length > 0) {
      setSelecionado((filtrados.find((c) => c.situacao === 'PENDENTE') ?? filtrados[0]).id);
    }
  }, [filtrados, selecionado]);

  if (carregando && !dados) return <Carregando />;
  if (erro && !dados) return <MensagemErro texto={erro} onTentar={() => recarregar()} />;

  const contar = (s: SituacaoColaborador) => (dados ?? []).filter((c) => c.situacao === s).length;

  return (
    <div className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.3fr)]">
      <section className="cartao flex flex-col overflow-hidden">
        <div className="space-y-3 border-b border-slate-100 p-4">
          <div className="relative">
            <Search size={18} className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input className="campo min-h-[46px] pl-10" placeholder="Buscar por nome, matrícula ou setor" value={busca} onChange={(e) => setBusca(e.target.value)} />
          </div>
          <div className="flex flex-wrap gap-2">
            <Chip ativo={situacao === ''} onClick={() => setSituacao('')} contador={dados?.length}>Todos</Chip>
            <Chip ativo={situacao === 'PENDENTE'} onClick={() => setSituacao('PENDENTE')} contador={contar('PENDENTE')}>Pendentes</Chip>
            <Chip ativo={situacao === 'CONFORME'} onClick={() => setSituacao('CONFORME')} contador={contar('CONFORME')}>Conformes</Chip>
          </div>
        </div>
        <ul className="max-h-[640px] divide-y divide-slate-100 overflow-y-auto">
          {filtrados.length === 0 && <Vazio icone={<UserRound />} titulo="Nenhum colaborador encontrado" />}
          {filtrados.map((c) => <ItemColaborador key={c.id} c={c} ativo={c.id === selecionado} onClick={() => setSelecionado(c.id)} />)}
        </ul>
      </section>
      {selecionado !== null ? <FichaColaborador id={selecionado} onAtualizar={() => recarregar(true)} /> : <div />}
    </div>
  );
}

function ItemColaborador({ c, ativo, onClick }: { c: ColaboradorResumo; ativo: boolean; onClick: () => void }) {
  const s = SITUACAO[c.situacao];
  return (
    <li>
      <button onClick={onClick} className={`flex w-full items-center gap-3 px-4 py-3 text-left transition ${ativo ? 'bg-marca-50' : 'hover:bg-slate-50'}`}>
        <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-bold ${ativo ? 'bg-marca-500 text-painel-900' : 'bg-slate-100 text-slate-600'}`}>
          {c.nome.split(' ').map((p) => p[0]).slice(0, 2).join('')}
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate font-semibold">{c.nome}</p>
          <p className="truncate text-xs text-slate-500">{c.matricula} · {c.areaNome} · turno {c.turno}</p>
        </div>
        <span className={`whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${s.cor}`}>
          {s.rotulo}{c.episPendentes > 0 ? ` (${c.episPendentes})` : ''}
        </span>
      </button>
    </li>
  );
}

export function FichaColaborador({ id, onAtualizar, modoOperador }: { id: number; onAtualizar?: () => void; modoOperador?: boolean }) {
  const avisar = useAvisos();
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.colaborador(id), [id]);
  const [entregaAberta, setEntregaAberta] = useState(false);

  useEventos((e) => e.ocorrencia?.operadorId === id && recarregar(true));

  if (carregando && !dados) return <div className="cartao"><Carregando /></div>;
  if (erro && !dados) return <MensagemErro texto={erro} onTentar={() => recarregar()} />;
  if (!dados) return null;
  const c = dados.resumo;
  const pendentes = dados.episObrigatorios.filter((e) => !e.detectado);

  return (
    <section className="cartao overflow-hidden">
      <div className="flex flex-wrap items-start gap-4 border-b border-slate-100 p-5">
        <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-painel-900 text-lg font-bold text-marca-500">
          {c.nome.split(' ').map((p) => p[0]).slice(0, 2).join('')}
        </div>
        <div className="min-w-0 flex-1">
          <h2 className="text-xl font-extrabold">{c.nome}</h2>
          <p className="text-sm text-slate-600">{c.funcao} · {c.areaNome} ({c.setor})</p>
          <p className="text-xs text-slate-500">Matrícula {c.matricula} · {c.turno ? ROTULO_TURNO[c.turno] : 'sem turno'} · {dados.email}</p>
        </div>
        <div className="text-right">
          <span className={`rounded-full px-3 py-1.5 text-xs font-bold ring-1 ${SITUACAO[c.situacao].cor}`}>{SITUACAO[c.situacao].rotulo}</span>
          <p className="mt-2 text-xs text-slate-500">Última leitura {fmtRelativo(c.ultimaDeteccao)}</p>
        </div>
      </div>

      <div className="grid gap-4 border-b border-slate-100 p-5 md:grid-cols-3">
        <Coluna titulo="EPIs obrigatórios" icone={<HardHat size={16} />} itens={dados.episObrigatorios.map((e) => e.tipoDescricao)} tom="neutro" />
        <Coluna titulo="EPIs detectados" icone={<CheckCircle2 size={16} />} itens={dados.episObrigatorios.filter((e) => e.detectado).map((e) => e.tipoDescricao)} tom="ok"
          vazio={dados.ultimaDeteccao ? 'Nenhum' : 'Sem leitura recente'} />
        <Coluna titulo="EPIs pendentes" icone={<AlertTriangle size={16} />} itens={pendentes.map((e) => e.tipoDescricao)} tom={pendentes.length ? 'alerta' : 'ok'} vazio="Nenhum pendente" />
      </div>

      <div className="border-b border-slate-100 p-5">
        <div className="mb-3 flex items-center justify-between gap-2">
          <h3 className="font-bold">Fichas de entrega de EPI</h3>
          {!modoOperador && <Botao variante="secundario" onClick={() => setEntregaAberta(true)}><PackageCheck size={16} /> Registrar entrega</Botao>}
        </div>
        <div className="overflow-x-auto">
          <table className="tabela w-full">
            <thead><tr><th>EPI</th><th>CA</th><th>Entrega</th><th>Válido até</th><th>Situação</th></tr></thead>
            <tbody>
              {dados.entregas.map((en) => (
                <tr key={en.id}>
                  <td className="font-semibold">{en.epiNome}</td>
                  <td className="font-mono text-xs">{en.codigoCA}</td>
                  <td>{fmtData(en.dataEntrega)}</td>
                  <td>{fmtData(en.dataValidade)}</td>
                  <td>
                    <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${en.status === 'ATIVA' ? 'bg-emerald-50 text-emerald-700 ring-emerald-200' : 'bg-red-50 text-red-700 ring-red-200'}`}>
                      {en.status === 'ATIVA' ? 'Válida' : en.status === 'VENCIDA' ? 'Vencida: trocar' : 'Devolvida'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="p-5">
        <h3 className="mb-3 font-bold">Histórico de ocorrências <span className="font-normal text-slate-500">({dados.totalOcorrencias}{dados.reincidente ? ' · reincidente no mês' : ''})</span></h3>
        {dados.ocorrencias.length === 0 ? <p className="text-sm text-slate-500">Nenhuma ocorrência registrada.</p> : (
          <ul className="space-y-2">
            {dados.ocorrencias.slice(0, 8).map((o) => (
              <li key={o.id}>
                <Link to={`/ocorrencias/${o.id}`} className="flex flex-wrap items-center gap-2 rounded-xl p-3 ring-1 ring-slate-200 hover:bg-slate-50">
                  <span className="font-mono text-sm font-bold">{o.codigo}</span>
                  <span className="flex-1 text-sm text-slate-600">sem {o.episAusentes.map((e) => e.tipoDescricao.toLowerCase()).join(', ')} · {fmtDataHora(o.dataAbertura)}</span>
                  <BadgeRisco nivel={o.severidade} compacto />
                  <BadgeStatus status={o.status} />
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>

      {!modoOperador && (
        <ModalEntrega aberto={entregaAberta} operadorId={id} onFechar={() => setEntregaAberta(false)}
          onSalvo={() => { setEntregaAberta(false); recarregar(true); onAtualizar?.(); avisar({ tipo: 'sucesso', titulo: 'Entrega registrada' }); }} />
      )}
    </section>
  );
}

function Coluna({ titulo, icone, itens, tom, vazio = '—' }: { titulo: string; icone: JSX.Element; itens: string[]; tom: 'neutro' | 'ok' | 'alerta'; vazio?: string }) {
  const cores = { neutro: 'text-slate-600', ok: 'text-emerald-700', alerta: 'text-red-700' };
  const fundo = { neutro: 'bg-slate-50', ok: 'bg-emerald-50', alerta: 'bg-red-50' };
  return (
    <div className={`rounded-xl p-4 ${fundo[tom]}`}>
      <p className={`mb-2 flex items-center gap-1.5 text-sm font-bold ${cores[tom]}`}>{icone} {titulo}</p>
      {itens.length === 0 ? <p className="text-sm text-slate-500">{vazio}</p> : (
        <ul className="space-y-1 text-sm text-slate-800">{itens.map((i) => <li key={i}>• {i}</li>)}</ul>
      )}
    </div>
  );
}

function ModalEntrega({ aberto, operadorId, onFechar, onSalvo }: { aberto: boolean; operadorId: number; onFechar: () => void; onSalvo: () => void }) {
  const { dados: epis } = useCarregar(api.epis);
  const [epiId, setEpiId] = useState<number | ''>('');
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);
  const salvar = async () => {
    if (!epiId) return setErro('Escolha o EPI.');
    setSalvando(true);
    setErro('');
    try {
      await api.entregarEpi(operadorId, Number(epiId));
      setEpiId('');
      onSalvo();
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Erro ao registrar.');
    } finally {
      setSalvando(false);
    }
  };
  return (
    <Modal aberto={aberto} titulo="Registrar entrega de EPI" onFechar={onFechar}>
      <select className="campo min-h-[46px]" value={epiId} onChange={(e) => setEpiId(e.target.value ? Number(e.target.value) : '')}>
        <option value="">Selecione o EPI…</option>
        {epis?.map((e) => <option key={e.id} value={e.id}>{e.nome} · CA {e.codigoCA}{e.certificadoValido ? '' : ' (CA vencido)'}</option>)}
      </select>
      <p className="mt-2 text-xs text-slate-500">A validade da ficha é de 6 meses, limitada à validade do CA.</p>
      {erro && <p className="mt-3 rounded-xl bg-red-50 p-3 text-sm text-red-700">{erro}</p>}
      <div className="mt-5 flex justify-end gap-2">
        <Botao variante="fantasma" onClick={onFechar}>Cancelar</Botao>
        <Botao onClick={salvar} carregando={salvando}>Registrar entrega</Botao>
      </div>
    </Modal>
  );
}

function CatalogoEpis() {
  const { dados, erro, carregando, recarregar } = useCarregar(api.epis);
  if (carregando && !dados) return <Carregando />;
  if (erro && !dados) return <MensagemErro texto={erro} onTentar={() => recarregar()} />;
  return (
    <section className="cartao overflow-hidden">
      <div className="overflow-x-auto">
        <table className="tabela w-full">
          <thead className="bg-slate-50"><tr><th>EPI</th><th>Tipo</th><th>CA</th><th>Validade do CA</th><th>Situação</th><th>Observações</th></tr></thead>
          <tbody>
            {(dados ?? []).map((e: Epi) => (
              <tr key={e.id}>
                <td className="font-semibold">{e.nome}</td>
                <td>{e.tipoDescricao}</td>
                <td className="font-mono text-xs">{e.codigoCA}</td>
                <td>{fmtData(e.validadeCA)}</td>
                <td>
                  {e.certificadoValido
                    ? <span className="inline-flex items-center gap-1 text-sm font-semibold text-emerald-700"><CheckCircle2 size={15} /> CA válido</span>
                    : <span className="inline-flex items-center gap-1 text-sm font-semibold text-red-700"><CircleDashed size={15} /> CA vencido</span>}
                </td>
                <td className="max-w-xs text-xs text-slate-500">{e.observacoes ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="border-t border-slate-100 px-4 py-3 text-xs text-slate-500">
        EPIs com CA vencido não podem ser entregues a colaboradores (NR-6).
      </p>
    </section>
  );
}

