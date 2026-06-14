import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { fmtBRL } from "@/lib/utils";
import type { ApiResponse, PageResponse, Donation } from "@/types";

export default function MyDonations() {
  const { data } = useQuery({
    queryKey: ["my-donations"],
    queryFn: () => api.get<ApiResponse<PageResponse<Donation>>>("/donations/me").then(r => r.data.data),
  });
  return (
    <div className="max-w-5xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6">Minhas doações</h1>
      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr><th className="text-left p-3">Campanha</th><th className="text-left p-3">Tipo</th><th className="text-left p-3">Valor/Item</th><th className="text-left p-3">Status</th><th className="p-3">Recibo</th></tr>
          </thead>
          <tbody>
            {data?.content?.map(d => (
              <tr key={d.id} className="border-t">
                <td className="p-3">{d.campaignTitle}</td>
                <td className="p-3">{d.type}</td>
                <td className="p-3">{d.type === "FINANCIAL" ? fmtBRL(d.amount) : `${d.itemName} x${d.itemQuantity} ${d.itemUnit ?? ""}`}</td>
                <td className="p-3">{d.status}</td>
                <td className="p-3 text-center">
                  <a href={`${import.meta.env.VITE_API_URL}/donations/${d.id}/receipt`} target="_blank" rel="noreferrer" className="text-primary-600 hover:underline">PDF</a>
                </td>
              </tr>
            ))}
            {!data?.content?.length && <tr><td colSpan={5} className="p-6 text-center text-slate-500">Nenhuma doação ainda.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
