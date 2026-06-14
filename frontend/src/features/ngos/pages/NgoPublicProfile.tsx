import { useParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import type { FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { ApiResponse, Ngo, Campaign, PageResponse } from "@/types";
import Button from "@/components/ui/Button";
import { toast } from "sonner";
import { useAuth } from "@/hooks/useAuth";
import defaultImage from "@/assets/images/img.png";

export default function NgoPublicProfile() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const qc = useQueryClient();
  const { data: activeCampaigns } = useQuery({
    queryKey: ["ngo-active-campaigns", id],
    queryFn: () => api.get<ApiResponse<PageResponse<Campaign>>>(`/campaigns/public?ngoId=${id}&status=ACTIVE`).then((r) => r.data.data),
    enabled: !!id,
  });

  const [selectedCampaignId, setSelectedCampaignId] = useState<number | null>(null);
  const [isVolunteerOpen, setIsVolunteerOpen] = useState(false);
  const [authVolunteerOpen, setAuthVolunteerOpen] = useState(false);
  const [volName, setVolName] = useState("");
  const [volEmail, setVolEmail] = useState("");
  const [volPhone, setVolPhone] = useState("");
  const [volMessage, setVolMessage] = useState("");

  useEffect(() => {
    const firstCampaignId = activeCampaigns?.content?.[0]?.id ?? null;
    setSelectedCampaignId((prev) => prev ?? firstCampaignId);
  }, [activeCampaigns]);

  const handleVolunteerSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!volName.trim() || !volEmail.trim() || !volPhone.trim()) {
      toast.error("Preencha nome, email e telefone.");
      return;
    }
    volunteerMutation.mutate();
  };

  const volunteerMutation = useMutation({
    mutationFn: async () => api.post(`/ngos/${id}/volunteers`, {
      name: volName,
      email: volEmail,
      phone: volPhone,
      message: volMessage,
    }),
    onSuccess: async () => {
      toast.success("Inscricao enviada.");
      setIsVolunteerOpen(false);
      setVolMessage("");
      setVolName(user?.name ?? "");
      setVolEmail(user?.email ?? "");
      setVolPhone(user?.phone ?? "");
      await qc.invalidateQueries({ queryKey: ["ngo-volunteers", id] });
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? "Erro ao enviar inscricao"),
  });

  useEffect(() => {
    if (isVolunteerOpen) {
      setVolName(user?.name ?? "");
      setVolEmail(user?.email ?? "");
      setVolPhone(user?.phone ?? "");
    }
  }, [isVolunteerOpen, user]);

  const { data, isLoading } = useQuery({
    queryKey: ["ngo-public", id],
    queryFn: () => api.get<ApiResponse<Ngo>>(`/ngos/public/${id}`).then((r) => r.data.data),
    enabled: !!id,
  });
  const { data: volunteers } = useQuery({
    queryKey: ["ngo-volunteers", id],
    queryFn: () => api.get<ApiResponse<{ id: number; name: string; email: string; phone?: string }[]>>(`/ngos/${id}/volunteers`).then((r) => r.data.data),
    enabled: !!id,
  });
  const isAdmin = user?.roles.includes("ADMIN");

  if (isLoading) {
    return <div className="max-w-5xl mx-auto px-4 py-10 text-slate-500">Carregando perfil da ONG...</div>;
  }
  if (!data) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-10 text-slate-500">
        {isAdmin ? "ONG não encontrada." : "ONG indisponível no momento."}
      </div>
    );
  }

  const isOngManager = user?.roles.includes("ONG_MANAGER");
  const isVolunteer = user?.roles.includes("VOLUNTEER");

  return (
    <div className="max-w-5xl mx-auto px-4 py-10">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-800 mb-6"
      >
        ← Voltar
      </button>
      <div className="bg-white rounded-xl shadow-sm p-6">
        <div className="flex gap-4 items-start">
          <img
            src={data.logoUrl || defaultImage}
            alt={data.name}
            className="w-24 h-24 rounded-lg object-cover border border-slate-200"
          />
          <div>
            <h1 className="text-3xl font-bold">{data.name}</h1>
            <p className="text-slate-500 text-sm mt-1">{data.categoryName} {data.city && `· ${data.city}/${data.state}`}</p>
            <p className="text-xs mt-2 inline-block px-2 py-1 rounded-full bg-emerald-100 text-emerald-700">{data.status}</p>
            {!isOngManager && (
              <div className="mt-3 flex flex-col gap-2">
                <Button
                  onClick={() => {
                    const selectedCampaign = activeCampaigns?.content?.find((c) => c.id === selectedCampaignId);
                    if (!selectedCampaignId || !selectedCampaign) {
                      toast.error("Selecione uma campanha ativa.");
                      return;
                    }
                    navigate(`/checkout?campaignId=${selectedCampaignId}&campaignTitle=${encodeURIComponent(selectedCampaign.title)}`);
                  }}
                >
                  Fazer Doacao
                </Button>
                <select
                  value={selectedCampaignId ?? ""}
                  onChange={(e) => setSelectedCampaignId(Number(e.target.value))}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                >
                  <option value="" disabled>Selecione uma campanha ativa</option>
                  {(activeCampaigns?.content ?? []).map((c) => (
                    <option key={c.id} value={c.id}>{c.title}</option>
                  ))}
                </select>
              </div>
            )}
          </div>
        </div>
        <div className="mt-6 grid md:grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-slate-500">Descrição</p>
            <p className="text-slate-700 mt-1">{data.description || "Não informada."}</p>
          </div>
          <div>
            <p className="text-slate-500">Contato</p>
            <p className="text-slate-700 mt-1">{data.phone || "Não informado."}</p>
          </div>
          <div>
            <p className="text-slate-500">Site</p>
            <p className="text-slate-700 mt-1">{data.website || "Não informado."}</p>
          </div>
          <div>
            <p className="text-slate-500">CNPJ</p>
            <p className="text-slate-700 mt-1">{data.cnpj}</p>
          </div>
        </div>
      </div>

      {data.allowVolunteers && (
        <div className="bg-white rounded-xl shadow-sm p-6 mt-6">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-lg font-semibold">Voluntariado</h2>
              <p className="text-sm text-slate-500">Vagas disponiveis: {data.volunteerSlots ?? 0}</p>
            </div>
            <Button onClick={() => (isVolunteer ? setIsVolunteerOpen(true) : setAuthVolunteerOpen(true))}>Quero ser voluntario</Button>
          </div>
          <div className="mt-4">
            {volunteers?.length ? (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-slate-500">
                      <th className="py-2">Nome</th>
                      <th className="py-2">Email</th>
                      <th className="py-2">Telefone</th>
                    </tr>
                  </thead>
                  <tbody>
                    {volunteers.map((v) => (
                      <tr key={v.id} className="border-t border-slate-100">
                        <td className="py-2">{v.name}</td>
                        <td className="py-2">{v.email}</td>
                        <td className="py-2">{v.phone || "-"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="text-sm text-slate-500">Nenhum voluntario cadastrado ainda.</p>
            )}
          </div>
        </div>
      )}

      {isVolunteerOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-lg rounded-xl bg-white p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold">Quero ser voluntario</h2>
              <button
                type="button"
                className="text-slate-500 hover:text-slate-700"
                onClick={() => setIsVolunteerOpen(false)}
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
                <Button type="button" variant="outline" onClick={() => setIsVolunteerOpen(false)}>Cancelar</Button>
                <Button type="submit" loading={volunteerMutation.isPending}>Enviar</Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {authVolunteerOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-lg">
            <h2 className="text-lg font-semibold mb-2">Entrar como voluntario</h2>
            <p className="text-sm text-slate-600">
              Para se candidatar como voluntario nesta ONG, entre com uma conta de voluntario ou crie uma.
            </p>
            <div className="mt-4 flex justify-end gap-2">
              <Button variant="outline" onClick={() => setAuthVolunteerOpen(false)}>Cancelar</Button>
              <Button
                variant="outline"
                onClick={() => {
                  setAuthVolunteerOpen(false);
                  navigate(`/cadastro?redirect=/ongs/${id}&msg=Crie sua conta de voluntario`);
                }}
              >
                Criar conta
              </Button>
              <Button
                onClick={() => {
                  setAuthVolunteerOpen(false);
                  navigate(`/login?redirect=/ongs/${id}&msg=Entre como voluntario para se candidatar`);
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
