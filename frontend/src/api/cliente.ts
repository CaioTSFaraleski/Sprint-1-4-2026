import type { Erro } from './tipos';

const CHAVE_SESSAO = 'safefield.sessao';

export class ApiErro extends Error {
  constructor(public status: number, mensagem: string, public detalhes: string[] = []) {
    super(mensagem);
  }
}

export function tokenAtual(): string | null {
  try {
    const bruto = localStorage.getItem(CHAVE_SESSAO);
    return bruto ? (JSON.parse(bruto).token as string) : null;
  } catch {
    return null;
  }
}

export function salvarSessao(valor: unknown | null) {
  try {
    if (valor === null) localStorage.removeItem(CHAVE_SESSAO);
    else localStorage.setItem(CHAVE_SESSAO, JSON.stringify(valor));
  } catch {
    /* navegação privada: a sessão vale só enquanto a aba estiver aberta */
  }
}

export function lerSessao<T>(): T | null {
  try {
    const bruto = localStorage.getItem(CHAVE_SESSAO);
    return bruto ? (JSON.parse(bruto) as T) : null;
  } catch {
    return null;
  }
}

let aoExpirar: (() => void) | null = null;
export function quandoSessaoExpirar(fn: () => void) {
  aoExpirar = fn;
}

export async function requisicao<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  const token = tokenAtual();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (corpo !== undefined) headers['Content-Type'] = 'application/json';

  let resposta: Response;
  try {
    resposta = await fetch(caminho, {
      method: metodo,
      headers,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ApiErro(0, 'Não foi possível falar com a API. Confira se o backend está rodando.');
  }

  if (resposta.status === 401 && token && !caminho.endsWith('/auth/login')) {
    aoExpirar?.();
  }
  if (!resposta.ok) {
    let erro: Partial<Erro> = {};
    try {
      erro = await resposta.json();
    } catch {
      /* corpo vazio */
    }
    throw new ApiErro(resposta.status, erro.mensagem ?? `Erro ${resposta.status}`, erro.detalhes ?? []);
  }
  if (resposta.status === 204) return undefined as T;
  return (await resposta.json()) as T;
}

/** Baixa um arquivo autenticado (ex.: CSV do relatório). */
export async function baixar(caminho: string, nomePadrao: string) {
  const token = tokenAtual();
  const resposta = await fetch(caminho, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
  if (!resposta.ok) throw new ApiErro(resposta.status, 'Não foi possível gerar o arquivo.');
  const blob = await resposta.blob();
  const disposicao = resposta.headers.get('Content-Disposition') ?? '';
  const nome = /filename="([^"]+)"/.exec(disposicao)?.[1] ?? nomePadrao;
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = nome;
  a.click();
  URL.revokeObjectURL(url);
}
