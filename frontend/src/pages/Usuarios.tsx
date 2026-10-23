import { Search, UserPlus, Users } from 'lucide-react';
import { useMemo, useState } from 'react';
import { api } from '../api/endpoints';
import type { Perfil, Turno } from '../api/tipos';
import { useAuth } from '../auth/Autenticacao';
import { useAvisos } from '../components/Avisos';
import { Botao, CabecalhoPagina, Campo, Carregando, Chip, MensagemErro, Modal, Vazio } from '../components/ui';
import { ROTULO_PERFIL, ROTULO_TURNO } from '../lib/formato';
import { useCarregar } from '../lib/useCarregar';

const COR: Record<Perfil, string> = {
  OPERADOR: 'bg-sky-100 text-sky-800',
  SUPERVISOR: 'bg-amber-100 text-amber-900',
  GESTOR: 'bg-violet-100 text-violet-800',
};

export default function Usuarios() {
  const { usuario } = useAuth();
  const avisar = useAvisos();
  const { dados, erro, carregando, recarregar } = useCarregar(() => api.usuarios());
  const [perfil, setPerfil] = useState<Perfil | ''>('');
  const [busca, setBusca] = useState('');
  const [novo, setNovo] = useState(false);
  const ehGestor = usuario?.perfil === 'GESTOR';

  const filtrados = useMemo(() => {
    const t = busca.trim().toLowerCase();
    return (dados ?? []).filter((u) => (!perfil || u.perfil === perfil) &&
      (!t || [u.nome, u.matricula, u.email, u.areaNome ?? ''].some((x) => x.toLowerCase().includes(t))));
  }, [dados, perfil, busca]);

  if (carregando && !dados) return <Carregando />;
  if (erro && !dados) return <MensagemErro texto={erro} onTentar={() => recarregar()} />;

  return (
    <>
      <CabecalhoPagina titulo="Usuários" subtitulo="Operadores, supervisores de segurança e gestores com acesso ao SafeField AI (RF01)."
        acoes={ehGestor ? <Botao onClick={() => setNovo(true)}><UserPlus size={16} /> Novo usuário</Botao> : undefined} />
      <div className="mb-4 flex flex-wrap items-center gap-2">
        <Chip ativo={perfil === ''} onClick={() => setPerfil('')} contador={dados?.length}>Todos</Chip>
        {(['OPERADOR', 'SUPERVISOR', 'GESTOR'] as Perfil[]).map((p) => (
          <Chip key={p} ativo={perfil === p} onClick={() => setPerfil(p)} contador={dados?.filter((u) => u.perfil === p).length}>{ROTULO_PERFIL[p]}es</Chip>
        ))}
        <div className="relative ml-auto w-full sm:w-72">
          <Search size={18} className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input className="campo pl-10" placeholder="Buscar" value={busca} onChange={(e) => setBusca(e.target.value)} />
        </div>
      </div>
      <section className="cartao overflow-hidden">
        {filtrados.length === 0 ? <Vazio icone={<Users />} titulo="Nenhum usuário encontrado" /> : (
          <div className="overflow-x-auto">
            <table className="tabela w-full">
              <thead className="bg-slate-50"><tr><th>Nome</th><th>Perfil</th><th>Matrícula</th><th>E-mail</th><th>Área / unidade</th><th>Turno</th></tr></thead>
              <tbody>
                {filtrados.map((u) => (
                  <tr key={u.id}>
                    <td className="font-semibold">{u.nome}{u.funcao && <p className="text-xs font-normal text-slate-500">{u.funcao}</p>}</td>
                    <td><span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${COR[u.perfil]}`}>{ROTULO_PERFIL[u.perfil]}</span></td>
                    <td className="font-mono text-xs">{u.matricula}</td>
                    <td className="text-slate-600">{u.email}</td>
                    <td>{u.areaNome ?? u.unidadeIndustrial ?? '—'}</td>
                    <td>{u.turno ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
      {!ehGestor && <p className="mt-3 text-xs text-slate-500">Somente o gestor industrial cadastra novos usuários.</p>}
      <ModalUsuario aberto={novo} onFechar={() => setNovo(false)}
        onSalvo={(nome) => { setNovo(false); recarregar(true); avisar({ tipo: 'sucesso', titulo: 'Usuário cadastrado', texto: `${nome} já pode entrar no sistema.` }); }} />
    </>
  );
}

function ModalUsuario({ aberto, onFechar, onSalvo }: { aberto: boolean; onFechar: () => void; onSalvo: (nome: string) => void }) {
  const { dados: areas } = useCarregar(api.areas);
  const [f, setF] = useState({ nome: '', matricula: '', email: '', senha: '', perfil: 'OPERADOR' as Perfil, turno: 'A' as Turno, funcao: '', areaId: '' as number | '', unidadeIndustrial: '' });
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);
  const salvar = async () => {
    setErro('');
    if (!f.nome || !f.matricula || !f.email || f.senha.length < 6) return setErro('Preencha nome, matrícula, e-mail e uma senha de 6+ caracteres.');
    if (f.perfil === 'OPERADOR' && !f.areaId) return setErro('Operador precisa de área.');
    setSalvando(true);
    try {
      await api.criarUsuario({
        nome: f.nome, matricula: f.matricula, email: f.email, senha: f.senha, perfil: f.perfil,
        turno: f.perfil === 'OPERADOR' ? f.turno : null, funcao: f.funcao || undefined,
        areaId: f.perfil === 'OPERADOR' ? Number(f.areaId) : null, unidadeIndustrial: f.unidadeIndustrial || undefined,
      });
      onSalvo(f.nome);
      setF({ ...f, nome: '', matricula: '', email: '', senha: '', funcao: '' });
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Erro ao salvar.');
    } finally {
      setSalvando(false);
    }
  };
  return (
    <Modal aberto={aberto} titulo="Novo usuário" onFechar={onFechar}>
      <div className="space-y-4">
        <div className="flex gap-2">
          {(['OPERADOR', 'SUPERVISOR', 'GESTOR'] as Perfil[]).map((p) => <Chip key={p} ativo={f.perfil === p} onClick={() => setF({ ...f, perfil: p })}>{ROTULO_PERFIL[p]}</Chip>)}
        </div>
        <Campo rotulo="Nome completo"><input className="campo" value={f.nome} onChange={(e) => setF({ ...f, nome: e.target.value })} /></Campo>
        <div className="grid grid-cols-2 gap-3">
          <Campo rotulo="Matrícula"><input className="campo" value={f.matricula} onChange={(e) => setF({ ...f, matricula: e.target.value })} placeholder="OP-2001" /></Campo>
          <Campo rotulo="Senha inicial"><input className="campo" type="password" value={f.senha} onChange={(e) => setF({ ...f, senha: e.target.value })} /></Campo>
        </div>
        <Campo rotulo="E-mail"><input className="campo" type="email" value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></Campo>
        {f.perfil === 'OPERADOR' && (
          <>
            <div className="grid grid-cols-2 gap-3">
              <Campo rotulo="Área">
                <select className="campo" value={f.areaId} onChange={(e) => setF({ ...f, areaId: e.target.value ? Number(e.target.value) : '' })}>
                  <option value="">Selecione…</option>{areas?.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
                </select>
              </Campo>
              <Campo rotulo="Turno">
                <select className="campo" value={f.turno} onChange={(e) => setF({ ...f, turno: e.target.value as Turno })}>
                  {(['A', 'B', 'C'] as Turno[]).map((t) => <option key={t} value={t}>{ROTULO_TURNO[t]}</option>)}
                </select>
              </Campo>
            </div>
            <Campo rotulo="Função"><input className="campo" value={f.funcao} onChange={(e) => setF({ ...f, funcao: e.target.value })} placeholder="Ex.: Soldador" /></Campo>
          </>
        )}
        {f.perfil === 'GESTOR' && (
          <Campo rotulo="Unidade industrial"><input className="campo" value={f.unidadeIndustrial} onChange={(e) => setF({ ...f, unidadeIndustrial: e.target.value })} /></Campo>
        )}
        {erro && <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{erro}</p>}
        <div className="flex justify-end gap-2">
          <Botao variante="fantasma" onClick={onFechar}>Cancelar</Botao>
          <Botao onClick={salvar} carregando={salvando}>Cadastrar</Botao>
        </div>
      </div>
    </Modal>
  );
}
