import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { api } from "@/lib/api";
import { fmtBRL } from "@/lib/utils";
import type { ApiResponse, PageResponse, Campaign, Ngo, NgoCategory } from "@/types";
import { useAuth } from "@/hooks/useAuth";
import defaultImage from "@/assets/images/img.png";

export default function CampaignsPage() {
  const { user } = useAuth();
  const isManager = user?.roles.some((r) => r === "ONG_MANAGER" || r === "ADMIN");
  const isAdmin = user?.roles.includes("ADMIN");

  const [q, setQ] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [city, setCity] = useState("");
  const [state, setState] = useState("");
  const [urgent, setUrgent] = useState("");
  const [page, setPage] = useState(0);

  const query = useMemo(() => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", "9");
    params.set("status", "ACTIVE");
    if (q) params.set("q", q);
    if (categoryId) params.set("categoryId", categoryId);
    if (city) params.set("city", city);
    if (state) params.set("state", state);
    if (urgent) params.set("urgent", urgent);
    return params.toString();
  }, [q, categoryId, city, state, urgent, page]);

  const { data, isLoading, isError } = useQuery({
    queryKey: ["campaigns", query],
    queryFn: () => api.get<ApiResponse<PageResponse<Campaign>>>(`/campaigns/public?${query}`).then(r => r.data.data),
    retry: false,
  });
  const { data: categories } = useQuery({
    queryKey: ["ngo-categories"],
    queryFn: () => api.get<ApiResponse<NgoCategory[]>>("/ngos/categories").then((r) => r.data.data),
  });
  const { data: myNgos } = useQuery({
    queryKey: ["my-ngos-for-campaigns-list"],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>("/ngos/me?page=0&size=1").then((r) => r.data.data),
    enabled: Boolean(isManager),
  });
  const myNgoId = myNgos?.content?.[0]?.id ?? null;
  const { data: myCampaigns } = useQuery({
    queryKey: ["my-campaigns-summary", myNgoId],
    queryFn: () => api.get<ApiResponse<PageResponse<Campaign>>>(`/campaigns/public?ngoId=${myNgoId}&page=0&size=1`).then((r) => r.data.data),
    enabled: Boolean(isManager && myNgoId),
  });
  const hasCampaigns = (myCampaigns?.totalElements ?? 0) > 0;

  const { data: activeNgos, isLoading: isActiveNgosLoading } = useQuery({
    queryKey: ["public-ngos-active-ids"],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>("/ngos/public?page=0&size=1000").then((r) => r.data.data.content),
    enabled: !isAdmin,
  });
  const activeNgoIds = useMemo(() => new Set((activeNgos ?? []).map((n) => n.id)), [activeNgos]);
  const filteredCampaigns = useMemo(() => {
    if (!data?.content) return [] as Campaign[];
    if (isAdmin || !activeNgos) return data.content;
    return data.content.filter((c) => activeNgoIds.has(c.ngoId));
  }, [data?.content, isAdmin, activeNgos, activeNgoIds]);

  return (
    <div className="max-w-7xl mx-auto px-4 py-10">
      <div className="flex flex-wrap justify-between gap-3 mb-6">
        <h1 className="text-3xl font-bold">Campanhas</h1>
        {isManager && (
          <div className="flex flex-wrap items-center gap-2">
            <Link to="/campanhas/criar" className="text-sm bg-primary-600 hover:bg-primary-700 text-white px-3 py-2 rounded-md">
              Criar campanha
            </Link>
            {hasCampaigns ? (
              <Link to="/campanhas/gestao" className="text-sm bg-primary-600 hover:bg-primary-700 text-white px-3 py-2 rounded-md">
                Gerenciar campanhas
              </Link>
            ) : (
              <button disabled className="text-sm bg-slate-300 text-slate-600 px-3 py-2 rounded-md cursor-not-allowed">
                Gerenciar campanhas
              </button>
            )}
          </div>
        )}
      </div>
      <div className="bg-white rounded-xl shadow-sm p-4 mb-5 grid md:grid-cols-6 gap-3">
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Busca textual" className="rounded-md border border-slate-300 px-3 py-2 text-sm" />
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} className="rounded-md border border-slate-300 px-3 py-2 text-sm">
          <option value="">Todas as áreas</option>
          {categories?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <input value={city} onChange={(e) => setCity(e.target.value)} placeholder="Cidade" className="rounded-md border border-slate-300 px-3 py-2 text-sm" />
        <input value={state} onChange={(e) => setState(e.target.value)} placeholder="UF" className="rounded-md border border-slate-300 px-3 py-2 text-sm" />
        <select value={urgent} onChange={(e) => setUrgent(e.target.value)} className="rounded-md border border-slate-300 px-3 py-2 text-sm">
          <option value="">Urgência (todos)</option>
          <option value="true">Urgentes</option>
          <option value="false">Não urgentes</option>
        </select>
        <button onClick={() => setPage(0)} className="bg-primary-600 text-white rounded-md px-3 py-2 text-sm">Aplicar filtros</button>
      </div>
      {(isLoading || (!isAdmin && isActiveNgosLoading)) && <p className="text-slate-500">Carregando…</p>}
      {isError && <p className="text-slate-500">Falha ao carregar campanhas.</p>}
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredCampaigns.map(c => (
          <Link to={`/campanhas/${c.id}`} key={c.id} className="bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-md transition">
            <img
              src={c.coverImageUrl || defaultImage}
              alt={c.title}
              className="w-full h-40 object-cover"
              loading="lazy"
            />
            <div className="p-4">
              <p className="text-xs text-slate-500">{c.ngoName} {c.ngoCity && `· ${c.ngoCity}/${c.ngoState}`}</p>
              <h3 className="font-semibold mb-1">{c.title}</h3>
              {c.urgent && <p className="text-xs text-red-600 font-medium mb-1">Urgente</p>}
              <p className="text-sm text-slate-600 line-clamp-2 mb-2">{c.description}</p>
              <div className="text-sm">Arrecadado: <strong>{fmtBRL(c.raisedAmount)}</strong>{c.financialGoal && <> / {fmtBRL(c.financialGoal)}</>}</div>
            </div>
          </Link>
        ))}
        {!isLoading && !isError && filteredCampaigns.length === 0 && <p className="text-slate-500">Nenhuma campanha ainda.</p>}
      </div>
      <div className="mt-6 flex justify-end gap-2">
        <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0} className="px-3 py-2 text-sm rounded-md border border-slate-300 disabled:opacity-50">Anterior</button>
        <button onClick={() => setPage((p) => p + 1)} disabled={data ? page + 1 >= data.totalPages : true} className="px-3 py-2 text-sm rounded-md border border-slate-300 disabled:opacity-50">Próxima</button>
      </div>
    </div>
  );
}
