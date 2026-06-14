import { Link } from "react-router-dom";
export default function NotFound() {
  return (
    <div className="max-w-7xl mx-auto px-4 py-20 text-center">
      <h1 className="text-3xl font-bold mb-2">404</h1>
      <p className="text-slate-600 mb-4">Página não encontrada.</p>
      <Link to="/" className="text-primary-600 hover:underline">Voltar para o início</Link>
    </div>
  );
}
