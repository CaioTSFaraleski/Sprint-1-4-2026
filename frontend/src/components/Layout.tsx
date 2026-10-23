import {
  Bell, ClipboardList, FileBarChart2, HardHat, LayoutDashboard, LogOut, Map, Menu, ScrollText, ShieldAlert,
  UserRound, Users, X,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { Perfil } from '../api/tipos';
import { useAuth } from '../auth/Autenticacao';
import { ROTULO_PERFIL, ROTULO_RISCO } from '../lib/formato';
import { useEventos, useTempoReal } from '../realtime/TempoReal';
import { useAvisos } from './Avisos';

interface ItemMenu {
  para: string;
  rotulo: string;
  icone: typeof LayoutDashboard;
  perfis: Perfil[];
}

const MENU: ItemMenu[] = [
  { para: '/', rotulo: 'Dashboard', icone: LayoutDashboard, perfis: ['SUPERVISOR', 'GESTOR'] },
  { para: '/minha-area', rotulo: 'Meus alertas', icone: ShieldAlert, perfis: ['OPERADOR'] },
  { para: '/minha-ficha', rotulo: 'Meus EPIs', icone: HardHat, perfis: ['OPERADOR'] },
  { para: '/ocorrencias', rotulo: 'Alertas e Ocorrências', icone: Bell, perfis: ['SUPERVISOR', 'GESTOR'] },
  { para: '/colaboradores', rotulo: 'Colaboradores e EPIs', icone: Users, perfis: ['SUPERVISOR', 'GESTOR'] },
  { para: '/areas', rotulo: 'Áreas e Regras', icone: Map, perfis: ['SUPERVISOR', 'GESTOR'] },
  { para: '/relatorios', rotulo: 'Relatórios', icone: FileBarChart2, perfis: ['SUPERVISOR', 'GESTOR'] },
  { para: '/auditoria', rotulo: 'Auditoria', icone: ScrollText, perfis: ['SUPERVISOR', 'GESTOR'] },
  { para: '/usuarios', rotulo: 'Usuários', icone: ClipboardList, perfis: ['SUPERVISOR', 'GESTOR'] },
];

export function Logo({ claro = true }: { claro?: boolean }) {
  return (
    <div className="flex items-center gap-2.5">
      <svg viewBox="0 0 64 64" className="h-9 w-9 shrink-0" aria-hidden>
        <rect width="64" height="64" rx="14" fill={claro ? '#1B2640' : '#0B1220'} />
        <path d="M32 10 50 17v13c0 11.5-7.6 20.6-18 24-10.4-3.4-18-12.5-18-24V17z" fill="#F59E0B" />
        <path d="m24 32 6 6 11-12" fill="none" stroke="#0B1220" strokeWidth="5" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      <div className="leading-tight">
        <p className={`text-lg font-extrabold tracking-tight ${claro ? 'text-white' : 'text-slate-900'}`}>
          SafeField <span className="text-marca-500">AI</span>
        </p>
        <p className={`text-[11px] font-medium ${claro ? 'text-slate-400' : 'text-slate-500'}`}>Metaindústria</p>
      </div>
    </div>
  );
}

export default function Layout() {
  const { usuario, sair } = useAuth();
  const { conectado } = useTempoReal();
  const avisar = useAvisos();
  const navigate = useNavigate();
  const local = useLocation();
  const [menuAberto, setMenuAberto] = useState(false);
  const [naoLidos, setNaoLidos] = useState(0);

  const atualizarAlertas = () => api.alertas(false).then((l) => setNaoLidos(l.length)).catch(() => {});
  useEffect(() => {
    atualizarAlertas();
  }, [local.pathname]);
  useEffect(() => setMenuAberto(false), [local.pathname]);

  useEventos((e) => {
    if (e.tipo !== 'OCORRENCIA_ABERTA' || !e.ocorrencia) return;
    const o = e.ocorrencia;
    atualizarAlertas();
    const epis = o.episAusentes.map((x) => x.tipoDescricao.toLowerCase()).join(', ');
    if (usuario?.perfil === 'OPERADOR') {
      navigator.vibrate?.([200, 100, 200]);
      avisar({ tipo: 'alerta', titulo: 'Atenção ao seu EPI', texto: `Coloque: ${epis}.`, acao: { rotulo: 'Ver alerta', onClick: () => navigate('/minha-area') } });
    } else {
      avisar({
        tipo: 'alerta',
        titulo: `Nova ocorrência ${o.codigo} · ${ROTULO_RISCO[o.severidade]}`,
        texto: `${o.operadorNome} sem ${epis} em ${o.areaNome}.`,
        acao: { rotulo: 'Abrir ocorrência', onClick: () => navigate(`/ocorrencias/${o.id}`) },
      });
    }
  });

  if (!usuario) return null;
  const itens = MENU.filter((m) => m.perfis.includes(usuario.perfil));

  const menu = (
    <nav className="flex flex-col gap-1">
      {itens.map(({ para, rotulo, icone: Icone }) => (
        <NavLink
          key={para}
          to={para}
          end={para === '/'}
          className={({ isActive }) =>
            `flex min-h-[46px] items-center gap-3 rounded-xl px-3.5 text-sm font-semibold transition ${
              isActive ? 'bg-marca-500 text-painel-900' : 'text-slate-300 hover:bg-painel-700 hover:text-white'
            }`
          }
        >
          <Icone size={19} />
          {rotulo}
        </NavLink>
      ))}
    </nav>
  );

  const rodapeMenu = (
    <div className="mt-auto space-y-3">
      <div className="flex items-center gap-2 rounded-xl bg-painel-800 px-3 py-2.5 text-xs text-slate-300">
        <span className={`h-2.5 w-2.5 rounded-full ${conectado ? 'pulsar bg-emerald-400' : 'bg-slate-500'}`} />
        {conectado ? 'Monitoramento em tempo real' : 'Reconectando ao tempo real…'}
      </div>
      <div className="flex items-center gap-3 rounded-xl bg-painel-800 p-3">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-painel-600 text-white">
          <UserRound size={18} />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold text-white">{usuario.nome}</p>
          <p className="truncate text-xs text-slate-400">
            {ROTULO_PERFIL[usuario.perfil]}{usuario.areaNome ? ` · ${usuario.areaNome}` : ''}
          </p>
        </div>
        <button onClick={sair} className="rounded-lg p-2 text-slate-400 hover:bg-painel-700 hover:text-white" title="Sair" aria-label="Sair">
          <LogOut size={18} />
        </button>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen lg:flex">
      {/* Menu lateral (desktop e tablet na horizontal) */}
      <aside className="nao-imprimir sticky top-0 hidden h-screen w-72 shrink-0 flex-col gap-8 bg-painel-900 p-5 lg:flex">
        <Link to="/"><Logo /></Link>
        {menu}
        {rodapeMenu}
      </aside>

      {/* Menu em gaveta (celular e tablet na vertical) */}
      {menuAberto && (
        <div className="fixed inset-0 z-40 bg-slate-900/60 lg:hidden" onClick={() => setMenuAberto(false)}>
          <aside className="flex h-full w-72 flex-col gap-8 bg-painel-900 p-5" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <Logo />
              <button onClick={() => setMenuAberto(false)} className="p-2 text-slate-300" aria-label="Fechar menu"><X /></button>
            </div>
            {menu}
            {rodapeMenu}
          </aside>
        </div>
      )}

      <div className="min-w-0 flex-1">
        <header className="nao-imprimir sticky top-0 z-30 flex items-center gap-3 border-b border-slate-200 bg-white/90 px-4 py-3 backdrop-blur lg:px-8">
          <button onClick={() => setMenuAberto(true)} className="rounded-lg p-2 text-slate-700 hover:bg-slate-100 lg:hidden" aria-label="Abrir menu">
            <Menu />
          </button>
          <div className="lg:hidden"><Logo claro={false} /></div>
          <div className="ml-auto flex items-center gap-2">
            <span className={`hidden items-center gap-2 rounded-full px-3 py-1.5 text-xs font-semibold sm:flex ${
              conectado ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'}`}>
              <span className={`h-2 w-2 rounded-full ${conectado ? 'pulsar bg-emerald-500' : 'bg-slate-400'}`} />
              {conectado ? 'Ao vivo' : 'Offline'}
            </span>
            <button
              onClick={() => navigate(usuario.perfil === 'OPERADOR' ? '/minha-area' : '/ocorrencias')}
              className="relative rounded-xl p-2.5 text-slate-700 hover:bg-slate-100"
              aria-label={`${naoLidos} alertas não lidos`}
              title="Alertas"
            >
              <Bell size={20} />
              {naoLidos > 0 && (
                <span className="absolute -right-0.5 -top-0.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-[11px] font-bold text-white">
                  {naoLidos > 99 ? '99+' : naoLidos}
                </span>
              )}
            </button>
          </div>
        </header>
        <main className="mx-auto max-w-7xl px-4 py-6 lg:px-8 lg:py-8">
          <div key={local.pathname} className="animar-entrada">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
