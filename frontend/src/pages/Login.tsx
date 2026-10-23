import { ArrowRight, Eye, HardHat, Radio, ShieldCheck } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/Autenticacao';
import { Logo } from '../components/Layout';
import { Botao, Campo } from '../components/ui';

const CONTAS = [
  { email: 'supervisor@metaindustria.com.br', nome: 'Renata Oliveira', perfil: 'Supervisora de segurança', descricao: 'Acompanha alertas, valida e escala ocorrências.' },
  { email: 'gestor@metaindustria.com.br', nome: 'Marcelo Torres', perfil: 'Gestor industrial', descricao: 'Indicadores, relatórios e cadastros.' },
  { email: 'operador@metaindustria.com.br', nome: 'Marcos Lima', perfil: 'Operador · Montagem Final', descricao: 'Recebe alertas e confirma a regularização.' },
];
const SENHA_DEMO = 'safefield123';

export default function Login() {
  const { entrar } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  const enviar = async (e?: FormEvent, credenciais?: { email: string; senha: string }) => {
    e?.preventDefault();
    const dados = credenciais ?? { email, senha };
    setErro('');
    if (!dados.email || !dados.senha) return setErro('Informe e-mail e senha.');
    setCarregando(true);
    try {
      const u = await entrar(dados.email.trim(), dados.senha);
      navigate(u.perfil === 'OPERADOR' ? '/minha-area' : '/');
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Não foi possível entrar.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="grid min-h-screen lg:grid-cols-[1.1fr_1fr]">
      <section className="relative hidden overflow-hidden bg-painel-900 p-12 text-white lg:flex lg:flex-col">
        <Logo />
        <div className="my-auto max-w-lg">
          <p className="mb-4 inline-flex items-center gap-2 rounded-full bg-painel-700 px-3 py-1 text-xs font-semibold text-marca-400">
            Challenge FIAP × SPI Integração · Metaindústria
          </p>
          <h1 className="text-4xl font-extrabold leading-tight tracking-tight">
            Segurança do trabalho <span className="text-marca-500">proativa</span>, não punitiva.
          </h1>
          <p className="mt-4 text-lg text-slate-300">
            Visão computacional e sensores identificam a falta de EPI em campo, alertam operador e supervisor na hora
            e transformam cada ocorrência em dado para prevenir a próxima.
          </p>
          <ul className="mt-10 space-y-4 text-slate-200">
            <li className="flex gap-3"><Eye className="mt-0.5 shrink-0 text-marca-500" size={20} /> Detecção de capacete, óculos, luvas, protetor auricular e mais, por área e turno.</li>
            <li className="flex gap-3"><Radio className="mt-0.5 shrink-0 text-marca-500" size={20} /> Alerta em tempo real para o operador e o supervisor responsável.</li>
            <li className="flex gap-3"><ShieldCheck className="mt-0.5 shrink-0 text-marca-500" size={20} /> Validação humana, trilha de auditoria e relatórios de conformidade.</li>
          </ul>
        </div>
        <HardHat className="pointer-events-none absolute -bottom-10 -right-10 text-painel-800" size={280} strokeWidth={1} />
      </section>

      <section className="flex items-center justify-center px-5 py-10">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden"><Logo claro={false} /></div>
          <h2 className="text-2xl font-extrabold tracking-tight">Entrar no SafeField AI</h2>
          <p className="mt-1 text-sm text-slate-500">Use sua conta corporativa ou uma das contas de demonstração.</p>

          <form onSubmit={enviar} className="mt-6 space-y-4">
            <Campo rotulo="E-mail">
              <input className="campo min-h-[48px]" type="email" autoComplete="username" value={email}
                onChange={(e) => setEmail(e.target.value)} placeholder="nome@metaindustria.com.br" />
            </Campo>
            <Campo rotulo="Senha">
              <input className="campo min-h-[48px]" type="password" autoComplete="current-password" value={senha}
                onChange={(e) => setSenha(e.target.value)} placeholder="••••••••" />
            </Campo>
            {erro && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">{erro}</p>}
            <Botao type="submit" grande carregando={carregando} className="w-full">Entrar <ArrowRight size={18} /></Botao>
          </form>

          <div className="mt-8">
            <p className="rotulo mb-3">Contas de demonstração · senha {SENHA_DEMO}</p>
            <div className="space-y-2">
              {CONTAS.map((c) => (
                <button
                  key={c.email}
                  type="button"
                  disabled={carregando}
                  onClick={() => { setEmail(c.email); setSenha(SENHA_DEMO); enviar(undefined, { email: c.email, senha: SENHA_DEMO }); }}
                  className="cartao flex w-full items-center gap-3 p-3.5 text-left transition hover:ring-marca-500 disabled:opacity-60"
                >
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-marca-100 font-bold text-marca-600">
                    {c.nome.split(' ').map((p) => p[0]).slice(0, 2).join('')}
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-bold">{c.nome} <span className="font-medium text-slate-500">· {c.perfil}</span></p>
                    <p className="truncate text-xs text-slate-500">{c.descricao}</p>
                  </div>
                  <ArrowRight size={16} className="text-slate-400" />
                </button>
              ))}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
