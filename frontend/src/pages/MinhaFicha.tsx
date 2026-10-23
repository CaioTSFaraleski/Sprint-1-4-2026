import { useAuth } from '../auth/Autenticacao';
import { CabecalhoPagina } from '../components/ui';
import { FichaColaborador } from './Colaboradores';

export default function MinhaFicha() {
  const { usuario } = useAuth();
  if (!usuario) return null;
  return (
    <div className="mx-auto max-w-4xl">
      <CabecalhoPagina titulo="Meus EPIs" subtitulo="EPIs obrigatórios da sua área, fichas de entrega e seu histórico." />
      <FichaColaborador id={usuario.id} modoOperador />
    </div>
  );
}
