import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api } from '../api/endpoints';
import { lerSessao, quandoSessaoExpirar, salvarSessao } from '../api/cliente';
import type { LoginResponse, Usuario } from '../api/tipos';

interface ContextoAuth {
  usuario: Usuario | null;
  entrar: (email: string, senha: string) => Promise<Usuario>;
  sair: () => void;
}

const Ctx = createContext<ContextoAuth | null>(null);

export function ProvedorAutenticacao({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(() => {
    const sessao = lerSessao<LoginResponse>();
    if (!sessao) return null;
    if (new Date(sessao.expiraEm).getTime() < Date.now()) {
      salvarSessao(null);
      return null;
    }
    return sessao.usuario;
  });

  const sair = useCallback(() => {
    salvarSessao(null);
    setUsuario(null);
  }, []);

  useEffect(() => quandoSessaoExpirar(sair), [sair]);

  const entrar = useCallback(async (email: string, senha: string) => {
    const resposta = await api.login(email, senha);
    salvarSessao(resposta);
    setUsuario(resposta.usuario);
    return resposta.usuario;
  }, []);

  const valor = useMemo(() => ({ usuario, entrar, sair }), [usuario, entrar, sair]);
  return <Ctx.Provider value={valor}>{children}</Ctx.Provider>;
}

export function useAuth() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useAuth fora do ProvedorAutenticacao');
  return ctx;
}
