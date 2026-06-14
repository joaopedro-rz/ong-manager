import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { ApiResponse, Ngo, PageResponse } from "@/types";
import Button from "@/components/ui/Button";
import { toast } from "sonner";

const statuses = ["PENDING", "ACTIVE", "SUSPENDED"] as const;

export default function NgoModerationPage() {
  const [status, setStatus] = useState("");
  const qc = useQueryClient();
  const { data } = useQuery({
    queryKey: ["ngo-moderation", status],
    queryFn: () =>
      api.get<ApiResponse<PageResponse<Ngo>>>(`/ngos/moderation?page=0&size=20${status ? `&status=${status}` : ""}`)
        .then((r) => r.data.data),
  });

  const changeMutation = useMutation({
    mutationFn: ({ id, nextStatus }: { id: number; nextStatus: string }) =>
      api.patch(`/ngos/${id}/status?status=${nextStatus}`),
    onSuccess: async () => {
      toast.success("Status da ONG atualizado.");
      await qc.invalidateQueries({ queryKey: ["ngo-moderation"] });
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? "Erro ao atualizar status"),
  });

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold">Moderação de ONGs</h1>
        <select value={status} onChange={(e) => setStatus(e.target.value)} className="rounded-md border border-slate-300 px-3 py-2 text-sm">
          <option value="">Todos os status</option>
          {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>
      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th className="text-left px-4 py-3">ONG</th>
              <th className="text-left px-4 py-3">Categoria</th>
              <th className="text-left px-4 py-3">Status</th>
              <th className="text-left px-4 py-3">Ações</th>
            </tr>
          </thead>
          <tbody>
            {data?.content?.map((ngo) => (
              <tr key={ngo.id} className="border-t">
                <td className="px-4 py-3">
                  <p className="font-medium">{ngo.name}</p>
                  <p className="text-xs text-slate-500">{ngo.cnpj}</p>
                </td>
                <td className="px-4 py-3">{ngo.categoryName || "-"}</td>
                <td className="px-4 py-3">{ngo.status}</td>
                <td className="px-4 py-3 flex gap-2">
                  <Button variant="outline" onClick={() => changeMutation.mutate({ id: ngo.id, nextStatus: "ACTIVE" })}>Ativar</Button>
                  <Button variant="outline" onClick={() => changeMutation.mutate({ id: ngo.id, nextStatus: "PENDING" })}>Pendente</Button>
                  <Button variant="danger" onClick={() => changeMutation.mutate({ id: ngo.id, nextStatus: "SUSPENDED" })}>Suspender</Button>
                </td>
              </tr>
            ))}
            {!data?.content?.length && (
              <tr><td className="px-4 py-6 text-slate-500" colSpan={4}>Nenhuma ONG para moderar.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
