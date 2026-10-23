import { ArrowLeft, CheckCircle2, HardHat } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { Epi, NivelRisco, TipoEPI } from '../api/tipos';
import { Botao, CabecalhoPagina, Campo } from '../components/ui';
import { fmtData, NIVEIS, ROTULO_EPI, ROTULO_RISCO, TIPOS_EPI } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';

export default function NovoEpi() {
  const navigate = useNavigate();
  const { dados: colaboradores } = useCarregar(() => api.colaboradores({}));
  const { dados: areas } = useCarregar(api.areas);
  const [form, setForm] = useState({
    nome: '', tipo: 'OCULOS' as TipoEPI, codigoCA: '', validadeCA: '', observacoes: '',
    operadorId: '' as number | '', areaObrigatoriaId: '' as number | '', severidadeObrigatoria: 'MEDIO' as NivelRisco,
  });
  const [erros, setErros] = useState<string[]>([]);
  const [salvando, setSalvando] = useState(false);
  const [criado, setCriado] = useState<Epi | null>(null);

  const mudar = <K extends keyof typeof form>(campo: K, valor: (typeof form)[K]) => setForm((f) => ({ ...f, [campo]: valor }));

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const problemas: string[] = [];
    if (!form.nome.trim()) problemas.push('Informe o nome do EPI.');
    if (!/^\d{3,6}$/.test(form.codigoCA)) problemas.push('O código CA deve ter de 3 a 6 dígitos.');
    if (!form.validadeCA) problemas.push('Informe a validade do CA.');
    setErros(problemas);
    if (problemas.length) return;
    setSalvando(true);
    try {
      const epi = await api.criarEpi({
        nome: form.nome.trim(), tipo: form.tipo, codigoCA: form.codigoCA, validadeCA: form.validadeCA,
        observacoes: form.observacoes.trim() || undefined,
        operadorId: form.operadorId || null,
        areaObrigatoriaId: form.areaObrigatoriaId || null,
        severidadeObrigatoria: form.areaObrigatoriaId ? form.severidadeObrigatoria : null,
      });
      setCriado(epi);
    } catch (err) {
      setErros([err instanceof Error ? err.message : 'Erro ao salvar.']);
    } finally {
      setSalvando(false);
    }
  };

  if (criado) {
    const colaborador = colaboradores?.find((c) => c.id === form.operadorId);
    const area = areas?.find((a) => a.id === form.areaObrigatoriaId);
    return (
      <div className="cartao mx-auto max-w-xl p-8 text-center">
        <span className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 text-emerald-600"><CheckCircle2 size={36} /></span>
        <h1 className="text-2xl font-extrabold">EPI cadastrado com sucesso</h1>
        <p className="mt-2 text-slate-600">{criado.nome} · CA {criado.codigoCA} · válido até {fmtData(criado.validadeCA)}</p>
        <ul className="mx-auto mt-4 max-w-sm space-y-1 text-left text-sm text-slate-600">
          {colaborador && <li>✓ Entrega registrada para {colaborador.nome}</li>}
          {area && <li>✓ Agora obrigatório em {area.nome} (severidade {ROTULO_RISCO[form.severidadeObrigatoria].toLowerCase()})</li>}
          <li>✓ Registrado na trilha de auditoria</li>
        </ul>
        <div className="mt-6 flex justify-center gap-2">
          <Botao variante="secundario" onClick={() => { setCriado(null); setForm((f) => ({ ...f, nome: '', codigoCA: '', validadeCA: '', observacoes: '' })); }}>Cadastrar outro</Botao>
          <Botao onClick={() => navigate('/colaboradores?aba=epis')}>Voltar para Colaboradores e EPIs</Botao>
        </div>
      </div>
    );
  }

  return (
    <>
      <Link to="/colaboradores" className="mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-slate-600 hover:text-slate-900">
        <ArrowLeft size={16} /> Colaboradores e EPIs
      </Link>
      <CabecalhoPagina titulo="Cadastro de EPI" subtitulo="Inclua o EPI no catálogo e, se quiser, já entregue a um colaborador e torne obrigatório em uma área." />
      <form onSubmit={enviar} className="cartao grid gap-5 p-6 md:grid-cols-2">
        <Campo rotulo="Nome do EPI">
          <input className="campo min-h-[46px]" value={form.nome} onChange={(e) => mudar('nome', e.target.value)} placeholder="Ex.: Óculos de proteção ampla visão" maxLength={100} />
        </Campo>
        <Campo rotulo="Tipo" ajuda="É o que a visão computacional reconhece na imagem.">
          <select className="campo min-h-[46px]" value={form.tipo} onChange={(e) => mudar('tipo', e.target.value as TipoEPI)}>
            {TIPOS_EPI.map((t) => <option key={t} value={t}>{ROTULO_EPI[t]}</option>)}
          </select>
        </Campo>
        <Campo rotulo="Código CA" ajuda="Certificado de Aprovação do Ministério do Trabalho.">
          <input className="campo min-h-[46px] font-mono" inputMode="numeric" value={form.codigoCA} onChange={(e) => mudar('codigoCA', e.target.value.replace(/\D/g, ''))} placeholder="12345" maxLength={6} />
        </Campo>
        <Campo rotulo="Validade do CA">
          <input className="campo min-h-[46px]" type="date" value={form.validadeCA} onChange={(e) => mudar('validadeCA', e.target.value)} />
        </Campo>
        <Campo rotulo="Vincular a um colaborador (opcional)" ajuda="Registra a ficha de entrega com validade de 6 meses.">
          <select className="campo min-h-[46px]" value={form.operadorId} onChange={(e) => mudar('operadorId', e.target.value ? Number(e.target.value) : '')}>
            <option value="">Não entregar agora</option>
            {colaboradores?.map((c) => <option key={c.id} value={c.id}>{c.nome} · {c.matricula} · {c.areaNome}</option>)}
          </select>
        </Campo>
        <div className="grid grid-cols-[1fr_140px] gap-3">
          <Campo rotulo="Obrigatório na área (opcional)">
            <select className="campo min-h-[46px]" value={form.areaObrigatoriaId} onChange={(e) => mudar('areaObrigatoriaId', e.target.value ? Number(e.target.value) : '')}>
              <option value="">Nenhuma</option>
              {areas?.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
            </select>
          </Campo>
          <Campo rotulo="Severidade">
            <select className="campo min-h-[46px]" value={form.severidadeObrigatoria} disabled={!form.areaObrigatoriaId}
              onChange={(e) => mudar('severidadeObrigatoria', e.target.value as NivelRisco)}>
              {NIVEIS.map((n) => <option key={n} value={n}>{ROTULO_RISCO[n]}</option>)}
            </select>
          </Campo>
        </div>
        <div className="md:col-span-2">
          <Campo rotulo="Observações">
            <textarea className="campo min-h-[90px]" value={form.observacoes} onChange={(e) => mudar('observacoes', e.target.value)} maxLength={500} />
          </Campo>
        </div>
        {erros.length > 0 && (
          <ul className="space-y-1 rounded-xl bg-red-50 p-4 text-sm text-red-700 md:col-span-2">{erros.map((e) => <li key={e}>{e}</li>)}</ul>
        )}
        <div className="flex justify-end gap-2 md:col-span-2">
          <Link to="/colaboradores"><Botao type="button" variante="fantasma">Cancelar</Botao></Link>
          <Botao type="submit" grande carregando={salvando}><HardHat size={18} /> Salvar EPI</Botao>
        </div>
      </form>
    </>
  );
}
