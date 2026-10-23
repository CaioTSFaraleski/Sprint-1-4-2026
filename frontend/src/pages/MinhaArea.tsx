import { BellRing, CheckCircle2, ClipboardCheck, Clock4, HardHat, ShieldCheck } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { OcorrenciaResumo } from '../api/tipos';
import { useAuth } from '../auth/Autenticacao';
import { useAvisos } from '../components/Avisos';
import { BadgeRisco, BadgeStatus, Botao, Carregando, MensagemErro, Modal } from '../components/ui';
import { fmtDataHora, fmtRelativo, ROTULO_TURNO } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';
import { useEventos } from '../realtime/TempoReal';

/** Tela do operador no tablet/celular: o que fazer agora, com botões grandes para uso com luvas. */
export default function MinhaArea() {
  const { usuario } = useAuth();
  const avisar = useAvisos();
  const ocorrencias = useCarregar(() => api.ocorrencias({ categoria: 'todos', dias: 30 }));
  const ficha = useCarregar(() => api.colaborador(usuario!.id), [usuario?.id]);
  const [confirmando, setConfirmando] = useState<OcorrenciaResumo | null>(null);
  const [observacao, setObservacao] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEventos(() => {
    ocorrencias.recarregar(true);
    ficha.recarregar(true);
  });

  if (ocorrencias.carregando && !ocorrencias.dados) return <Carregando />;
  if (ocorrencias.erro && !ocorrencias.dados) return <MensagemErro texto={ocorrencias.erro} onTentar={() => ocorrencias.recarregar()} />;

  const lista = ocorrencias.dados ?? [];
  const pendentes = lista.filter((o) => o.status === 'ABERTA' || o.status === 'ESCALADA');
  const emValidacao = lista.filter((o) => o.status === 'AGUARDANDO_VALIDACAO');
  const historico = lista.filter((o) => o.status === 'ENCERRADA').slice(0, 5);

  const confirmar = async () => {
    if (!confirmando) return;
    setEnviando(true);
    try {
      await api.regularizar(confirmando.id, observacao || undefined);
      avisar({ tipo: 'sucesso', titulo: 'Regularização enviada', texto: 'O supervisor vai validar em campo.' });
      setConfirmando(null);
      setObservacao('');
      ocorrencias.recarregar(true);
      ficha.recarregar(true);
    } catch (e) {
      avisar({ tipo: 'erro', titulo: 'Não foi possível enviar', texto: e instanceof Error ? e.message : undefined });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-extrabold tracking-tight md:text-3xl">Olá, {usuario?.nome.split(' ')[0]}</h1>
        <p className="text-slate-500">{usuario?.areaNome} · {usuario?.turno ? ROTULO_TURNO[usuario.turno] : ''}</p>
      </div>

      {pendentes.length === 0 ? (
        <div className="cartao flex items-center gap-4 bg-emerald-50 p-6 ring-emerald-200">
          <span className="rounded-2xl bg-emerald-500 p-3 text-white"><ShieldCheck size={30} /></span>
          <div>
            <p className="text-lg font-bold text-emerald-900">Tudo certo com seus EPIs</p>
            <p className="text-sm text-emerald-800">Nenhum alerta pendente. Bom trabalho e segurança sempre.</p>
          </div>
        </div>
      ) : (
        pendentes.map((o) => (
          <section key={o.id} className="cartao overflow-hidden ring-2 ring-red-300">
            <div className="flex items-center gap-3 bg-red-600 px-5 py-3 text-white">
              <BellRing className="pulsar" size={22} />
              <p className="font-bold">Alerta de segurança · {fmtRelativo(o.dataAbertura)}</p>
              <span className="ml-auto"><BadgeRisco nivel={o.severidade} /></span>
            </div>
            <div className="p-5">
              <p className="text-sm font-semibold text-slate-500">Coloque agora:</p>
              <div className="mt-2 flex flex-wrap gap-2">
                {o.episAusentes.map((e) => (
                  <span key={e.tipo} className="flex items-center gap-2 rounded-xl bg-red-50 px-4 py-3 text-lg font-bold text-red-800 ring-1 ring-red-200">
                    <HardHat size={22} /> {e.tipoDescricao}
                  </span>
                ))}
              </div>
              <p className="mt-3 text-sm text-slate-600">Detectado por {o.dispositivo} em {o.areaNome} às {fmtDataHora(o.dataAbertura)}.</p>
              <div className="mt-5 flex flex-col gap-2 sm:flex-row">
                <Botao grande variante="sucesso" className="flex-1" onClick={() => setConfirmando(o)}>
                  <ClipboardCheck size={20} /> Já coloquei: confirmar regularização
                </Botao>
                <Link to={`/ocorrencias/${o.id}`} className="sm:w-auto"><Botao grande variante="secundario" className="w-full">Ver detalhes</Botao></Link>
              </div>
            </div>
          </section>
        ))
      )}

      {emValidacao.length > 0 && (
        <section className="cartao p-5">
          <h2 className="mb-3 flex items-center gap-2 font-bold"><Clock4 size={18} className="text-sky-600" /> Aguardando validação do supervisor</h2>
          <ul className="space-y-2">
            {emValidacao.map((o) => (
              <li key={o.id}>
                <Link to={`/ocorrencias/${o.id}`} className="flex flex-wrap items-center gap-2 rounded-xl p-3 ring-1 ring-slate-200 hover:bg-slate-50">
                  <span className="font-mono text-sm font-bold">{o.codigo}</span>
                  <span className="flex-1 text-sm text-slate-600">{o.episAusentes.map((e) => e.tipoDescricao).join(', ')}</span>
                  <BadgeStatus status={o.status} />
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="cartao p-5">
        <h2 className="mb-1 font-bold">Orientações da minha área</h2>
        <p className="mb-3 text-sm text-slate-500">EPIs obrigatórios em {usuario?.areaNome} no seu turno.</p>
        {ficha.dados ? (
          <ul className="grid gap-2 sm:grid-cols-2">
            {ficha.dados.episObrigatorios.map((e) => (
              <li key={e.tipo} className={`flex items-center gap-3 rounded-xl p-3.5 ring-1 ${e.detectado ? 'ring-slate-200' : 'bg-red-50 ring-red-200'}`}>
                {e.detectado ? <CheckCircle2 className="text-emerald-600" /> : <HardHat className="text-red-600" />}
                <div className="min-w-0 flex-1">
                  <p className="font-semibold">{e.tipoDescricao}</p>
                  <p className="truncate text-xs text-slate-500">{e.epiNome}</p>
                </div>
                <BadgeRisco nivel={e.severidade} compacto />
              </li>
            ))}
          </ul>
        ) : <Carregando />}
      </section>

      {historico.length > 0 && (
        <section className="cartao p-5">
          <h2 className="mb-3 font-bold">Últimas ocorrências regularizadas</h2>
          <ul className="space-y-1 text-sm text-slate-600">
            {historico.map((o) => (
              <li key={o.id}>✓ {fmtDataHora(o.dataAbertura)} · {o.episAusentes.map((e) => e.tipoDescricao.toLowerCase()).join(', ')} · {o.areaNome}</li>
            ))}
          </ul>
        </section>
      )}

      <Modal aberto={confirmando !== null} titulo="Confirmar regularização" onFechar={() => setConfirmando(null)}>
        <p className="text-slate-700">Você colocou <b>{confirmando?.episAusentes.map((e) => e.tipoDescricao.toLowerCase()).join(' e ')}</b>?</p>
        <p className="mt-1 text-sm text-slate-500">O supervisor de segurança vai conferir e validar.</p>
        <textarea className="campo mt-4 min-h-[80px]" placeholder="Comentário (opcional)" value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={500} />
        <div className="mt-5 grid grid-cols-2 gap-2">
          <Botao grande variante="secundario" onClick={() => setConfirmando(null)}>Voltar</Botao>
          <Botao grande variante="sucesso" carregando={enviando} onClick={confirmar}>Sim, confirmar</Botao>
        </div>
      </Modal>
    </div>
  );
}
