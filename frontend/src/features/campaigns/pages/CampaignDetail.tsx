import { useParams, useNavigate, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { fmtBRL } from "@/lib/utils";
import type { ApiResponse, Campaign, Ngo } from "@/types";
import Button from "@/components/ui/Button";
import defaultImage from "@/assets/images/img.png";
import { useAuth } from "@/hooks/useAuth";

export default function CampaignDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { data } = useQuery({
    queryKey: ["campaign", id],
    queryFn: () => api.get<ApiResponse<Campaign>>(`/campaigns/public/${id}`).then(r => r.data.data),
    enabled: !!id,
  });
  const isAdmin = user?.roles.includes("ADMIN");
  const { isLoading: isNgoLoading, isError: isNgoError } = useQuery({
    queryKey: ["ngo-public-status", data?.ngoId],
    queryFn: () => api.get<ApiResponse<Ngo>>(`/ngos/public/${data?.ngoId}`).then(r => r.data.data),
    enabled: Boolean(data?.ngoId) && !isAdmin,
    retry: false,
  });

  if (!data) return <div className="max-w-4xl mx-auto px-4 py-10 text-slate-500">Carregando…</div>;
  if (!isAdmin && isNgoLoading) {
    return <div className="max-w-4xl mx-auto px-4 py-10 text-slate-500">Carregando…</div>;
  }
  if (!isAdmin && isNgoError) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-10 text-slate-500">
        Campanha indisponível no momento.
      </div>
    );
  }
  const showNgoLink = isAdmin || !isNgoError;

  const isOngManager = user?.roles.includes("ONG_MANAGER");
  const pct = data.financialGoal
    ? Math.min(100, (Number(data.raisedAmount) / Number(data.financialGoal)) * 100)
    : 0;

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-800 mb-6"
      >
        ← Voltar
      </button>
      <img
        src={data.coverImageUrl || defaultImage}
        alt={data.title}
        className="w-full h-72 object-cover rounded-xl mb-6"
      />
      <p className="text-sm text-slate-500">
        {showNgoLink ? (
          <Link to={`/ongs/${data.ngoId}`} className="hover:underline">
            {data.ngoName}
          </Link>
        ) : (
          <span>{data.ngoName}</span>
        )}
      </p>
      <h1 className="text-3xl font-bold mb-2">{data.title}</h1>
      <p className="text-slate-700 mb-6">{data.description}</p>
      <div className="bg-white rounded-xl p-6 shadow-sm">
        <div className="flex justify-between mb-2 text-sm">
          <span>Arrecadado: <strong>{fmtBRL(data.raisedAmount)}</strong></span>
          {data.financialGoal && <span>Meta: {fmtBRL(data.financialGoal)}</span>}
        </div>
        <div className="h-2 bg-slate-200 rounded-full overflow-hidden">
          <div className="h-full bg-primary-600" style={{ width: `${pct}%` }} />
        </div>
      </div>
      {!isOngManager && (
        <div className="mt-4">
          <Button
            onClick={() => navigate(`/checkout?campaignId=${data.id}&campaignTitle=${encodeURIComponent(data.title)}`)}
          >
            Fazer Doacao
          </Button>
        </div>
      )}
      <div className="mt-6 grid md:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl p-6 shadow-sm">
          <h2 className="text-lg font-semibold mb-3">Necessidades de itens</h2>
          {data.items?.length ? data.items.map((i) => (
            <p key={i.id} className="text-sm text-slate-700 mb-1">
              {i.name} ({i.category || "geral"}) — {i.quantityReceived}/{i.quantityNeeded} {i.unit}
            </p>
          )) : <p className="text-sm text-slate-500">Sem itens cadastrados.</p>}
        </div>
        <div className="bg-white rounded-xl p-6 shadow-sm">
          <h2 className="text-lg font-semibold mb-3">Atualizações</h2>
          {data.updates?.length ? data.updates.map((u) => (
            <div key={u.id} className="mb-3">
              <p className="text-sm font-medium">{u.title}</p>
              <p className="text-sm text-slate-600">{u.content}</p>
            </div>
          )) : <p className="text-sm text-slate-500">Sem atualizações ainda.</p>}
        </div>
      </div>
    </div>
  );
}
