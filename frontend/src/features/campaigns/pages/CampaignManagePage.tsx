import { useMemo, useState, useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { ApiResponse, Campaign, Ngo, PageResponse, CampaignItem, CampaignUpdate } from "@/types";
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

export default function CampaignManagePage() {
  const qc = useQueryClient();
  const { user } = useAuth();
  const isAdmin = user?.roles.includes("ADMIN");
  const [selectedNgoId, setSelectedNgoId] = useState<number | null>(null);
  const [selectedCampaignId, setSelectedCampaignId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Campaign | null>(null);
  const [itemName, setItemName] = useState("");
  const [itemQty, setItemQty] = useState(1);
  const [itemEdits, setItemEdits] = useState<Record<number, { quantityReceived: number; quantityNeeded: number }>>({});
  const [updateTitle, setUpdateTitle] = useState("");
  const [updateContent, setUpdateContent] = useState("");
  const [editingUpdateId, setEditingUpdateId] = useState<number | null>(null);
  const [updateEdits, setUpdateEdits] = useState<Record<number, { title: string; content: string }>>({});
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
    queryKey: ["my-ngos-for-campaign", ngoQueryUrl],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>(ngoQueryUrl).then((r) => r.data.data),
  });

  const ngoId = useMemo(() => selectedNgoId ?? myNgos?.content?.[0]?.id ?? null, [myNgos, selectedNgoId]);

  const { data: campaigns } = useQuery({
    queryKey: ["my-campaigns", ngoId],
    queryFn: () => api.get<ApiResponse<PageResponse<Campaign>>>(`/campaigns/public?ngoId=${ngoId}&page=0&size=50`).then((r) => r.data.data),
    enabled: !!ngoId,
  });

  useEffect(() => {
    const firstCampaignId = campaigns?.content?.[0]?.id ?? null;
    setSelectedCampaignId((prev) => prev ?? firstCampaignId);
  }, [campaigns]);

  const { data: campaignDetails } = useQuery({
    queryKey: ["campaign-details", selectedCampaignId],
    queryFn: () => api.get<ApiResponse<Campaign>>(`/campaigns/public/${selectedCampaignId}`).then((r) => r.data.data),
    enabled: !!selectedCampaignId,
  });

  useEffect(() => {
    if (!campaignDetails) return;
    setItemEdits(
      (campaignDetails.items as CampaignItem[] | undefined)?.reduce((acc, item) => {
        acc[item.id] = {
          quantityReceived: Number(item.quantityReceived ?? 0),
          quantityNeeded: Number(item.quantityNeeded ?? 0),
        };
        return acc;
      }, {} as Record<number, { quantityReceived: number; quantityNeeded: number }>) ?? {},
    );
    setUpdateEdits(
      (campaignDetails.updates as CampaignUpdate[] | undefined)?.reduce((acc, update) => {
        acc[update.id] = { title: update.title ?? "", content: update.content ?? "" };
        return acc;
      }, {} as Record<number, { title: string; content: string }>) ?? {},
    );
  }, [campaignDetails]);

  const openEditCampaign = (c: Campaign) => {
    setEditing(c);
    setCoverFile(null);
    setCoverPreview(c.coverImageUrl || "");
    setForm({
      ngoId: c.ngoId,
      title: c.title,
      description: c.description || "",
      financialGoal: Number(c.financialGoal || 0),
      startDate: c.startDate,
      endDate: c.endDate || "",
      coverImageUrl: c.coverImageUrl || "",
      status: c.status,
      urgent: Boolean(c.urgent),
    });
  };

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = { ...form, ngoId: ngoId ?? form.ngoId, financialGoal: Number(form.financialGoal), coverImageUrl: form.coverImageUrl || undefined };
      const response = editing
        ? await api.put<ApiResponse<Campaign>>(`/campaigns/${editing.id}`, payload)
        : await api.post<ApiResponse<Campaign>>("/campaigns", payload);
      const campaignId = response.data.data.id;
      if (coverFile) {
        const fd = new FormData();
        fd.append("file", coverFile);
        const coverResp = await api.post<ApiResponse<Campaign>>(`/campaigns/${campaignId}/cover`, fd, { headers: { "Content-Type": "multipart/form-data" } });
        setForm((prev) => ({ ...prev, coverImageUrl: coverResp.data.data.coverImageUrl ?? prev.coverImageUrl }));
      }
      return campaignId;
    },
    onSuccess: async () => {
      toast.success("Campanha salva.");
      setEditing(null);
      setCoverFile(null);
      setCoverPreview("");
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
      await qc.invalidateQueries({ queryKey: ["campaigns"] });
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? "Erro ao salvar campanha"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/campaigns/${id}`),
    onSuccess: async () => {
      toast.success("Campanha removida.");
      setSelectedCampaignId(null);
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
      await qc.invalidateQueries({ queryKey: ["campaigns"] });
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
    },
  });

  const addItemMutation = useMutation({
    mutationFn: (campaignId: number) => api.post(`/campaigns/${campaignId}/items`, { name: itemName, quantityNeeded: itemQty, unit: "un" }),
    onSuccess: async () => {
      setItemName("");
      setItemQty(1);
      toast.success("Item adicionado.");
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
    },
  });

  const updateItemMutation = useMutation({
    mutationFn: ({ campaignId, itemId, payload }: { campaignId: number; itemId: number; payload: { quantityReceived: number; quantityNeeded: number } }) =>
      api.patch(`/campaigns/${campaignId}/items/${itemId}`, payload),
    onSuccess: async () => {
      toast.success("Item atualizado.");
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
    },
  });

  const deleteItemMutation = useMutation({
    mutationFn: ({ campaignId, itemId }: { campaignId: number; itemId: number }) => api.delete(`/campaigns/${campaignId}/items/${itemId}`),
    onSuccess: async () => {
      toast.success("Item removido.");
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
    },
  });

  const addUpdateMutation = useMutation({
    mutationFn: (campaignId: number) => api.post(`/campaigns/${campaignId}/updates`, { title: updateTitle, content: updateContent }),
    onSuccess: async () => {
      setUpdateTitle("");
      setUpdateContent("");
      toast.success("Atualização publicada.");
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
    },
  });

  const updateUpdateMutation = useMutation({
    mutationFn: ({ campaignId, updateId, payload }: { campaignId: number; updateId: number; payload: { title: string; content: string } }) =>
      api.put(`/campaigns/${campaignId}/updates/${updateId}`, payload),
    onSuccess: async () => {
      toast.success("Atualização salva.");
      setEditingUpdateId(null);
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
    },
  });

  const deleteUpdateMutation = useMutation({
    mutationFn: ({ campaignId, updateId }: { campaignId: number; updateId: number }) => api.delete(`/campaigns/${campaignId}/updates/${updateId}`),
    onSuccess: async () => {
      toast.success("Atualização removida.");
      await qc.invalidateQueries({ queryKey: ["campaign-details"] });
      await qc.invalidateQueries({ queryKey: ["my-campaigns"] });
    },
  });

  return (
    <div className="max-w-7xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6">Gestão de Campanhas</h1>
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6 grid md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">ONG</label>
          <select
            value={ngoId ?? ""}
            onChange={(e) => {
              setSelectedNgoId(Number(e.target.value));
              setSelectedCampaignId(null);
            }}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
          >
            {myNgos?.content?.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Campanha</label>
          <select
            value={selectedCampaignId ?? ""}
            onChange={(e) => setSelectedCampaignId(Number(e.target.value))}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
          >
            <option value="" disabled>Selecione uma campanha</option>
            {(campaigns?.content ?? []).map((c) => <option key={c.id} value={c.id}>{c.title}</option>)}
          </select>
        </div>
      </div>

      {campaignDetails && (
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6 space-y-4">
          <div className="flex items-start justify-between gap-3">
            <div>
              <h2 className="text-xl font-semibold">{campaignDetails.title}</h2>
              <p className="text-xs text-slate-500">{campaignDetails.status} {campaignDetails.urgent ? "· Urgente" : ""}</p>
            </div>
            <div className="flex gap-2">
              <Button variant="outline" onClick={() => openEditCampaign(campaignDetails)}>Editar campanha</Button>
              <Button variant="danger" onClick={() => deleteMutation.mutate(campaignDetails.id)}>Excluir</Button>
            </div>
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <p className="text-xs text-slate-500 mb-2">Itens necessários</p>
              {(campaignDetails.items as CampaignItem[] | undefined)?.map((item) => {
                const edit = itemEdits[item.id] ?? { quantityReceived: 0, quantityNeeded: 0 };
                const progress = edit.quantityNeeded > 0 ? Math.min(100, Math.round((edit.quantityReceived / edit.quantityNeeded) * 100)) : 0;
                return (
                  <div key={item.id} className="border border-slate-200 rounded-md p-3 mb-3">
                    <div className="flex items-center justify-between gap-2">
                      <p className="text-sm font-medium">{item.name}</p>
                      <Button
                        variant="danger"
                        onClick={() => deleteItemMutation.mutate({ campaignId: campaignDetails.id, itemId: item.id })}
                      >
                        Excluir item
                      </Button>
                    </div>
                    <div className="mt-2 flex items-center gap-2 text-xs text-slate-500">
                      <span>{edit.quantityReceived}/{edit.quantityNeeded} {item.unit}</span>
                      <div className="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden">
                        <div className="h-full bg-emerald-500" style={{ width: `${progress}%` }} />
                      </div>
                    </div>
                    <div className="mt-3 grid grid-cols-2 gap-2">
                      <input
                        type="number"
                        value={edit.quantityReceived}
                        onChange={(e) =>
                          setItemEdits((prev) => ({
                            ...prev,
                            [item.id]: { ...prev[item.id], quantityReceived: Number(e.target.value) },
                          }))
                        }
                        className="rounded-md border border-slate-300 px-3 py-2 text-sm"
                        placeholder="Recebido"
                      />
                      <input
                        type="number"
                        value={edit.quantityNeeded}
                        onChange={(e) =>
                          setItemEdits((prev) => ({
                            ...prev,
                            [item.id]: { ...prev[item.id], quantityNeeded: Number(e.target.value) },
                          }))
                        }
                        className="rounded-md border border-slate-300 px-3 py-2 text-sm"
                        placeholder="Necessário"
                      />
                    </div>
                    <Button
                      variant="outline"
                      className="mt-2"
                      onClick={() => updateItemMutation.mutate({
                        campaignId: campaignDetails.id,
                        itemId: item.id,
                        payload: { quantityReceived: edit.quantityReceived, quantityNeeded: edit.quantityNeeded },
                      })}
                    >
                      Salvar
                    </Button>
                  </div>
                );
              })}

              <div className="flex gap-2 mt-3">
                <input
                  value={itemName}
                  onChange={(e) => setItemName(e.target.value)}
                  placeholder="Novo item"
                  className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm"
                />
                <input
                  type="number"
                  value={itemQty}
                  onChange={(e) => setItemQty(Number(e.target.value))}
                  className="w-24 rounded-md border border-slate-300 px-3 py-2 text-sm"
                />
                <Button variant="outline" onClick={() => addItemMutation.mutate(campaignDetails.id)}>Adicionar</Button>
              </div>
            </div>

            <div>
              <p className="text-xs text-slate-500 mb-2">Atualizações</p>
              {(campaignDetails.updates as CampaignUpdate[] | undefined)?.map((u) => {
                const edit = updateEdits[u.id] ?? { title: u.title ?? "", content: u.content ?? "" };
                const isEditing = editingUpdateId === u.id;
                return (
                  <div key={u.id} className="border border-slate-200 rounded-md p-3 mb-3">
                    {isEditing ? (
                      <div className="space-y-2">
                        <input
                          value={edit.title}
                          onChange={(e) =>
                            setUpdateEdits((prev) => ({
                              ...prev,
                              [u.id]: { ...prev[u.id], title: e.target.value },
                            }))
                          }
                          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                        />
                        <textarea
                          value={edit.content}
                          onChange={(e) =>
                            setUpdateEdits((prev) => ({
                              ...prev,
                              [u.id]: { ...prev[u.id], content: e.target.value },
                            }))
                          }
                          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm min-h-20"
                        />
                        <div className="flex gap-2">
                          <Button
                            variant="outline"
                            onClick={() => updateUpdateMutation.mutate({
                              campaignId: campaignDetails.id,
                              updateId: u.id,
                              payload: { title: edit.title, content: edit.content },
                            })}
                          >
                            Salvar
                          </Button>
                          <Button variant="outline" onClick={() => setEditingUpdateId(null)}>Cancelar</Button>
                        </div>
                      </div>
                    ) : (
                      <div className="space-y-2">
                        <p className="text-sm"><strong>{u.title}:</strong> {u.content}</p>
                        <div className="flex gap-2">
                          <Button variant="outline" onClick={() => setEditingUpdateId(u.id)}>Editar</Button>
                          <Button variant="danger" onClick={() => deleteUpdateMutation.mutate({ campaignId: campaignDetails.id, updateId: u.id })}>Excluir</Button>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}

              <div className="mt-2 space-y-2">
                <input
                  value={updateTitle}
                  onChange={(e) => setUpdateTitle(e.target.value)}
                  placeholder="Título da atualização"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                />
                <textarea
                  value={updateContent}
                  onChange={(e) => setUpdateContent(e.target.value)}
                  placeholder="Conteúdo"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm min-h-20"
                />
                <Button variant="outline" onClick={() => addUpdateMutation.mutate(campaignDetails.id)}>Publicar</Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {editing && (
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6 space-y-3">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-xl font-semibold">Editar campanha</h2>
            <Button variant="outline" onClick={() => setEditing(null)}>Cancelar edição</Button>
          </div>
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
          <Button onClick={() => saveMutation.mutate()} loading={saveMutation.isPending}>Salvar alterações</Button>
        </div>
      )}

      {!campaignDetails && (
        <p className="text-sm text-slate-500">Selecione uma campanha para gerenciar itens e atualizações.</p>
      )}
    </div>
  );
}
