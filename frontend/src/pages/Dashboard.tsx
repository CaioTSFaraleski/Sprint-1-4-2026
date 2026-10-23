import {
  Activity, AlertOctagon, BellRing, CheckCircle2, Clock4, Cpu, Play, Repeat, ShieldCheck, Zap, ZapOff,
} from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Bar, CartesianGrid, ComposedChart, Legend, Line, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { api } from '../api/endpoints';
import type { Deteccao } from '../api/tipos';
import { useAuth } from '../auth/Autenticacao';
import { useAvisos } from '../components/Avisos';
import { BadgeRisco, BadgeStatus, Botao, CabecalhoPagina, Carregando, KPI, MensagemErro, PONTO_RISCO, Vazio } from '../components/ui';
import { fmtDiaMes, fmtHora, fmtMin, fmtPct, fmtRelativo, ROTULO_EPI, ROTULO_RISCO } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';
import { useEventos } from '../realtime/TempoReal';

export default function Dashboard() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const avisar = useAvisos();
  const { dados, erro, carregando, recarregar, setDados } = useCarregar(api.dashboard);
  const [novos, setNovos] = useState<Set<number>>(new Set());
  const [simulando, setSimulando] = useState<'conforme' | 'falha' | null>(null);

  useEventos((e) => {
    if (e.deteccao) {
      // detecção conforme chega pelo WebSocket e entra no topo do feed sem recarregar
      const det = e.deteccao;
      setDados((d) => (d ? { ...d, ultimasDeteccoes: [det, ...d.ultimasDeteccoes.filter((x) => x.id !== det.id)].slice(0, 15) } : d));
      setNovos((s) => new Set(s).add(det.id));
    }
    if (e.ocorrencia) recarregar(true);
  });

  const simular = async (naoConforme: boolean) => {
    setSimulando(naoConforme ? 'falha' : 'conforme');
    try {
      const d = await api.simularDeteccao(naoConforme);
      if (d.conforme) avisar({ tipo: 'sucesso', titulo: 'Detecção conforme', texto: `${d.operadorNome} com todos os EPIs em ${d.areaNome}.` });
      recarregar(true);
    } catch (e) {
      avisar({ tipo: 'erro', titulo: 'Não foi possível simular', texto: e instanceof Error ? e.message : undefined });
    } finally {
      setSimulando(null);
    }
  };

  const alternarSimulador = async () => {
    if (!dados) return;
    const s = await api.alterarSimulador(!dados.simuladorAtivo);
    setDados({ ...dados, simuladorAtivo: s.ativo });
    avisar({ tipo: 'info', titulo: s.ativo ? 'Simulador ligado' : 'Simulador desligado',
      texto: s.ativo ? `Uma detecção a cada ${Math.round(s.intervaloMs / 1000)} s.` : 'As câmeras simuladas pararam de enviar detecções.' });
  };

  if (carregando && !dados) return <Carregando />;
  if (erro && !dados) return <MensagemErro texto={erro} onTentar={() => recarregar()} />;
  if (!dados) return null;

  const serie = dados.serie7d.map((s) => ({ ...s, dia: fmtDiaMes(s.data) }));
  const primeiroNome = usuario?.nome.split(' ')[0];

  return (
    <>
      <CabecalhoPagina
        titulo="Dashboard de Monitoramento"
        subtitulo={`Olá, ${primeiroNome}. Visão em tempo real da conformidade de EPIs na planta.`}
        acoes={<Link to="/ocorrencias"><Botao variante="secundario"><BellRing size={16} /> Ver todos os alertas</Botao></Link>}
      />

      <div className="grid grid-cols-2 gap-4 xl:grid-cols-5">
        <KPI titulo="Alertas ativos" icone={<AlertOctagon size={20} />} tom={dados.criticasAtivas > 0 ? 'critico' : 'alerta'}
          valor={dados.alertasAtivos} onClick={() => navigate('/ocorrencias')}
          detalhe={<><b className="text-red-700">{dados.criticasAtivas} críticos</b> · {dados.aguardandoValidacao} em validação · {dados.escaladas} escalados</>} />
        <KPI titulo="Conformidade hoje" icone={<ShieldCheck size={20} />} tom={dados.taxaConformidadeHoje >= 90 ? 'ok' : 'alerta'}
          valor={fmtPct(dados.taxaConformidadeHoje)} detalhe={`${dados.deteccoesHoje} detecções analisadas hoje`} />
        <KPI titulo="Conformidade 7 dias" icone={<CheckCircle2 size={20} />} tom="ok" valor={fmtPct(dados.taxaConformidade7d)}
          detalhe={<span className={dados.variacaoConformidade >= 0 ? 'font-semibold text-emerald-700' : 'font-semibold text-red-700'}>
            {dados.variacaoConformidade >= 0 ? '▲' : '▼'} {Math.abs(dados.variacaoConformidade).toLocaleString('pt-BR')} p.p. vs. semana anterior</span>} />
        <KPI titulo="Tempo médio de regularização" icone={<Clock4 size={20} />} valor={fmtMin(dados.tempoMedioRegularizacaoMin)}
          detalhe="Da detecção até o operador regularizar (7 dias)" />
        <KPI titulo="Reincidência" icone={<Repeat size={20} />} tom={dados.taxaReincidencia > 40 ? 'alerta' : 'neutro'}
          valor={fmtPct(dados.taxaReincidencia)} detalhe="Mesmo EPI faltando de novo em até 7 dias" />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 xl:grid-cols-3">
        <section className="cartao p-5 xl:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-bold">Ocorrências e conformidade nos últimos 7 dias</h2>
            <Link to="/relatorios" className="text-sm font-semibold text-marca-600 hover:underline">Relatório completo</Link>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={serie} margin={{ top: 5, right: 5, left: -15, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" vertical={false} />
                <XAxis dataKey="dia" tick={{ fontSize: 12 }} stroke="#94A3B8" />
                <YAxis yAxisId="oc" allowDecimals={false} tick={{ fontSize: 12 }} stroke="#94A3B8" />
                <YAxis yAxisId="pct" orientation="right" domain={[50, 100]} tick={{ fontSize: 12 }} stroke="#94A3B8" unit="%" />
                <Tooltip formatter={(v: number, nome: string) => (nome === 'Conformidade' ? `${v}%` : v)} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar yAxisId="oc" dataKey="ocorrencias" name="Ocorrências" fill="#F59E0B" radius={[6, 6, 0, 0]} maxBarSize={36} />
                <Bar yAxisId="oc" dataKey="criticas" name="Críticas" fill="#DC2626" radius={[6, 6, 0, 0]} maxBarSize={36} />
                <Line yAxisId="pct" type="monotone" dataKey="taxaConformidade" name="Conformidade" stroke="#059669" strokeWidth={3} dot={{ r: 4 }} />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        </section>

        <section className="cartao p-5">
          <h2 className="mb-1 font-bold">Simulador de câmeras</h2>
          <p className="mb-4 text-sm text-slate-500">
            Substitui as câmeras e sensores reais no MVP: envia detecções pelo mesmo fluxo do edge gateway.
          </p>
          <button onClick={alternarSimulador}
            className={`mb-4 flex w-full items-center gap-3 rounded-xl p-3.5 text-left ring-1 transition ${
              dados.simuladorAtivo ? 'bg-emerald-50 ring-emerald-200' : 'bg-slate-50 ring-slate-200'}`}>
            {dados.simuladorAtivo ? <Zap className="text-emerald-600" /> : <ZapOff className="text-slate-500" />}
            <div className="flex-1">
              <p className="text-sm font-bold">{dados.simuladorAtivo ? 'Geração automática ligada' : 'Geração automática desligada'}</p>
              <p className="text-xs text-slate-500">Toque para {dados.simuladorAtivo ? 'desligar' : 'ligar'}</p>
            </div>
            <span className={`h-6 w-11 rounded-full p-0.5 transition ${dados.simuladorAtivo ? 'bg-emerald-500' : 'bg-slate-300'}`}>
              <span className={`block h-5 w-5 rounded-full bg-white shadow transition ${dados.simuladorAtivo ? 'translate-x-5' : ''}`} />
            </span>
          </button>
          <div className="grid gap-2">
            <Botao variante="perigo" grande carregando={simulando === 'falha'} onClick={() => simular(true)}>
              <Play size={16} /> Simular operador sem EPI
            </Botao>
            <Botao variante="secundario" grande carregando={simulando === 'conforme'} onClick={() => simular(false)}>
              <CheckCircle2 size={16} /> Simular detecção conforme
            </Botao>
          </div>
        </section>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 xl:grid-cols-3">
        <section className="cartao xl:col-span-2">
          <div className="flex items-center justify-between p-5 pb-3">
            <h2 className="font-bold">Ocorrências ativas</h2>
            <Link to="/ocorrencias" className="text-sm font-semibold text-marca-600 hover:underline">Ver todas</Link>
          </div>
          {dados.ocorrenciasAtivas.length === 0 ? (
            <Vazio icone={<ShieldCheck />} titulo="Nenhuma ocorrência ativa" texto="Todos os operadores monitorados estão com os EPIs obrigatórios." />
          ) : (
            <ul className="divide-y divide-slate-100">
              {dados.ocorrenciasAtivas.map((o) => (
                <li key={o.id}>
                  <Link to={`/ocorrencias/${o.id}`} className="flex flex-wrap items-center gap-3 px-5 py-3.5 hover:bg-slate-50">
                    <span className={`h-10 w-1.5 rounded-full ${PONTO_RISCO[o.severidade]}`} />
                    <div className="min-w-[180px] flex-1">
                      <p className="font-semibold">{o.operadorNome} <span className="font-normal text-slate-500">· {o.areaNome}</span></p>
                      <p className="text-sm text-slate-600">Sem {o.episAusentes.map((e) => e.tipoDescricao.toLowerCase()).join(', ')}</p>
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                      <BadgeRisco nivel={o.severidade} />
                      <BadgeStatus status={o.status} />
                      <span className="w-16 text-right text-xs text-slate-500">{fmtRelativo(o.dataAbertura)}</span>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="cartao">
          <div className="flex items-center justify-between p-5 pb-3">
            <h2 className="flex items-center gap-2 font-bold"><Activity size={18} className="text-marca-600" /> Detecções ao vivo</h2>
            <span className="text-xs text-slate-500">últimas 15</span>
          </div>
          <ul className="max-h-[420px] divide-y divide-slate-100 overflow-y-auto">
            {dados.ultimasDeteccoes.map((d) => <ItemDeteccao key={d.id} d={d} novo={novos.has(d.id)} />)}
          </ul>
        </section>
      </div>

      <section className="cartao mt-6 p-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-bold">Áreas industriais · últimos 7 dias</h2>
          <Link to="/areas" className="text-sm font-semibold text-marca-600 hover:underline">Configurar áreas</Link>
        </div>
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {dados.areas.map((a) => (
            <div key={a.id} className="rounded-xl p-4 ring-1 ring-slate-200">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="font-bold">{a.nome}</p>
                  <p className="text-xs text-slate-500">{a.setor} · risco {ROTULO_RISCO[a.nivelRisco].toLowerCase()}</p>
                </div>
                {a.ocorrenciasAtivas > 0
                  ? <span className="rounded-full bg-red-50 px-2.5 py-1 text-xs font-bold text-red-700 ring-1 ring-red-200">{a.ocorrenciasAtivas} ativa{a.ocorrenciasAtivas > 1 ? 's' : ''}</span>
                  : <span className="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700 ring-1 ring-emerald-200">em dia</span>}
              </div>
              <div className="mt-3 flex items-center gap-3">
                <div className="h-2.5 flex-1 overflow-hidden rounded-full bg-slate-100">
                  <div className={`h-full rounded-full ${a.taxaConformidade7d >= 90 ? 'bg-emerald-500' : a.taxaConformidade7d >= 80 ? 'bg-amber-500' : 'bg-red-500'}`}
                    style={{ width: `${a.taxaConformidade7d}%` }} />
                </div>
                <span className="w-14 text-right text-sm font-bold">{fmtPct(a.taxaConformidade7d)}</span>
              </div>
              <p className="mt-1.5 text-xs text-slate-500">{a.ocorrencias7d} ocorrências na semana</p>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

function ItemDeteccao({ d, novo }: { d: Deteccao; novo: boolean }) {
  const conteudo = (
    <div className={`flex items-start gap-3 px-5 py-3 ${novo ? 'destaque-novo' : ''}`}>
      <span className={`mt-0.5 rounded-lg p-1.5 ${d.conforme ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'}`}>
        {d.conforme ? <CheckCircle2 size={16} /> : <AlertOctagon size={16} />}
      </span>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-semibold">{d.operadorNome}</p>
        <p className="truncate text-xs text-slate-500">
          {d.areaNome} · {d.conforme ? 'todos os EPIs' : `${d.ocorrenciaCodigo ?? 'não conforme'}`}
        </p>
        <p className="mt-0.5 flex items-center gap-1 truncate text-[11px] text-slate-400">
          <Cpu size={11} /> {d.dispositivo} · {d.episDetectados.map((t) => ROTULO_EPI[t]).join(', ') || 'nenhum EPI'}
        </p>
      </div>
      <span className="text-xs text-slate-500">{fmtHora(d.dataHora)}</span>
    </div>
  );
  return <li>{d.ocorrenciaId ? <Link to={`/ocorrencias/${d.ocorrenciaId}`} className="block hover:bg-slate-50">{conteudo}</Link> : conteudo}</li>;
}
