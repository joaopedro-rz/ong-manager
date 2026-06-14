import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { api } from "@/lib/api";
import { fmtBRL } from "@/lib/utils";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, LabelList } from "recharts";
import type { ApiResponse, Ngo, PageResponse } from "@/types";

type MonthlyPoint = { period: string; amount: number; count: number };

type AdminDashboard = {
  totalNgos: number;
  totalActiveCampaigns: number;
  totalConfirmedDonations: number;
  totalRaised: number;
  totalVolunteers: number;
  monthlyDonations?: { period: string; amount: number | string; count: number | string }[];
};

type NgoDashboard = {
  ngoId: number;
  ngoName: string;
  totalRaised: number | string;
  monthlyDonations?: { period: string; amount: number | string; count: number | string }[];
};

export default function Dashboard() {
  const [selectedNgoId, setSelectedNgoId] = useState("");
  const normalizePeriod = (raw: string) => {
    if (!raw) return raw;
    const trimmed = raw.trim();
    if (trimmed.includes("-")) {
      const [year, month] = trimmed.split("-");
      if (year && month) return `${month.padStart(2, "0")}/${year}`;
    }
    return trimmed;
  };
  const parsePeriod = (raw: string) => {
    if (!raw) return null;
    const trimmed = raw.trim();
    let year: number | undefined;
    let month: number | undefined;
    if (trimmed.includes("-")) {
      const [y, m] = trimmed.split("-");
      year = Number(y);
      month = Number(m);
    } else if (trimmed.includes("/")) {
      const [m, y] = trimmed.split("/");
      year = Number(y);
      month = Number(m);
    }
    if (!year || !month) return null;
    return {
      year,
      month,
      key: `${String(month).padStart(2, "0")}/${year}`,
      date: new Date(year, month - 1, 1),
    };
  };
  const buildSeries = (source: { period: string; amount: number | string; count: number | string }[]) => {
    const points: MonthlyPoint[] = [];
    const now = new Date();
    const nowMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const byPeriod = new Map<string, MonthlyPoint>();
    const seenDates: Date[] = [];
    source.forEach((p) => {
      const period = normalizePeriod(p.period ?? "");
      const parsed = parsePeriod(period);
      if (!parsed) return;
      byPeriod.set(parsed.key, {
        period: parsed.key,
        amount: Number(p.amount ?? 0) || 0,
        count: Number(p.count ?? 0) || 0,
      });
      seenDates.push(parsed.date);
    });
    const startDate = seenDates.length
      ? new Date(Math.min(...seenDates.map((d) => d.getTime())))
      : new Date(now.getFullYear(), now.getMonth() - 5, 1);
    const endDate = nowMonth;
    const cursor = new Date(startDate.getFullYear(), startDate.getMonth(), 1);
    while (cursor <= endDate) {
      const key = `${String(cursor.getMonth() + 1).padStart(2, "0")}/${cursor.getFullYear()}`;
      points.push(byPeriod.get(key) ?? { period: key, amount: 0, count: 0 });
      cursor.setMonth(cursor.getMonth() + 1);
    }
    return points;
  };
  const { data: adminData, isLoading, isError } = useQuery({
    queryKey: ["dash-admin"],
    queryFn: () => api.get("/dashboard/admin").then(r => r.data.data as AdminDashboard),
  });
  const { data: ngoData } = useQuery({
    queryKey: ["dash-ngo", selectedNgoId],
    queryFn: () => api.get(`/dashboard/ngo/${selectedNgoId}`).then(r => r.data.data as NgoDashboard),
    enabled: Boolean(selectedNgoId),
  });
  const { data: ngos } = useQuery({
    queryKey: ["dashboard-ngos"],
    queryFn: () => api.get<ApiResponse<PageResponse<Ngo>>>("/ngos/moderation?page=0&size=1000&status=ACTIVE").then(r => r.data.data),
  });

  if (isLoading) return <div className="max-w-7xl mx-auto px-4 py-10 text-slate-500">Carregando dashboard...</div>;
  if (isError || !adminData) return <div className="max-w-7xl mx-auto px-4 py-10 text-red-500">Erro ao carregar dashboard. Verifique o servidor.</div>;

  const cards = [
    { label: "ONGs", value: adminData.totalNgos ?? 0 },
    { label: "Campanhas ativas", value: adminData.totalActiveCampaigns ?? 0 },
    { label: "Doações confirmadas", value: adminData.totalConfirmedDonations ?? 0 },
    { label: "Total arrecadado", value: fmtBRL(adminData.totalRaised ?? 0) },
    { label: "Voluntários", value: adminData.totalVolunteers ?? 0 },
  ];

  const chartSource = selectedNgoId && ngoData?.monthlyDonations
    ? ngoData.monthlyDonations
    : (adminData.monthlyDonations ?? []);
  const mappedSource = chartSource.map((p: any) => ({
    period: p.period ?? p.month,
    amount: p.amount ?? p.donations ?? 0,
    count: p.count ?? p.ngos ?? 0,
  }));
  const chart: MonthlyPoint[] = buildSeries(mappedSource);
  const chartTitle = selectedNgoId && ngoData?.ngoName
    ? `Faturamento por mês — ${ngoData.ngoName}`
    : "Faturamento por mês — Todas";
  const handleExportPdf = async () => {
    try {
      const url = selectedNgoId
        ? `/dashboard/ngo/${selectedNgoId}/report/pdf`
        : "/dashboard/admin/report/pdf";
      const response = await api.get(url, { responseType: "blob" });
      const blob = new Blob([response.data], { type: "application/pdf" });
      const link = document.createElement("a");
      const safeNgoName = ngoData?.ngoName ? ngoData.ngoName.replace(/\s+/g, "-") : "";
      const filename = selectedNgoId && safeNgoName
        ? `relatorio-ong-${safeNgoName}.pdf`
        : "relatorio-dashboard.pdf";
      link.href = window.URL.createObjectURL(blob);
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(link.href);
    } catch (error) {
      console.error("Erro ao baixar PDF", error);
    }
  };
  return (
    <div className="max-w-7xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 mb-8">
        {cards.map(c => (
          <div key={c.label} className="bg-white rounded-xl shadow-sm p-5">
            <p className="text-xs text-slate-500">{c.label}</p>
            <p className="text-2xl font-bold text-primary-700 mt-1">{c.value}</p>
          </div>
        ))}
      </div>
      <div className="bg-white rounded-xl shadow-sm p-6 h-72 sm:h-80 lg:h-96">
        <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
          <p className="text-sm text-slate-600">{chartTitle}</p>
          <div className="flex flex-wrap items-center gap-3">
            <select
              value={selectedNgoId}
              onChange={(e) => setSelectedNgoId(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="">Principal (todas)</option>
              {(ngos?.content ?? []).map((ngo) => (
                <option key={ngo.id} value={ngo.id}>{ngo.name}</option>
              ))}
            </select>
          </div>
        </div>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chart} margin={{ top: 16, right: 8, left: 0, bottom: 8 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
            <XAxis dataKey="period" tick={{ fontSize: 12 }} minTickGap={8} />
            <YAxis tick={{ fontSize: 12 }} />
            <Tooltip formatter={(value: number) => fmtBRL(value)} />
            <Bar dataKey="amount" name="Valor" fill="#059669" radius={[8, 8, 0, 0]}>
              <LabelList
                dataKey="amount"
                position="top"
                formatter={(value: number) => (Number(value) === 0 ? "" : fmtBRL(value))}
              />
            </Bar>
          </BarChart>
        </ResponsiveContainer>
        <div className="mt-4 flex justify-end">
          <button
            type="button"
            onClick={handleExportPdf}
            className="rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          >
            Baixar PDF
          </button>
        </div>
      </div>
    </div>
  );
}
