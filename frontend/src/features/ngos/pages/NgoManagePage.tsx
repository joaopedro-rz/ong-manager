import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import type { ApiResponse, Ngo, NgoCategory, PageResponse, VolunteerApplication } from "@/types";
import { toast } from "sonner";

type FormData = {
  name: string;
  cnpj: string;
  city?: string;
  state?: string;
  description?: string;
  phone?: string;
  website?: string;
  socialMedia?: string;
  certifications?: string;
  categoryId?: number;
  allowVolunteers?: boolean;
  volunteerSlots?: number;
};

export default function NgoManagePage() {
  const qc = useQueryClient();
  const { user } = useAuth();
  const [logoFile, setLogoFile] = useState<File | null>(null);

  const { data: myNgos } = useQuery({
    queryKey: ["my-ngos"],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>("/ngos/me?page=0&size=1").then((r) => r.data.data),
  });
  const ngo = myNgos?.content?.[0];

  const { data: categories } = useQuery({
    queryKey: ["ngo-categories"],
    queryFn: () => api.get<ApiResponse<NgoCategory[]>>("/ngos/categories").then((r) => r.data.data),
  });

  const { register, handleSubmit, reset, formState: { isSubmitting }, watch } = useForm<FormData>({
    values: useMemo(() => ({
      name: ngo?.name ?? "",
      cnpj: ngo?.cnpj ?? "",
      city: ngo?.city ?? "",
      state: ngo?.state ?? "",
      description: ngo?.description ?? "",
      phone: ngo?.phone ?? "",
      website: ngo?.website ?? "",
      socialMedia: "",
      certifications: "",
      categoryId: undefined,
      allowVolunteers: ngo?.allowVolunteers ?? false,
      volunteerSlots: ngo?.volunteerSlots ?? 0,
    }), [ngo]),
  });

  const allowVolunteers = watch("allowVolunteers");

  const saveMutation = useMutation({
    mutationFn: async (data: FormData) => {
      const { city, state, ...rest } = data;
      const payload = {
        ...rest,
        address: {
          city: city?.trim() || null,
          state: state?.trim() || null,
        },
      };
      if (ngo) {
        await api.put(`/ngos/${ngo.id}`, payload);
        return ngo.id;
      }
      const created = await api.post<ApiResponse<Ngo>>("/ngos", payload);
      return created.data.data.id;
    },
    onSuccess: async (ngoId) => {
      if (logoFile) {
        const fd = new FormData();
        fd.append("file", logoFile);
        await api.post(`/ngos/${ngoId}/logo`, fd, { headers: { "Content-Type": "multipart/form-data" } });
      }
      toast.success("Dados da ONG salvos com sucesso.");
      await qc.invalidateQueries({ queryKey: ["my-ngos"] });
      await qc.invalidateQueries({ queryKey: ["ngos"] });
      reset();
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? "Erro ao salvar ONG"),
  });

  const onSubmit = (d: FormData) => saveMutation.mutate(d);

  const { data: pendingApplications } = useQuery({
    queryKey: ["ngo-volunteer-pending", ngo?.id],
    queryFn: () => api.get<ApiResponse<VolunteerApplication[]>>(`/ngos/${ngo?.id}/volunteers/applications?status=PENDING`).then((r) => r.data.data),
    enabled: !!ngo?.id,
  });

  const { data: approvedApplications } = useQuery({
    queryKey: ["ngo-volunteer-approved", ngo?.id],
    queryFn: () => api.get<ApiResponse<VolunteerApplication[]>>(`/ngos/${ngo?.id}/volunteers/applications?status=APPROVED`).then((r) => r.data.data),
    enabled: !!ngo?.id,
  });

  const approveMutation = useMutation({
    mutationFn: async (applicationId: number) => api.patch(`/ngos/${ngo?.id}/volunteers/${applicationId}/approve`),
    onSuccess: async () => {
      toast.success("Voluntario aprovado.");
      await qc.invalidateQueries({ queryKey: ["ngo-volunteer-pending", ngo?.id] });
      await qc.invalidateQueries({ queryKey: ["ngo-volunteer-approved", ngo?.id] });
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? "Erro ao aprovar voluntario"),
  });

  const rejectMutation = useMutation({
    mutationFn: async (applicationId: number) => api.patch(`/ngos/${ngo?.id}/volunteers/${applicationId}/reject`),
    onSuccess: async () => {
      toast.success("Voluntario rejeitado.");
      await qc.invalidateQueries({ queryKey: ["ngo-volunteer-pending", ngo?.id] });
      await qc.invalidateQueries({ queryKey: ["ngo-volunteer-approved", ngo?.id] });
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? "Erro ao rejeitar voluntario"),
  });

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-2">Gestão da ONG</h1>
      <p className="text-sm text-slate-500 mb-6">Perfil: {user?.roles.join(", ")}</p>

      <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-xl shadow-sm p-6 space-y-4">
        <Input label="Nome da ONG" {...register("name", { required: true })} />
        <Input label="CNPJ" {...register("cnpj", { required: true })} />
        <div className="grid md:grid-cols-2 gap-3">
          <Input label="Cidade" {...register("city")} />
          <Input label="Estado" {...register("state")} />
        </div>
        <Input label="Telefone" {...register("phone")} />
        <Input label="Site" {...register("website")} />
        <Input label="Redes sociais" {...register("socialMedia")} />
        <Input label="Certificações" {...register("certifications")} />
        <div>
          <label htmlFor="description" className="block text-sm font-medium text-slate-700 mb-1">Descrição</label>
          <textarea id="description" {...register("description")} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm min-h-24" />
        </div>
        <div>
          <label htmlFor="categoryId" className="block text-sm font-medium text-slate-700 mb-1">Área de atuação</label>
          <select id="categoryId" {...register("categoryId", { valueAsNumber: true })} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
            <option value="">Selecione uma categoria</option>
            {categories?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div>
          <label htmlFor="logo" className="block text-sm font-medium text-slate-700 mb-1">Logo</label>
          <input id="logo" type="file" accept="image/*" onChange={(e) => setLogoFile(e.target.files?.[0] ?? null)} />
        </div>
        <div className="rounded-md border border-slate-200 p-3">
          <label className="inline-flex items-center gap-2 text-sm">
            <input type="checkbox" {...register("allowVolunteers")} />
            Aceitar voluntarios
          </label>
          {allowVolunteers && (
            <div className="mt-3">
              <Input
                label="Numero de vagas para voluntarios"
                type="number"
                {...register("volunteerSlots", { valueAsNumber: true })}
              />
            </div>
          )}
        </div>
        <Button type="submit" loading={isSubmitting || saveMutation.isPending} className="w-full">
          {ngo ? "Atualizar ONG" : "Criar ONG"}
        </Button>
      </form>

      <div className="mt-10 bg-white rounded-xl shadow-sm p-6">
        <h2 className="text-xl font-semibold mb-4">Solicitacoes de Voluntariado</h2>
        {!pendingApplications?.length ? (
          <p className="text-sm text-slate-500">Nenhuma solicitacao pendente no momento.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-slate-500">
                  <th className="py-2">Nome</th>
                  <th className="py-2">Email</th>
                  <th className="py-2">Telefone</th>
                  <th className="py-2">Mensagem</th>
                  <th className="py-2">Acoes</th>
                </tr>
              </thead>
              <tbody>
                {pendingApplications.map((app) => (
                  <tr key={app.id} className="border-t border-slate-100">
                    <td className="py-2">{app.volunteerName}</td>
                    <td className="py-2">{app.volunteerEmail}</td>
                    <td className="py-2">{app.volunteerPhone || "-"}</td>
                    <td className="py-2">{app.motivation || "-"}</td>
                    <td className="py-2">
                      <div className="flex gap-2">
                        <Button
                          onClick={() => approveMutation.mutate(app.id)}
                          loading={approveMutation.isPending}
                        >
                          Aprovar
                        </Button>
                        <Button
                          variant="outline"
                          onClick={() => rejectMutation.mutate(app.id)}
                          loading={rejectMutation.isPending}
                        >
                          Rejeitar
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="mt-6 bg-white rounded-xl shadow-sm p-6">
        <h2 className="text-xl font-semibold mb-4">Voluntarios Ativos</h2>
        {!approvedApplications?.length ? (
          <p className="text-sm text-slate-500">Nenhum voluntario ativo no momento.</p>
        ) : (
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
                {approvedApplications.map((app) => (
                  <tr key={app.id} className="border-t border-slate-100">
                    <td className="py-2">{app.volunteerName}</td>
                    <td className="py-2">{app.volunteerEmail}</td>
                    <td className="py-2">{app.volunteerPhone || "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
