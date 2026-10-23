import { Download, FileBarChart2, Printer, ScrollText } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api, type FiltroRelatorio } from '../api/endpoints';
import type { NivelRisco, Relatorio, TipoEPI } from '../api/tipos';
import { useAvisos } from '../components/Avisos';
import { Botao, CabecalhoPagina, Campo, Carregando, Chip, KPI, MensagemErro } from '../components/ui';
import { diasAtrasISO, fmtData, fmtDataHora, fmtDiaMes, fmtMin, fmtPct, hojeISO, NIVEIS, ROTULO_EPI, ROTULO_RISCO, ROTULO_TURNO, TIPOS_EPI } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';

const PRESETS = [7, 30, 90];

export default function Relatorios() {
  const avisar = useAvisos();
  const { dados: areas } = useCarregar(api.areas);
  const [filtro, setFiltro] = useState<FiltroRelatorio>({ inicio: diasAtrasISO(29), fim: hojeISO(), areaId: null, setor: '', tipoEpi: '', severidade: '' });
  const [aplicado, setAplicado] = useState<FiltroRelatorio>(filtro);
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.relatorio(aplicado), [aplicado]);
  const [baixando, setBaixando] = useState(false);
  const setores = [...new Set((areas ?? []).map((a) => a.setor))].sort();

  const preset = (dias: number) => {
    const f = { ...filtro, inicio: diasAtrasISO(dias - 1), fim: hojeISO() };
    setFiltro(f);
    setAplicado(f);
  };

  const exportar = async () => {
    setBaixando(true);
    try {
      await api.baixarRelatorioCsv(aplicado);
    } catch (e) {
      avisar({ tipo: 'erro', titulo: 'Falha ao exportar', texto: e instanceof Error ? e.message : undefined });
    } finally {
      setBaixando(false);
    }
  };

  const linkAuditoria = `/auditoria?inicio=${aplicado.inicio}&fim=${aplicado.fim}${aplicado.areaId ? `&areaId=${aplicado.areaId}` : ''}`;

  return (
    <>
      <CabecalhoPagina titulo="Relatórios de Conformidade"
        subtitulo="Conformidade por área, setor, período, turno e tipo de EPI, para decisões preventivas."
        acoes={<>
          <Botao variante="secundario" onClick={() => window.print()}><Printer size={16} /> Imprimir / PDF</Botao>
          <Botao onClick={exportar} carregando={baixando}><Download size={16} /> Exportar CSV</Botao>
        </>} />

      <section className="cartao nao-imprimir mb-6 p-4">
        <div className="mb-3 flex flex-wrap gap-2">
          {PRESETS.map((d) => <Chip key={d} ativo={aplicado.inicio === diasAtrasISO(d - 1) && aplicado.fim === hojeISO()} onClick={() => preset(d)}>Últimos {d} dias</Chip>)}
        </div>
        <div className="grid gap-3 md:grid-cols-3 xl:grid-cols-6">
          <Campo rotulo="De"><input type="date" className="campo" value={filtro.inicio} onChange={(e) => setFiltro({ ...filtro, inicio: e.target.value })} /></Campo>
          <Campo rotulo="Até"><input type="date" className="campo" value={filtro.fim} onChange={(e) => setFiltro({ ...filtro, fim: e.target.value })} /></Campo>
          <Campo rotulo="Setor">
            <select className="campo" value={filtro.setor} onChange={(e) => setFiltro({ ...filtro, setor: e.target.value, areaId: null })}>
              <option value="">Todos</option>{setores.map((s) => <option key={s}>{s}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Área">
            <select className="campo" value={filtro.areaId ?? ''} onChange={(e) => setFiltro({ ...filtro, areaId: e.target.value ? Number(e.target.value) : null })}>
              <option value="">Todas</option>
              {areas?.filter((a) => !filtro.setor || a.setor === filtro.setor).map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Tipo de EPI">
            <select className="campo" value={filtro.tipoEpi} onChange={(e) => setFiltro({ ...filtro, tipoEpi: e.target.value as TipoEPI | '' })}>
              <option value="">Todos</option>{TIPOS_EPI.map((t) => <option key={t} value={t}>{ROTULO_EPI[t]}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Severidade">
            <select className="campo" value={filtro.severidade} onChange={(e) => setFiltro({ ...filtro, severidade: e.target.value as NivelRisco | '' })}>
              <option value="">Todas</option>{NIVEIS.map((n) => <option key={n} value={n}>{ROTULO_RISCO[n]}</option>)}
            </select>
          </Campo>
        </div>
        <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
          <p className="text-xs text-slate-500">Tipo de EPI e severidade filtram as ocorrências; o índice de conformidade considera todas as detecções da área/setor.</p>
          <Botao onClick={() => setAplicado({ ...filtro })}><FileBarChart2 size={16} /> Gerar relatório</Botao>
        </div>
      </section>

      {carregando && !dados ? <Carregando texto="Gerando relatório…" /> : erro && !dados ? <MensagemErro texto={erro} onTentar={() => recarregar()} /> : dados && (
        <Conteudo r={dados} areaNome={areas?.find((a) => a.id === dados.areaId)?.nome} linkAuditoria={linkAuditoria} />
      )}
    </>
  );
}

function Conteudo({ r, areaNome, linkAuditoria }: { r: Relatorio; areaNome?: string; linkAuditoria: string }) {
  const serie = r.serieDiaria.map((s) => ({ ...s, dia: fmtDiaMes(s.data) }));
  return (
    <div className="space-y-6">
      <p className="text-sm text-slate-600">
        Período de <b>{fmtData(r.inicio)}</b> a <b>{fmtData(r.fim)}</b>
        {areaNome ? <> · área <b>{areaNome}</b></> : r.setor ? <> · setor <b>{r.setor}</b></> : ' · todas as áreas'}
        {r.tipoEpi && <> · EPI <b>{ROTULO_EPI[r.tipoEpi]}</b></>}
        {r.severidade && <> · severidade <b>{ROTULO_RISCO[r.severidade]}</b></>}
        <span className="text-slate-400"> · gerado em {fmtDataHora(r.geradoEm)}</span>
      </p>
      <div className="grid grid-cols-2 gap-4 xl:grid-cols-5">
        <KPI titulo="Índice de conformidade" icone={<FileBarChart2 size={20} />} tom={r.indiceConformidade >= 90 ? 'ok' : 'alerta'}
          valor={fmtPct(r.indiceConformidade)} detalhe={`${r.deteccoesConformes} de ${r.totalDeteccoes} detecções`} />
        <KPI titulo="Ocorrências" icone={<FileBarChart2 size={20} />} valor={r.totalOcorrencias} detalhe="Não conformidades registradas" />
        <KPI titulo="Críticas" icone={<FileBarChart2 size={20} />} tom="critico" valor={r.ocorrenciasCriticas}
          detalhe={r.totalOcorrencias ? `${fmtPct(Math.round((1000 * r.ocorrenciasCriticas) / r.totalOcorrencias) / 10)} do total` : '—'} />
        <KPI titulo="Tempo médio de regularização" icone={<FileBarChart2 size={20} />} valor={fmtMin(r.tempoMedioRegularizacaoMin)} detalhe="Da detecção à regularização" />
        <KPI titulo="Reincidência" icone={<FileBarChart2 size={20} />} valor={fmtPct(r.taxaReincidencia)} detalhe="Mesmo EPI faltando de novo em até 7 dias" />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <section className="cartao p-5">
          <h2 className="mb-4 font-bold">Conformidade diária</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <LineChart data={serie} margin={{ top: 5, right: 10, left: -15, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" vertical={false} />
                <XAxis dataKey="dia" tick={{ fontSize: 11 }} stroke="#94A3B8" interval="preserveStartEnd" minTickGap={18} />
                <YAxis domain={[50, 100]} unit="%" tick={{ fontSize: 11 }} stroke="#94A3B8" />
                <Tooltip formatter={(v: number) => `${v}%`} />
                <Line type="monotone" dataKey="taxaConformidade" name="Conformidade" stroke="#059669" strokeWidth={2.5} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </section>
        <section className="cartao p-5">
          <h2 className="mb-4 font-bold">EPIs mais ausentes</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <BarChart data={r.porEpi} layout="vertical" margin={{ top: 0, right: 20, left: 40, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" horizontal={false} />
                <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11 }} stroke="#94A3B8" />
                <YAxis type="category" dataKey="descricao" width={110} tick={{ fontSize: 11 }} stroke="#94A3B8" />
                <Tooltip />
                <Bar dataKey="ocorrencias" name="Ocorrências" fill="#F59E0B" radius={[0, 6, 6, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>
      </div>

      <section className="cartao overflow-hidden">
        <div className="flex items-center justify-between p-5 pb-3">
          <h2 className="font-bold">Resumo por área</h2>
          <Link to={linkAuditoria} className="nao-imprimir flex items-center gap-1.5 text-sm font-semibold text-marca-600 hover:underline">
            <ScrollText size={15} /> Ver trilha de auditoria completa
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="tabela w-full">
            <thead className="bg-slate-50"><tr><th>Área</th><th>Setor</th><th>Detecções</th><th>Conformidade</th><th>Ocorrências</th><th>Críticas</th><th>Tempo médio</th></tr></thead>
            <tbody>
              {r.porArea.map((a) => (
                <tr key={a.areaId}>
                  <td className="font-semibold">{a.area}</td>
                  <td>{a.setor}</td>
                  <td>{a.deteccoes}</td>
                  <td>
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-24 overflow-hidden rounded-full bg-slate-100">
                        <div className={`h-full ${a.taxaConformidade >= 90 ? 'bg-emerald-500' : a.taxaConformidade >= 80 ? 'bg-amber-500' : 'bg-red-500'}`} style={{ width: `${a.taxaConformidade}%` }} />
                      </div>
                      <b>{fmtPct(a.taxaConformidade)}</b>
                    </div>
                  </td>
                  <td>{a.ocorrencias}</td>
                  <td className={a.criticas ? 'font-semibold text-red-700' : ''}>{a.criticas}</td>
                  <td>{fmtMin(a.tempoMedioRegularizacaoMin)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <section className="cartao p-5">
          <h2 className="mb-3 font-bold">Por turno</h2>
          <table className="tabela w-full">
            <thead><tr><th>Turno</th><th>Detecções</th><th>Conformidade</th><th>Ocorrências</th></tr></thead>
            <tbody>
              {r.porTurno.map((t) => (
                <tr key={t.turno}><td className="whitespace-nowrap font-semibold">Turno {t.turno} <span className="text-xs font-normal text-slate-500">{ROTULO_TURNO[t.turno].split('· ')[1]}</span></td><td>{t.deteccoes}</td><td>{fmtPct(t.taxaConformidade)}</td><td>{t.ocorrencias}</td></tr>
              ))}
            </tbody>
          </table>
        </section>
        <section className="cartao p-5">
          <h2 className="mb-1 font-bold">Colaboradores com ocorrências repetidas</h2>
          <p className="mb-3 text-xs text-slate-500">Para orientação e treinamento, não para punição automática.</p>
          {r.reincidentes.length === 0 ? <p className="text-sm text-slate-500">Nenhum reincidente no período.</p> : (
            <table className="tabela w-full">
              <thead><tr><th>Colaborador</th><th>Área</th><th>Ocorrências</th></tr></thead>
              <tbody>
                {r.reincidentes.map((x) => (
                  <tr key={x.operadorId}><td><p className="font-semibold">{x.nome}</p><p className="text-xs text-slate-500">{x.matricula}</p></td><td>{x.area}</td><td className="font-bold">{x.ocorrencias}</td></tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  );
}
