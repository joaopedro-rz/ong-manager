import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { api } from "@/lib/api";
import type { ApiResponse, Campaign, Ngo, PageResponse } from "@/types";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import { toast } from "sonner";
import { useAuth } from "@/hooks/useAuth";

const statuses = ["DRAFT", "ACTIVE", "COMPLETED", "CANCELLED"] as const;

type CampaignForm = {
  ngoId: number;
  title: string;
  description: string;
  financialGoal: number;
  startDate: string;
  endDate: string;
  coverImageUrl: string;
  status: string;
  urgent: boolean;
};

export default function CampaignCreatePage() {
  const qc = useQueryClient();
  const nav = useNavigate();
  const { user } = useAuth();
  const isAdmin = user?.roles.includes("ADMIN");
  const [selectedNgoId, setSelectedNgoId] = useState<number | null>(null);
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [coverPreview, setCoverPreview] = useState("");

  const [form, setForm] = useState<CampaignForm>({
    ngoId: 0,
    title: "",
    description: "",
    financialGoal: 0,
    startDate: "",
    endDate: "",
    coverImageUrl: "",
    status: "ACTIVE",
    urgent: false,
  });

  const ngoQueryUrl = isAdmin
    ? "/ngos?status=ACTIVE&page=0&size=100"
    : "/ngos/me?page=0&size=20";

  const { data: myNgos } = useQuery({
    queryKey: ["my-ngos-for-campaign-create", ngoQueryUrl],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>(ngoQueryUrl).then((r) => r.data.data),
  });

  const ngoId = useMemo(() => selectedNgoId ?? myNgos?.content?.[0]?.id ?? null, [myNgos, selectedNgoId]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      if (!ngoId && !form.ngoId) {
        throw new Error("Selecione uma ONG");
      }
      const payload = {
        ...form,
        ngoId: ngoId ?? form.ngoId,
        financialGoal: Number(form.financialGoal),
        coverImageUrl: form.coverImageUrl || undefined,
      };
      const response = await api.post<ApiResponse<Campaign>>("/campaigns", payload);
      const campaignId = response.data.data.id;
      if (coverFile) {
        const fd = new FormData();
        fd.append("file", coverFile);
        await api.post<ApiResponse<Campaign>>(`/campaigns/${campaignId}/cover`, fd, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      }
      return campaignId;
    },
    onSuccess: async () => {
      toast.success("Campanha criada.");
      await qc.invalidateQueries({ queryKey: ["campaigns"] });
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
      nav("/campanhas/gestao");
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? e.message ?? "Erro ao criar campanha"),
  });

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6">Criar campanha</h1>
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6 grid md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">ONG</label>
          <select
            value={ngoId ?? ""}
            onChange={(e) => setSelectedNgoId(Number(e.target.value))}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
          >
            {myNgos?.content?.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
          </select>
        </div>
      </div>
      <div className="bg-white rounded-xl shadow-sm p-6 space-y-3">
        <div className="grid md:grid-cols-2 gap-3">
          <Input label="Título" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
          <Input label="Meta financeira" type="number" value={form.financialGoal} onChange={(e) => setForm({ ...form, financialGoal: Number(e.target.value) })} />
          <Input label="Data início" type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
          <Input label="Data fim" type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Imagem de capa</label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => {
                const file = e.target.files?.[0] ?? null;
                setCoverFile(file);
                setCoverPreview(file ? URL.createObjectURL(file) : "");
              }}
            />
            {(coverPreview || form.coverImageUrl) && (
              <img
                src={coverPreview || form.coverImageUrl}
                alt="Prévia da capa"
                className="mt-2 h-24 w-full object-cover rounded-md"
              />
            )}
          </div>
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} className="rounded-md border border-slate-300 px-3 py-2 text-sm">
            {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <div>
          <label htmlFor="desc" className="block text-sm font-medium text-slate-700 mb-1">Descrição</label>
          <textarea id="desc" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm min-h-24" />
        </div>
        <label className="inline-flex items-center gap-2 text-sm">
          <input type="checkbox" checked={form.urgent} onChange={(e) => setForm({ ...form, urgent: e.target.checked })} />
          Campanha urgente
        </label>
        <Button onClick={() => saveMutation.mutate()} loading={saveMutation.isPending}>Criar campanha</Button>
      </div>
    </div>
  );
}

