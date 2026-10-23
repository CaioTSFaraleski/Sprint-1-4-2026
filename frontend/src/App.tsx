import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import type { ReactNode } from 'react';
import type { Perfil } from './api/tipos';
import { ProvedorAutenticacao, useAuth } from './auth/Autenticacao';
import { ProvedorAvisos } from './components/Avisos';
import Layout from './components/Layout';
import { ProvedorTempoReal } from './realtime/TempoReal';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Ocorrencias from './pages/Ocorrencias';
import OcorrenciaDetalhe from './pages/OcorrenciaDetalhe';
import Colaboradores from './pages/Colaboradores';
import NovoEpi from './pages/NovoEpi';
import Areas from './pages/Areas';
import Auditoria from './pages/Auditoria';
import Relatorios from './pages/Relatorios';
import Usuarios from './pages/Usuarios';
import MinhaArea from './pages/MinhaArea';
import MinhaFicha from './pages/MinhaFicha';

function Protegida({ perfis, children }: { perfis?: Perfil[]; children: ReactNode }) {
  const { usuario } = useAuth();
  if (!usuario) return <Navigate to="/login" replace />;
  if (perfis && !perfis.includes(usuario.perfil)) return <Navigate to={inicioDo(usuario.perfil)} replace />;
  return <>{children}</>;
}

const inicioDo = (perfil: Perfil) => (perfil === 'OPERADOR' ? '/minha-area' : '/');
const GESTAO: Perfil[] = ['SUPERVISOR', 'GESTOR'];

function Rotas() {
  const { usuario } = useAuth();
  return (
    <ProvedorTempoReal usuario={usuario}>
      <Routes>
        <Route path="/login" element={usuario ? <Navigate to={inicioDo(usuario.perfil)} replace /> : <Login />} />
        <Route element={<Protegida><Layout /></Protegida>}>
          <Route path="/" element={<Protegida perfis={GESTAO}><Dashboard /></Protegida>} />
          <Route path="/ocorrencias" element={<Protegida perfis={GESTAO}><Ocorrencias /></Protegida>} />
          <Route path="/ocorrencias/:id" element={<OcorrenciaDetalhe />} />
          <Route path="/colaboradores" element={<Protegida perfis={GESTAO}><Colaboradores /></Protegida>} />
          <Route path="/epis/novo" element={<Protegida perfis={GESTAO}><NovoEpi /></Protegida>} />
          <Route path="/areas" element={<Protegida perfis={GESTAO}><Areas /></Protegida>} />
          <Route path="/auditoria" element={<Protegida perfis={GESTAO}><Auditoria /></Protegida>} />
          <Route path="/relatorios" element={<Protegida perfis={GESTAO}><Relatorios /></Protegida>} />
          <Route path="/usuarios" element={<Protegida perfis={GESTAO}><Usuarios /></Protegida>} />
          <Route path="/minha-area" element={<Protegida perfis={['OPERADOR']}><MinhaArea /></Protegida>} />
          <Route path="/minha-ficha" element={<Protegida perfis={['OPERADOR']}><MinhaFicha /></Protegida>} />
        </Route>
        <Route path="*" element={<Navigate to={usuario ? inicioDo(usuario.perfil) : '/login'} replace />} />
      </Routes>
    </ProvedorTempoReal>
  );
}

export default function App() {
  return (
    <BrowserRouter basename={import.meta.env.BASE_URL}>
      <ProvedorAvisos>
        <ProvedorAutenticacao>
          <Rotas />
        </ProvedorAutenticacao>
      </ProvedorAvisos>
    </BrowserRouter>
  );
}
