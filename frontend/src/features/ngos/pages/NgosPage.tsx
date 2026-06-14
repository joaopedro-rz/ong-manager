import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "@/lib/api";
import type { ApiResponse, PageResponse, Ngo, NgoCategory } from "@/types";
import { useAuth } from "@/hooks/useAuth";
import defaultImage from "@/assets/images/img.png";

export default function NgosPage() {
  const { user } = useAuth();
  const isManager = user?.roles.some((r) => r === "ONG_MANAGER" || r === "ADMIN");

  const [params, setParams] = useSearchParams();
  const [search, setSearch] = useState(params.get("search") ?? params.get("q") ?? "");
  const [city, setCity] = useState(params.get("city") ?? "");
  const [regionState, setRegionState] = useState(params.get("state") ?? "");
  const page = Number(params.get("page") ?? "0");
  const categoryId = params.get("categoryId") ?? "";

  const queryParams = useMemo(() => {
    const p = new URLSearchParams();
    p.set("page", String(page));
    p.set("size", "9");
    if (search.trim()) p.set("search", search.trim());
    if (city.trim()) p.set("city", city.trim());
    if (regionState.trim()) p.set("state", regionState.trim());
    if (categoryId) p.set("categoryId", categoryId);
    return p.toString();
  }, [page, search, city, regionState, categoryId]);

  const { data, isLoading, isError } = useQuery({
    queryKey: ["ngos", queryParams],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>(`/ngos?${queryParams}`).then(r => r.data.data),
    retry: false,
  });

  const { data: categories } = useQuery({
    queryKey: ["ngo-categories"],
    queryFn: () => api.get<ApiResponse<NgoCategory[]>>("/ngos/categories").then(r => r.data.data),
  });

  const { data: myNgos } = useQuery({
    queryKey: ["my-ngos-summary"],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>("/ngos/me?page=0&size=1").then((r) => r.data.data),
    enabled: Boolean(isManager),
  });
  const hasNgo = (myNgos?.totalElements ?? 0) > 0;

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

  return (
    <div className="max-w-7xl mx-auto px-4 py-10">
      <div className="flex flex-wrap items-center justify-between gap-3 mb-6">
        <h1 className="text-3xl font-bold">ONGs parceiras</h1>
        {isManager && (hasNgo ? (
          <Link to="/ongs/gestao" className="text-sm bg-primary-600 hover:bg-primary-700 text-white px-3 py-2 rounded-md">
            Minha ONG
          </Link>
        ) : (
          <div className="flex flex-wrap items-center gap-3 text-sm text-slate-600">
            <span>Nenhuma ONG cadastrada.</span>
            <Link to="/ongs/gestao" className="text-sm bg-primary-600 hover:bg-primary-700 text-white px-3 py-2 rounded-md">
              Cadastrar ONG
            </Link>
          </div>
        ))}
      </div>
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
          <option value="">Todas as áreas</option>
          {categories?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <button onClick={applyFilters} className="bg-primary-600 text-white rounded-md px-3 py-2 text-sm">Aplicar filtros</button>
      </div>
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
        {isLoading && <p className="text-slate-500">Carregando…</p>}
        {isError && <p className="text-slate-500">Falha ao carregar ONGs.</p>}
        {!isLoading && !isError && !data?.content?.length && <p className="text-slate-500">Nenhuma ONG ativa.</p>}
        {data?.content?.map(n => (
          <Link key={n.id} to={`/ongs/${n.id}`} className="bg-white rounded-xl shadow-sm p-5 block hover:shadow-md transition">
            <div className="flex items-center gap-3 mb-2">
              <img
                src={n.logoUrl || defaultImage}
                alt={n.name}
                className="w-12 h-12 rounded-md object-cover border border-slate-200"
                loading="lazy"
              />
              <div>
                <h3 className="font-semibold">{n.name}</h3>
                <p className="text-xs text-slate-500">{n.categoryName} {n.city && `· ${n.city}/${n.state}`}</p>
              </div>
            </div>
            <p className="text-sm text-slate-600 line-clamp-3">{n.description}</p>
          </Link>
        ))}
      </div>
      <div className="mt-6 flex justify-end gap-2">
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
          Próxima
        </button>
      </div>
    </div>
  );
}
