import { useMemo, useState, useEffect } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useSearchParams, useNavigate } from "react-router-dom";
import { api } from "@/lib/api";
import type { ApiResponse, PageResponse, Ngo, NgoCategory } from "@/types";
import { toast } from "sonner";
import Button from "@/components/ui/Button";
import { useAuth } from "@/hooks/useAuth";
import type { FormEvent } from "react";

export default function VolunteerPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const isVolunteer = user?.roles.includes("VOLUNTEER");
  const [params, setParams] = useSearchParams();
  const [search, setSearch] = useState(params.get("search") ?? "");
  const [city, setCity] = useState(params.get("city") ?? "");
  const [regionState, setRegionState] = useState(params.get("state") ?? "");
  const categoryId = params.get("categoryId") ?? "";
  const page = Number(params.get("page") ?? "0");

  const [selectedNgo, setSelectedNgo] = useState<Ngo | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [authPromptNgo, setAuthPromptNgo] = useState<Ngo | null>(null);
  const [volName, setVolName] = useState("");
  const [volEmail, setVolEmail] = useState("");
  const [volPhone, setVolPhone] = useState("");
  const [volMessage, setVolMessage] = useState("");

  useEffect(() => {
    if (isModalOpen) {
      setVolName(user?.name ?? "");
      setVolEmail(user?.email ?? "");
      setVolPhone(user?.phone ?? "");
    }
  }, [isModalOpen, user]);

  const queryParams = useMemo(() => {
    const p = new URLSearchParams();
    p.set("page", String(page));
    p.set("size", "20");
    p.set("allowVolunteers", "true");
    if (search.trim()) p.set("search", search.trim());
    if (city.trim()) p.set("city", city.trim());
    if (regionState.trim()) p.set("state", regionState.trim());
    if (categoryId) p.set("categoryId", categoryId);
    return p.toString();
  }, [page, search, city, regionState, categoryId]);

  const { data, isLoading } = useQuery({
    queryKey: ["volunteer-ngos", queryParams],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>(`/ngos?${queryParams}`).then(r => r.data.data),
  });

  const { data: categories } = useQuery({
    queryKey: ["ngo-categories"],
    queryFn: () => api.get<ApiResponse<NgoCategory[]>>("/ngos/categories").then(r => r.data.data),
  });

  const applyFilters = () => {
    const next = new URLSearchParams(params);
    next.set("page", "0");
    if (search.trim()) next.set("search", search.trim()); else next.delete("search");
    if (city.trim()) next.set("city", city.trim()); else next.delete("city");
    if (regionState.trim()) next.set("state", regionState.trim()); else next.delete("state");
    if (categoryId) next.set("categoryId", categoryId); else next.delete("categoryId");
    setParams(next);
  };

  const gotoPage = (nextPage: number) => {
    const next = new URLSearchParams(params);
    next.set("page", String(nextPage));
    setParams(next);
  };

  const applyMutation = useMutation({
    mutationFn: async () => {
      if (!selectedNgo) throw new Error("Selecione uma ONG.");
      return api.post(`/ngos/${selectedNgo.id}/volunteers`, {
        name: volName,
        email: volEmail,
        phone: volPhone,
        message: volMessage,
      });
    },
    onSuccess: () => {
      toast.success("Inscricao enviada.");
      setIsModalOpen(false);
      setVolMessage("");
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? e.message ?? "Erro ao enviar inscricao"),
  });

  const handleVolunteerSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!volName.trim() || !volEmail.trim() || !volPhone.trim()) {
      toast.error("Preencha nome, email e telefone.");
      return;
    }
    applyMutation.mutate();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6">Voluntariado</h1>

      <div className="bg-white rounded-xl shadow-sm p-4 mb-5 grid md:grid-cols-5 gap-3">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Buscar por nome"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        />
        <input
          value={city}
          onChange={(e) => setCity(e.target.value)}
          placeholder="Cidade"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        />
        <input
          value={regionState}
          onChange={(e) => setRegionState(e.target.value)}
          placeholder="Estado"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        />
        <select
          value={categoryId}
          onChange={(e) => {
            const next = new URLSearchParams(params);
            if (e.target.value) next.set("categoryId", e.target.value); else next.delete("categoryId");
            setParams(next);
          }}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="">Todas as areas</option>
          {categories?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <button onClick={applyFilters} className="bg-primary-600 text-white rounded-md px-3 py-2 text-sm">Aplicar filtros</button>
      </div>

      <div className="bg-white rounded-xl shadow-sm p-4">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-slate-500">
                <th className="py-2">ONG</th>
                <th className="py-2">Cidade/Estado</th>
                <th className="py-2">Area de atuacao</th>
                <th className="py-2">Vagas</th>
                <th className="py-2">Acao</th>
              </tr>
            </thead>
            <tbody>
              {isLoading && (
                <tr>
                  <td colSpan={5} className="py-4 text-slate-500">Carregando…</td>
                </tr>
              )}
              {!isLoading && !data?.content?.length && (
                <tr>
                  <td colSpan={5} className="py-4 text-slate-500">Nenhuma ONG com voluntariado no momento.</td>
                </tr>
              )}
              {data?.content?.map((ngo) => (
                <tr key={ngo.id} className="border-t border-slate-100">
                  <td className="py-2 font-medium">{ngo.name}</td>
                  <td className="py-2">{ngo.city ? `${ngo.city}/${ngo.state}` : "-"}</td>
                  <td className="py-2">{ngo.categoryName || "-"}</td>
                  <td className="py-2">{ngo.volunteerSlots ?? 0}</td>
                  <td className="py-2">
                    <Button
                      variant="outline"
                      onClick={() => {
                        if (!isVolunteer) {
                           setAuthPromptNgo(ngo);
                           return;
                         }
                         setSelectedNgo(ngo);
                         setIsModalOpen(true);
                       }}
                     >
                       Candidatar-se
                     </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <button
            onClick={() => gotoPage(Math.max(0, page - 1))}
            disabled={page === 0}
            className="px-3 py-2 text-sm rounded-md border border-slate-300 disabled:opacity-50"
          >
            Anterior
          </button>
          <button
            onClick={() => gotoPage(page + 1)}
            disabled={data ? page + 1 >= data.totalPages : true}
            className="px-3 py-2 text-sm rounded-md border border-slate-300 disabled:opacity-50"
          >
            Proxima
          </button>
        </div>
      </div>

      {isModalOpen && selectedNgo && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-lg rounded-xl bg-white p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold">Candidatar-se: {selectedNgo.name}</h2>
              <button
                type="button"
                className="text-slate-500 hover:text-slate-700"
                onClick={() => setIsModalOpen(false)}
              >
                Fechar
              </button>
            </div>
            <form className="space-y-3" onSubmit={handleVolunteerSubmit}>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Nome</label>
                <input
                  value={volName}
                  onChange={(e) => setVolName(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
                <input
                  value={volEmail}
                  onChange={(e) => setVolEmail(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Telefone</label>
                <input
                  value={volPhone}
                  onChange={(e) => setVolPhone(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Mensagem</label>
                <textarea
                  value={volMessage}
                  onChange={(e) => setVolMessage(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm min-h-24"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>Cancelar</Button>
                <Button type="submit" loading={applyMutation.isPending}>Enviar</Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {authPromptNgo && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-lg">
            <h2 className="text-lg font-semibold mb-2">Criar conta para se candidatar</h2>
            <p className="text-sm text-slate-600">
              Para se candidatar em <span className="font-medium">{authPromptNgo.name}</span>, entre com uma conta de voluntário.
            </p>
            <div className="mt-4 flex justify-end gap-2">
              <Button variant="outline" onClick={() => setAuthPromptNgo(null)}>Cancelar</Button>
              <Button
                variant="outline"
                onClick={() => {
                  setAuthPromptNgo(null);
                  navigate("/cadastro?redirect=/voluntariado&msg=Crie sua conta de voluntario");
                }}
              >
                Criar conta
              </Button>
              <Button
                onClick={() => {
                  setAuthPromptNgo(null);
                  navigate("/login?redirect=/voluntariado&msg=Entre como voluntario para se candidatar");
                }}
              >
                Ir para login
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
