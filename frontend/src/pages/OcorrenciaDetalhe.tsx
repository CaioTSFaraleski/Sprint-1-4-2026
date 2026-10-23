import {
  ArrowLeft, BellRing, CheckCircle2, ClipboardCheck, Cpu, MapPin, MessageSquare, ScrollText, ShieldAlert,
  ThumbsDown, TrendingUp, UserRound, XCircle,
} from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { Acao, OcorrenciaDetalhe as Detalhe, TipoAcao } from '../api/tipos';
import { useAuth } from '../auth/Autenticacao';
import { useAvisos } from '../components/Avisos';
import EvidenciaCamera from '../components/EvidenciaCamera';
import LinhaDoTempo from '../components/LinhaDoTempo';
import { BadgeRisco, BadgeStatus, Botao, Campo, Carregando, MensagemErro, Modal } from '../components/ui';
import { fmtDataHora, ROTULO_ACAO, ROTULO_PERFIL, ROTULO_TURNO } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';
import { useEventos } from '../realtime/TempoReal';

type Dialogo = 'regularizar' | 'aprovar' | 'rejeitar' | 'escalar' | null;

const ICONE_ACAO: Record<TipoAcao, JSX.Element> = {
  ABERTURA: <ShieldAlert size={16} />,
  ALERTA_EMITIDO: <BellRing size={16} />,
  REGULARIZACAO: <ClipboardCheck size={16} />,
  VALIDACAO_APROVADA: <CheckCircle2 size={16} />,
  VALIDACAO_REJEITADA: <ThumbsDown size={16} />,
  ESCALONAMENTO: <TrendingUp size={16} />,
  COMENTARIO: <MessageSquare size={16} />,
  ENCERRAMENTO: <CheckCircle2 size={16} />,
};

export default function OcorrenciaDetalhe() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { usuario } = useAuth();
  const avisar = useAvisos();
  const { dados, erro, carregando, recarregar, setDados } = useCarregar(() => api.ocorrencia(Number(id)), [id]);
  const [dialogo, setDialogo] = useState<Dialogo>(null);
  const [observacao, setObservacao] = useState('');
  const [comentario, setComentario] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [sucesso, setSucesso] = useState<string | null>(null);

  useEventos((e) => {
    if (e.ocorrencia?.id === Number(id)) recarregar(true);
  });

  if (carregando && !dados) return <Carregando />;
  if (erro && !dados) return <MensagemErro texto={erro} onTentar={() => recarregar()} />;
  if (!dados) return null;

  const o = dados.resumo;
  const ehSupervisor = usuario?.perfil === 'SUPERVISOR';
  const ehOperadorDono = usuario?.perfil === 'OPERADOR' && usuario.id === o.operadorId;
  const podeRegularizar = (ehSupervisor || ehOperadorDono) && (o.status === 'ABERTA' || o.status === 'ESCALADA');
  const podeValidar = ehSupervisor && o.status === 'AGUARDANDO_VALIDACAO';
  const podeEscalar = ehSupervisor && o.status === 'ABERTA';

  const executar = async (fn: () => Promise<Detalhe>, mensagem: string) => {
    setEnviando(true);
    try {
      const atualizado = await fn();
      setDados(atualizado);
      setDialogo(null);
      setObservacao('');
      setSucesso(mensagem);
    } catch (e) {
      avisar({ tipo: 'erro', titulo: 'Ação não realizada', texto: e instanceof Error ? e.message : undefined });
    } finally {
      setEnviando(false);
    }
  };

  const comentar = async () => {
    if (!comentario.trim()) return;
    setEnviando(true);
    try {
      setDados(await api.comentar(o.id, comentario.trim()));
      setComentario('');
    } catch (e) {
      avisar({ tipo: 'erro', titulo: 'Comentário não enviado', texto: e instanceof Error ? e.message : undefined });
    } finally {
      setEnviando(false);
    }
  };

  const confirmarDialogo = () => {
    if (dialogo === 'regularizar') executar(() => api.regularizar(o.id, observacao), 'Regularização registrada. A ocorrência agora aguarda a validação do supervisor.');
    if (dialogo === 'aprovar') executar(() => api.validar(o.id, true, observacao), 'Regularização validada. Ocorrência encerrada e indicadores atualizados.');
    if (dialogo === 'rejeitar') executar(() => api.validar(o.id, false, observacao), 'Regularização não validada. O operador recebeu um novo alerta.');
    if (dialogo === 'escalar') executar(() => api.escalar(o.id, observacao), 'Ocorrência escalada. Os gestores foram alertados.');
  };

  return (
    <>
      <button onClick={() => navigate(-1)} className="nao-imprimir mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-slate-600 hover:text-slate-900">
        <ArrowLeft size={16} /> Voltar
      </button>

      <div className="mb-6 flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h1 className="font-mono text-2xl font-extrabold tracking-tight md:text-3xl">{o.codigo}</h1>
            <BadgeRisco nivel={o.severidade} />
            <BadgeStatus status={o.status} />
            {o.reincidente && <span className="rounded-full bg-amber-100 px-2.5 py-1 text-xs font-bold text-amber-900">Reincidente</span>}
          </div>
          <p className="mt-2 text-slate-600">
            <b>{o.operadorNome}</b> sem <b>{o.episAusentes.map((e) => e.tipoDescricao.toLowerCase()).join(' e ')}</b> em {o.areaNome} · {fmtDataHora(o.dataAbertura)}
          </p>
        </div>
        <div className="nao-imprimir flex flex-wrap gap-2">
          {podeRegularizar && (
            <Botao grande variante="sucesso" onClick={() => setDialogo('regularizar')}>
              <ClipboardCheck size={18} /> {ehOperadorDono ? 'Já coloquei o EPI' : 'Registrar regularização em campo'}
            </Botao>
          )}
          {podeValidar && (
            <>
              <Botao grande variante="sucesso" onClick={() => setDialogo('aprovar')}><CheckCircle2 size={18} /> Validar regularização</Botao>
              <Botao grande variante="secundario" onClick={() => setDialogo('rejeitar')}><XCircle size={18} /> Não validar</Botao>
            </>
          )}
          {podeEscalar && (
            <Botao grande variante="secundario" onClick={() => setDialogo('escalar')}><TrendingUp size={18} /> Escalar risco</Botao>
          )}
        </div>
      </div>

      <section className="cartao mb-6 p-5">
        <LinhaDoTempo status={o.status} />
      </section>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-5">
        <div className="space-y-6 xl:col-span-3">
          <section className="cartao p-5">
            <h2 className="mb-3 font-bold">Evidência da detecção</h2>
            <EvidenciaCamera deteccao={dados.deteccao} ausentes={o.episAusentes.map((e) => e.tipo)} />
            <p className="mt-3 text-sm text-slate-600">{dados.descricao}</p>
          </section>

          <section className="cartao p-5">
            <h2 className="mb-4 font-bold">Histórico de ações</h2>
            <ol className="relative space-y-4 border-l-2 border-slate-100 pl-6">
              {dados.acoes.map((a) => <ItemAcao key={a.id} a={a} />)}
            </ol>
            <div className="nao-imprimir mt-5 flex gap-2 border-t border-slate-100 pt-4">
              <input className="campo" placeholder="Adicionar comentário ao histórico…" value={comentario}
                onChange={(e) => setComentario(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && comentar()} maxLength={500} />
              <Botao variante="secundario" onClick={comentar} disabled={!comentario.trim()} carregando={enviando && !dialogo}>Enviar</Botao>
            </div>
          </section>
        </div>

        <div className="space-y-6 xl:col-span-2">
          <section className="cartao divide-y divide-slate-100">
            <Info icone={<UserRound size={18} />} rotulo="Colaborador" valor={o.operadorNome} detalhe={`Matrícula ${o.operadorMatricula}`} />
            <Info icone={<MapPin size={18} />} rotulo="Área" valor={o.areaNome} detalhe={`${o.setor} · supervisão: ${dados.supervisorNome ?? '—'}`} />
            <Info icone={<Cpu size={18} />} rotulo="Origem" valor={dados.deteccao.dispositivo ?? '—'}
              detalhe={`${dados.deteccao.origem.toLowerCase()} · confiança ${(dados.deteccao.confianca * 100).toFixed(1)}%`} />
            <Info icone={<ClipboardCheck size={18} />} rotulo="Regularização" valor={fmtDataHora(o.dataRegularizacao)}
              detalhe={o.dataEncerramento ? `Encerrada em ${fmtDataHora(o.dataEncerramento)}` : 'Encerramento exige validação do supervisor'} />
          </section>

          <section className="cartao p-5">
            <h2 className="mb-1 font-bold">Orientações da área</h2>
            <p className="mb-3 text-xs text-slate-500">Regras de segurança de {o.areaNome} válidas no horário da detecção.</p>
            <ul className="space-y-2">
              {dados.orientacoes.map((r) => {
                const faltou = o.episAusentes.some((e) => e.tipo === r.tipoEpi);
                return (
                  <li key={r.id} className={`flex items-start gap-3 rounded-xl p-3 ring-1 ${faltou ? 'bg-red-50 ring-red-200' : 'ring-slate-200'}`}>
                    {faltou ? <XCircle size={18} className="mt-0.5 shrink-0 text-red-600" /> : <CheckCircle2 size={18} className="mt-0.5 shrink-0 text-emerald-600" />}
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-semibold">{r.tipoEpiDescricao}</p>
                      <p className="text-xs text-slate-600">{r.descricao}{r.turno ? ` · ${ROTULO_TURNO[r.turno]}` : ''}</p>
                    </div>
                    <BadgeRisco nivel={r.severidadePadrao} compacto />
                  </li>
                );
              })}
            </ul>
          </section>

          <section className="cartao p-5">
            <h2 className="mb-3 font-bold">Alertas enviados</h2>
            <ul className="space-y-2">
              {dados.alertas.map((al) => (
                <li key={al.id} className="rounded-xl bg-slate-50 p-3 text-sm">
                  <p className="font-semibold">{al.destinatarioNome} <span className="font-normal text-slate-500">· {al.canal === 'PWA_PUSH' ? 'notificação no tablet' : 'painel'}</span></p>
                  <p className="text-slate-600">{al.mensagem}</p>
                  <p className="mt-1 text-xs text-slate-400">{fmtDataHora(al.dataEnvio)} · {al.status.toLowerCase()}</p>
                </li>
              ))}
            </ul>
          </section>

          {usuario?.perfil !== 'OPERADOR' && (
            <Link to={`/auditoria?ocorrenciaId=${o.id}`} className="nao-imprimir cartao flex items-center gap-3 p-4 font-semibold text-slate-700 hover:ring-marca-500">
              <ScrollText size={18} className="text-marca-600" /> Ver na trilha de auditoria
            </Link>
          )}
        </div>
      </div>

      <Modal aberto={dialogo !== null} onFechar={() => setDialogo(null)}
        titulo={dialogo === 'regularizar' ? 'Confirmar regularização' : dialogo === 'aprovar' ? 'Validar regularização'
          : dialogo === 'rejeitar' ? 'Não validar regularização' : 'Escalar ocorrência'}>
        <p className="mb-4 text-sm text-slate-600">
          {dialogo === 'regularizar' && `Confirme que ${ehOperadorDono ? 'você colocou' : 'o operador colocou'}: ${o.episAusentes.map((e) => e.tipoDescricao.toLowerCase()).join(', ')}. O supervisor vai conferir em campo.`}
          {dialogo === 'aprovar' && 'Confirme que o uso do EPI foi conferido. A ocorrência será encerrada.'}
          {dialogo === 'rejeitar' && 'A ocorrência volta para "aguardando regularização" e o operador recebe um novo alerta. Informe o motivo.'}
          {dialogo === 'escalar' && 'Use para risco crítico ou reincidente: os gestores são alertados e o procedimento de segurança é acionado.'}
        </p>
        <Campo rotulo={dialogo === 'rejeitar' ? 'Motivo (obrigatório)' : 'Observação (opcional)'}>
          <textarea className="campo min-h-[90px]" value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={500}
            placeholder={dialogo === 'rejeitar' ? 'Ex.: protetor auricular colocado de forma incorreta' : ''} />
        </Campo>
        <div className="mt-5 flex justify-end gap-2">
          <Botao variante="fantasma" onClick={() => setDialogo(null)}>Cancelar</Botao>
          <Botao variante={dialogo === 'rejeitar' || dialogo === 'escalar' ? 'perigo' : 'sucesso'} carregando={enviando}
            disabled={dialogo === 'rejeitar' && !observacao.trim()} onClick={confirmarDialogo}>
            Confirmar
          </Botao>
        </div>
      </Modal>

      <Modal aberto={sucesso !== null} onFechar={() => setSucesso(null)} titulo="Tudo certo">
        <div className="flex flex-col items-center gap-3 py-2 text-center">
          <span className="rounded-full bg-emerald-100 p-3 text-emerald-600"><CheckCircle2 size={32} /></span>
          <p className="text-slate-700">{sucesso}</p>
          <div className="mt-2 flex gap-2">
            <Botao variante="secundario" onClick={() => setSucesso(null)}>Ver ocorrência</Botao>
            <Botao onClick={() => navigate(usuario?.perfil === 'OPERADOR' ? '/minha-area' : '/ocorrencias')}>Voltar para a lista</Botao>
          </div>
        </div>
      </Modal>
    </>
  );
}

function ItemAcao({ a }: { a: Acao }) {
  const cor = a.tipo === 'VALIDACAO_REJEITADA' || a.tipo === 'ABERTURA' ? 'bg-red-100 text-red-700'
    : a.tipo === 'ESCALONAMENTO' ? 'bg-purple-100 text-purple-700'
    : a.tipo === 'VALIDACAO_APROVADA' || a.tipo === 'ENCERRAMENTO' ? 'bg-emerald-100 text-emerald-700'
    : 'bg-slate-100 text-slate-600';
  return (
    <li className="relative">
      <span className={`absolute -left-[37px] flex h-7 w-7 items-center justify-center rounded-full ring-4 ring-white ${cor}`}>{ICONE_ACAO[a.tipo]}</span>
      <p className="text-sm font-semibold">{ROTULO_ACAO[a.tipo]}</p>
      <p className="text-xs text-slate-500">{fmtDataHora(a.dataHora)} · {a.autorNome} ({ROTULO_PERFIL[a.autorPerfil] ?? a.autorPerfil})</p>
      {a.observacao && <p className="mt-1 text-sm text-slate-700">{a.observacao}</p>}
    </li>
  );
}

function Info({ icone, rotulo, valor, detalhe }: { icone: JSX.Element; rotulo: string; valor: string; detalhe?: string }) {
  return (
    <div className="flex gap-3 p-4">
      <span className="mt-0.5 text-slate-400">{icone}</span>
      <div className="min-w-0">
        <p className="rotulo">{rotulo}</p>
        <p className="font-semibold">{valor}</p>
        {detalhe && <p className="text-xs text-slate-500">{detalhe}</p>}
      </div>
    </div>
  );
}
