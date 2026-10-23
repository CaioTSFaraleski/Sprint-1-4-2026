import { CheckCircle2, MapPin, Pencil, Plus, Search, ShieldPlus, Trash2, Users } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { api, type AreaForm } from '../api/endpoints';
import type { Area, NivelRisco, Turno } from '../api/tipos';
import { useAuth } from '../auth/Autenticacao';
import { useAvisos } from '../components/Avisos';
import { BadgeRisco, Botao, CabecalhoPagina, Campo, Carregando, Chip, MensagemErro, Modal, Vazio } from '../components/ui';
import { NIVEIS, ROTULO_RISCO, ROTULO_TURNO } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';

const TURNOS: Turno[] = ['A', 'B', 'C'];

export default function Areas() {
  const avisar = useAvisos();
  const { dados, erro, carregando, recarregar, setDados } = useCarregar(api.areas);
  const [busca, setBusca] = useState('');
  const [setor, setSetor] = useState('');
  const [selecionada, setSelecionada] = useState<number | null>(null);
  const [aba, setAba] = useState<'epis' | 'dados'>('epis');
  const [formArea, setFormArea] = useState<{ aberto: boolean; area: Area | null }>({ aberto: false, area: null });

  const setores = useMemo(() => [...new Set((dados ?? []).map((a) => a.setor))].sort(), [dados]);
  const filtradas = useMemo(() => {
    const t = busca.trim().toLowerCase();
    return (dados ?? []).filter((a) => (!setor || a.setor === setor) && (!t || a.nome.toLowerCase().includes(t) || a.setor.toLowerCase().includes(t)));
  }, [dados, busca, setor]);

  useEffect(() => {
    if (selecionada === null && dados?.length) setSelecionada(dados[0].id);
  }, [dados, selecionada]);

  const atual = dados?.find((a) => a.id === selecionada) ?? null;
  const substituir = (a: Area) => setDados((l) => (l ? l.map((x) => (x.id === a.id ? a : x)) : l));

  if (carregando && !dados) return <Carregando />;
  if (erro && !dados) return <MensagemErro texto={erro} onTentar={() => recarregar()} />;

  return (
    <>
      <CabecalhoPagina titulo="Áreas e Regras de Segurança"
        subtitulo="Quais EPIs são obrigatórios em cada área, por turno e atividade. É daqui que o motor de não conformidade lê as regras."
        acoes={<Botao onClick={() => setFormArea({ aberto: true, area: null })}><Plus size={16} /> Nova área</Botao>} />

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)]">
        <section className="cartao overflow-hidden">
          <div className="space-y-3 border-b border-slate-100 p-4">
            <div className="relative">
              <Search size={18} className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
              <input className="campo min-h-[46px] pl-10" placeholder="Buscar área" value={busca} onChange={(e) => setBusca(e.target.value)} />
            </div>
            <div className="flex flex-wrap gap-2">
              <Chip ativo={setor === ''} onClick={() => setSetor('')}>Todos os setores</Chip>
              {setores.map((s) => <Chip key={s} ativo={setor === s} onClick={() => setSetor(s)}>{s}</Chip>)}
            </div>
          </div>
          <ul className="divide-y divide-slate-100">
            {filtradas.length === 0 && <Vazio icone={<MapPin />} titulo="Nenhuma área encontrada" />}
            {filtradas.map((a) => (
              <li key={a.id}>
                <button onClick={() => setSelecionada(a.id)} className={`flex w-full items-center gap-3 px-4 py-3.5 text-left ${a.id === selecionada ? 'bg-marca-50' : 'hover:bg-slate-50'}`}>
                  <div className="min-w-0 flex-1">
                    <p className="font-semibold">{a.nome} {!a.ativa && <span className="ml-1 rounded bg-slate-200 px-1.5 text-xs text-slate-600">inativa</span>}</p>
                    <p className="text-xs text-slate-500">{a.setor} · turnos {a.turnos.join(', ')} · {a.regras.length} regras · {a.totalOperadores} operadores</p>
                  </div>
                  {a.ocorrenciasAtivas > 0 && <span className="rounded-full bg-red-50 px-2 py-0.5 text-xs font-bold text-red-700 ring-1 ring-red-200">{a.ocorrenciasAtivas}</span>}
                  <BadgeRisco nivel={a.nivelRisco} />
                </button>
              </li>
            ))}
          </ul>
        </section>

        {atual && (
          <section className="cartao overflow-hidden">
            <div className="flex flex-wrap items-start gap-3 border-b border-slate-100 p-5">
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <h2 className="text-xl font-extrabold">{atual.nome}</h2>
                  <BadgeRisco nivel={atual.nivelRisco} />
                </div>
                <p className="mt-1 text-sm text-slate-600">{atual.descricao}</p>
                <p className="mt-1 flex flex-wrap gap-x-4 text-xs text-slate-500">
                  <span>Setor {atual.setor}</span>
                  <span className="flex items-center gap-1"><Users size={12} /> {atual.totalOperadores} operadores</span>
                  <span>Supervisão: {atual.supervisorNome ?? '—'}</span>
                </p>
              </div>
              <Botao variante="secundario" onClick={() => setFormArea({ aberto: true, area: atual })}><Pencil size={15} /> Editar área</Botao>
            </div>
            <div className="flex gap-2 border-b border-slate-100 px-5 pt-3">
              {(['epis', 'dados'] as const).map((t) => (
                <button key={t} onClick={() => setAba(t)}
                  className={`-mb-px border-b-2 px-3 pb-3 text-sm font-semibold ${aba === t ? 'border-marca-500 text-slate-900' : 'border-transparent text-slate-500 hover:text-slate-700'}`}>
                  {t === 'epis' ? `EPIs obrigatórios (${atual.regras.length})` : 'Regras de segurança'}
                </button>
              ))}
            </div>
            {aba === 'epis' ? (
              <EpisObrigatorios area={atual} onAtualizada={(a) => { substituir(a); }} avisar={avisar} />
            ) : (
              <RegrasTexto area={atual} />
            )}
          </section>
        )}
      </div>

      <ModalArea estado={formArea} onFechar={() => setFormArea({ aberto: false, area: null })}
        onSalva={(a, nova) => {
          setFormArea({ aberto: false, area: null });
          if (nova) { setDados((l) => (l ? [...l, a].sort((x, y) => x.nome.localeCompare(y.nome)) : [a])); setSelecionada(a.id); }
          else substituir(a);
          avisar({ tipo: 'sucesso', titulo: nova ? 'Área cadastrada' : 'Área atualizada', texto: 'Alteração registrada na trilha de auditoria.' });
        }} />
    </>
  );
}

function EpisObrigatorios({ area, onAtualizada, avisar }: { area: Area; onAtualizada: (a: Area) => void; avisar: ReturnType<typeof useAvisos> }) {
  const [adicionando, setAdicionando] = useState(false);
  const [removendo, setRemovendo] = useState<number | null>(null);
  const remover = async (regraId: number) => {
    if (!confirm('Remover esta regra? A área deixa de exigir esse EPI.')) return;
    setRemovendo(regraId);
    try {
      onAtualizada(await api.removerRegra(area.id, regraId));
      avisar({ tipo: 'sucesso', titulo: 'Regra removida' });
    } catch (e) {
      avisar({ tipo: 'erro', titulo: 'Não foi possível remover', texto: e instanceof Error ? e.message : undefined });
    } finally {
      setRemovendo(null);
    }
  };
  return (
    <div className="p-5">
      {area.regras.length === 0 ? (
        <Vazio icone={<ShieldPlus />} titulo="Nenhum EPI obrigatório" texto="Sem regras, nenhuma detecção nesta área gera ocorrência." />
      ) : (
        <ul className="space-y-2">
          {area.regras.map((r) => (
            <li key={r.id} className="flex flex-wrap items-center gap-3 rounded-xl p-3.5 ring-1 ring-slate-200">
              <CheckCircle2 size={18} className="text-emerald-600" />
              <div className="min-w-[200px] flex-1">
                <p className="font-semibold">{r.tipoEpiDescricao} <span className="font-normal text-slate-500">· {r.epiNome}</span></p>
                <p className="text-xs text-slate-500">{r.turno ? ROTULO_TURNO[r.turno] : 'Todos os turnos'}{r.atividade ? ` · atividade: ${r.atividade}` : ''}</p>
              </div>
              <BadgeRisco nivel={r.severidadePadrao} />
              <button onClick={() => remover(r.id)} disabled={removendo === r.id} className="rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-600" aria-label="Remover regra">
                <Trash2 size={17} />
              </button>
            </li>
          ))}
        </ul>
      )}
      <div className="mt-4">
        <Botao variante="secundario" onClick={() => setAdicionando(true)}><ShieldPlus size={16} /> Adicionar EPI obrigatório</Botao>
      </div>
      <ModalRegra aberto={adicionando} area={area} onFechar={() => setAdicionando(false)}
        onSalva={(a) => { setAdicionando(false); onAtualizada(a); avisar({ tipo: 'sucesso', titulo: 'Regra adicionada', texto: 'Novas detecções já são avaliadas com ela.' }); }} />
    </div>
  );
}

function RegrasTexto({ area }: { area: Area }) {
  return (
    <div className="space-y-3 p-5">
      <p className="text-sm text-slate-600">Como o motor avalia cada detecção em {area.nome}:</p>
      <ol className="list-decimal space-y-2 pl-5 text-sm text-slate-700">
        <li>Identifica o turno pelo horário da detecção ({area.turnos.map((t) => ROTULO_TURNO[t]).join('; ')}).</li>
        <li>Separa as regras obrigatórias que valem para esse turno ({area.regras.length} cadastradas).</li>
        <li>Compara com os EPIs reconhecidos pela câmera ou sensor.</li>
        <li>Faltando algum, abre ocorrência com a maior severidade entre as regras violadas, sobe um nível se faltarem 2 ou mais EPIs e outro se o operador for reincidente em 7 dias.</li>
        <li>Envia alerta ao operador e ao supervisor ({area.supervisorNome ?? 'sem supervisor definido'}) ao mesmo tempo.</li>
      </ol>
      <div className="rounded-xl bg-amber-50 p-4 text-sm text-amber-900 ring-1 ring-amber-200">
        A detecção automática nunca encerra nem pune sozinha: o encerramento exige validação do supervisor (RN04).
      </div>
    </div>
  );
}

function ModalRegra({ aberto, area, onFechar, onSalva }: { aberto: boolean; area: Area; onFechar: () => void; onSalva: (a: Area) => void }) {
  const { dados: epis } = useCarregar(api.epis);
  const [epiId, setEpiId] = useState<number | ''>('');
  const [descricao, setDescricao] = useState('');
  const [severidade, setSeveridade] = useState<NivelRisco>('ALTO');
  const [turno, setTurno] = useState<Turno | ''>('');
  const [atividade, setAtividade] = useState('');
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);

  const salvar = async () => {
    if (!epiId) return setErro('Escolha o EPI.');
    const epi = epis?.find((e) => e.id === epiId);
    setSalvando(true);
    setErro('');
    try {
      const a = await api.adicionarRegra(area.id, {
        epiId: Number(epiId), descricao: descricao.trim() || `Uso obrigatório de ${epi?.tipoDescricao.toLowerCase()} em ${area.nome}`,
        severidadePadrao: severidade, turno: turno || null, atividade: atividade.trim() || undefined,
      });
      setEpiId(''); setDescricao(''); setTurno(''); setAtividade('');
      onSalva(a);
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Erro ao salvar.');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <Modal aberto={aberto} titulo={`Novo EPI obrigatório em ${area.nome}`} onFechar={onFechar}>
      <div className="space-y-4">
        <Campo rotulo="EPI">
          <select className="campo min-h-[46px]" value={epiId} onChange={(e) => setEpiId(e.target.value ? Number(e.target.value) : '')}>
            <option value="">Selecione…</option>
            {epis?.filter((e) => e.certificadoValido).map((e) => <option key={e.id} value={e.id}>{e.tipoDescricao} · {e.nome}</option>)}
          </select>
        </Campo>
        <div className="grid grid-cols-2 gap-3">
          <Campo rotulo="Severidade se faltar">
            <select className="campo min-h-[46px]" value={severidade} onChange={(e) => setSeveridade(e.target.value as NivelRisco)}>
              {NIVEIS.map((n) => <option key={n} value={n}>{ROTULO_RISCO[n]}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Turno">
            <select className="campo min-h-[46px]" value={turno} onChange={(e) => setTurno(e.target.value as Turno | '')}>
              <option value="">Todos os turnos</option>
              {TURNOS.map((t) => <option key={t} value={t}>{ROTULO_TURNO[t]}</option>)}
            </select>
          </Campo>
        </div>
        <Campo rotulo="Atividade (opcional)" ajuda="Ex.: Esmerilhamento, Troca de ferramenta">
          <input className="campo min-h-[46px]" value={atividade} onChange={(e) => setAtividade(e.target.value)} maxLength={100} />
        </Campo>
        <Campo rotulo="Descrição da regra (opcional)">
          <input className="campo min-h-[46px]" value={descricao} onChange={(e) => setDescricao(e.target.value)} maxLength={300}
            placeholder="Ex.: Óculos obrigatório no esmerilhamento" />
        </Campo>
        {erro && <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{erro}</p>}
        <div className="flex justify-end gap-2">
          <Botao variante="fantasma" onClick={onFechar}>Cancelar</Botao>
          <Botao onClick={salvar} carregando={salvando}>Adicionar regra</Botao>
        </div>
      </div>
    </Modal>
  );
}

function ModalArea({ estado, onFechar, onSalva }: { estado: { aberto: boolean; area: Area | null }; onFechar: () => void; onSalva: (a: Area, nova: boolean) => void }) {
  const { usuario } = useAuth();
  const { dados: supervisores } = useCarregar(() => api.usuarios('SUPERVISOR'), [usuario?.id]);
  const vazio: AreaForm = { nome: '', setor: '', nivelRisco: 'MEDIO', turnos: ['A', 'B'], descricao: '', supervisorId: null, ativa: true };
  const [form, setForm] = useState<AreaForm>(vazio);
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    const a = estado.area;
    setErro('');
    setForm(a ? { nome: a.nome, setor: a.setor, nivelRisco: a.nivelRisco, turnos: a.turnos, descricao: a.descricao ?? '', supervisorId: a.supervisorId, ativa: a.ativa } : vazio);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estado.aberto, estado.area]);

  const salvar = async () => {
    if (!form.nome.trim() || !form.setor.trim()) return setErro('Informe nome e setor.');
    if (form.turnos.length === 0) return setErro('Escolha ao menos um turno.');
    setSalvando(true);
    setErro('');
    try {
      const a = estado.area ? await api.atualizarArea(estado.area.id, form) : await api.criarArea(form);
      onSalva(a, !estado.area);
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Erro ao salvar.');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <Modal aberto={estado.aberto} titulo={estado.area ? `Editar ${estado.area.nome}` : 'Nova área industrial'} onFechar={onFechar}>
      <div className="space-y-4">
        <Campo rotulo="Nome da área"><input className="campo min-h-[46px]" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} maxLength={80} /></Campo>
        <div className="grid grid-cols-2 gap-3">
          <Campo rotulo="Setor"><input className="campo min-h-[46px]" value={form.setor} onChange={(e) => setForm({ ...form, setor: e.target.value })} placeholder="Produção" maxLength={60} /></Campo>
          <Campo rotulo="Nível de risco">
            <select className="campo min-h-[46px]" value={form.nivelRisco} onChange={(e) => setForm({ ...form, nivelRisco: e.target.value as NivelRisco })}>
              {NIVEIS.map((n) => <option key={n} value={n}>{ROTULO_RISCO[n]}</option>)}
            </select>
          </Campo>
        </div>
        <div>
          <span className="mb-1.5 block text-sm font-semibold text-slate-700">Turnos de operação</span>
          <div className="flex flex-wrap gap-2">
            {TURNOS.map((t) => (
              <Chip key={t} ativo={form.turnos.includes(t)}
                onClick={() => setForm({ ...form, turnos: form.turnos.includes(t) ? form.turnos.filter((x) => x !== t) : [...form.turnos, t] })}>
                {ROTULO_TURNO[t]}
              </Chip>
            ))}
          </div>
        </div>
        <Campo rotulo="Supervisor responsável">
          <select className="campo min-h-[46px]" value={form.supervisorId ?? ''} onChange={(e) => setForm({ ...form, supervisorId: e.target.value ? Number(e.target.value) : null })}>
            <option value="">Sem supervisor</option>
            {supervisores?.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
          </select>
        </Campo>
        <Campo rotulo="Descrição"><textarea className="campo min-h-[80px]" value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} maxLength={500} /></Campo>
        {estado.area && (
          <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
            <input type="checkbox" className="h-5 w-5 accent-amber-500" checked={form.ativa} onChange={(e) => setForm({ ...form, ativa: e.target.checked })} />
            Área ativa (recebe detecções)
          </label>
        )}
        {erro && <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{erro}</p>}
        <div className="flex justify-end gap-2">
          <Botao variante="fantasma" onClick={onFechar}>Cancelar</Botao>
          <Botao onClick={salvar} carregando={salvando}>{estado.area ? 'Salvar alterações' : 'Cadastrar área'}</Botao>
        </div>
      </div>
    </Modal>
  );
}
