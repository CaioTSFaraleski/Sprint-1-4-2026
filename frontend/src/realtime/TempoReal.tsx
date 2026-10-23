import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import type { EventoTempoReal, Usuario } from '../api/tipos';

type Ouvinte = (e: EventoTempoReal) => void;

interface ContextoTempoReal {
  conectado: boolean;
  assinar: (fn: Ouvinte) => () => void;
}

const Ctx = createContext<ContextoTempoReal | null>(null);

/**
 * Conexão WebSocket com /ws/alertas (RNF01: alerta no painel em até 3 s).
 * O operador assina só os próprios eventos (?operadorId=); supervisor e gestor recebem tudo.
 * Reconecta sozinho com espera crescente se a API cair (RNF05).
 */
export function ProvedorTempoReal({ usuario, children }: { usuario: Usuario | null; children: ReactNode }) {
  const [conectado, setConectado] = useState(false);
  const ouvintes = useRef(new Set<Ouvinte>());

  useEffect(() => {
    if (!usuario) return;
    let socket: WebSocket | null = null;
    let tentativas = 0;
    let timer: number | undefined;
    let encerrado = false;

    const conectar = () => {
      const protocolo = location.protocol === 'https:' ? 'wss' : 'ws';
      const filtro = usuario.perfil === 'OPERADOR' ? `?operadorId=${usuario.id}` : '';
      socket = new WebSocket(`${protocolo}://${location.host}/ws/alertas${filtro}`);
      socket.onopen = () => {
        tentativas = 0;
        setConectado(true);
      };
      socket.onmessage = (msg) => {
        try {
          const evento = JSON.parse(msg.data) as EventoTempoReal;
          ouvintes.current.forEach((fn) => fn(evento));
        } catch {
          /* mensagem inesperada: ignora */
        }
      };
      socket.onclose = () => {
        setConectado(false);
        if (encerrado) return;
        tentativas += 1;
        timer = window.setTimeout(conectar, Math.min(15000, 1000 * 2 ** Math.min(tentativas, 4)));
      };
    };
    conectar();
    return () => {
      encerrado = true;
      window.clearTimeout(timer);
      socket?.close();
    };
  }, [usuario]);

  const assinar = (fn: Ouvinte) => {
    ouvintes.current.add(fn);
    return () => {
      ouvintes.current.delete(fn);
    };
  };

  return <Ctx.Provider value={{ conectado, assinar }}>{children}</Ctx.Provider>;
}

export function useTempoReal() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useTempoReal fora do ProvedorTempoReal');
  return ctx;
}

/** Executa fn a cada evento recebido (a referência mais recente de fn é sempre usada). */
export function useEventos(fn: Ouvinte) {
  const { assinar } = useTempoReal();
  const ref = useRef(fn);
  ref.current = fn;
  useEffect(() => assinar((e) => ref.current(e)), [assinar]);
}
