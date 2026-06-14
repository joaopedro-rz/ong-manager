import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { api } from "@/lib/api";
import type { AuthData, User } from "@/types";

interface Ctx {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (r: string) => boolean;
}
const AuthCtx = createContext<Ctx>({} as Ctx);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem("user");
    if (stored) setUser(JSON.parse(stored));
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const { data } = await api.post<{ data: AuthData }>("/auth/login", { email, password });
    localStorage.setItem("accessToken", data.data.accessToken);
    localStorage.setItem("refreshToken", data.data.refreshToken);
    localStorage.setItem("user", JSON.stringify(data.data.user));
    setUser(data.data.user);
  };

  const logout = () => { localStorage.clear(); setUser(null); window.location.href = "/"; };
  const hasRole = (r: string) => user?.roles.includes(r) ?? false;

  return <AuthCtx.Provider value={{ user, loading, login, logout, hasRole }}>{children}</AuthCtx.Provider>;
}

export const useAuth = () => useContext(AuthCtx);
