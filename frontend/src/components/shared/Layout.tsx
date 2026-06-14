import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { Heart, LogOut, Menu, X } from "lucide-react";
import { useState } from "react";

export default function Layout() {
  const { user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const isAdmin = user?.roles.includes("ADMIN") ?? false;
  const linkCls = ({ isActive }: { isActive: boolean }) =>
    `px-3 py-2 rounded-md text-sm ${isActive ? "text-primary-600 font-semibold" : "text-slate-600 hover:text-primary-600"}`;
  const closeMenu = () => setMenuOpen(false);
  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b border-slate-200 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 font-display font-bold text-primary-700 text-lg" onClick={closeMenu}>
            <Heart className="text-primary-600" /> ONG Manager
          </Link>
          <nav className="hidden md:flex items-center gap-1">
            {!isAdmin && (
              <>
                <NavLink to="/" className={linkCls} end>Início</NavLink>
                <NavLink to="/campanhas" className={linkCls}>Campanhas</NavLink>
                <NavLink to="/ongs" className={linkCls}>ONGs</NavLink>
                <NavLink to="/voluntariado" className={linkCls}>Voluntariado</NavLink>
                {user?.roles.some((r) => r === "ONG_MANAGER" || r === "ADMIN") && (
                  <NavLink to="/ongs/gestao" className={linkCls}>Gestão ONG</NavLink>
                )}
              </>
            )}
            {isAdmin && (
              <>
                <NavLink to="/dashboard" className={linkCls}>Dashboard</NavLink>
                <NavLink to="/admin/campanhas" className={linkCls}>Admin Campanhas</NavLink>
                <NavLink to="/admin/ongs" className={linkCls}>Moderação ONGs</NavLink>
              </>
            )}
          </nav>
          <div className="flex items-center gap-2">
            {user ? (
              <>
                <span className="text-sm text-slate-600 hidden sm:inline">Olá, {user.name.split(" ")[0]}</span>
                <button onClick={logout} className="inline-flex items-center gap-1 text-sm text-slate-600 hover:text-danger" aria-label="Sair">
                  <LogOut size={16} /> Sair
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm text-slate-600 hover:text-primary-600" onClick={closeMenu}>Entrar</Link>
                <Link to="/cadastro" className="text-sm bg-primary-600 hover:bg-primary-700 text-white px-3 py-2 rounded-md" onClick={closeMenu}>Criar conta</Link>
              </>
            )}
            <button
              type="button"
              className="md:hidden inline-flex items-center justify-center rounded-md border border-slate-200 p-2 text-slate-600"
              aria-label={menuOpen ? "Fechar menu" : "Abrir menu"}
              onClick={() => setMenuOpen((open) => !open)}
            >
              {menuOpen ? <X size={18} /> : <Menu size={18} />}
            </button>
          </div>
        </div>
        {menuOpen && (
          <div className="md:hidden border-t border-slate-200 bg-white">
            <div className="max-w-7xl mx-auto px-4 py-3 flex flex-col gap-1">
              {!isAdmin && (
                <>
                  <NavLink to="/" className={linkCls} end onClick={closeMenu}>Início</NavLink>
                  <NavLink to="/campanhas" className={linkCls} onClick={closeMenu}>Campanhas</NavLink>
                  <NavLink to="/ongs" className={linkCls} onClick={closeMenu}>ONGs</NavLink>
                  <NavLink to="/voluntariado" className={linkCls} onClick={closeMenu}>Voluntariado</NavLink>
                  {user?.roles.some((r) => r === "ONG_MANAGER" || r === "ADMIN") && (
                    <NavLink to="/ongs/gestao" className={linkCls} onClick={closeMenu}>Gestão ONG</NavLink>
                  )}
                </>
              )}
              {isAdmin && (
                <>
                  <NavLink to="/dashboard" className={linkCls} onClick={closeMenu}>Dashboard</NavLink>
                  <NavLink to="/admin/campanhas" className={linkCls} onClick={closeMenu}>Admin Campanhas</NavLink>
                  <NavLink to="/admin/ongs" className={linkCls} onClick={closeMenu}>Moderação ONGs</NavLink>
                </>
              )}
            </div>
          </div>
        )}
      </header>
      <main className="flex-1"><Outlet /></main>
      <footer className="bg-white border-t border-slate-200 py-6 text-center text-sm text-slate-500">
        © {new Date().getFullYear()} ONG Manager — TCC2 UTFPR
      </footer>
    </div>
  );
}
