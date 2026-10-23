import { useCallback, useEffect, useRef, useState } from 'react';

/** Carrega dados assíncronos com estados de carregando/erro e função para recarregar. */
export function useCarregar<T>(fn: () => Promise<T>, deps: unknown[] = []) {
  const [dados, setDados] = useState<T | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);
  const fnRef = useRef(fn);
  fnRef.current = fn;
  const versao = useRef(0);

  const recarregar = useCallback(async (silencioso = false) => {
    const minha = ++versao.current;
    if (!silencioso) setCarregando(true);
    try {
      const r = await fnRef.current();
      if (minha === versao.current) {
        setDados(r);
        setErro(null);
      }
    } catch (e) {
      if (minha === versao.current) setErro(e instanceof Error ? e.message : 'Erro inesperado');
    } finally {
      if (minha === versao.current) setCarregando(false);
    }
  }, []);

  useEffect(() => {
    recarregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { dados, erro, carregando, recarregar, setDados };
}
