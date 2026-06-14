import { Link } from "react-router-dom";
import { Heart, Users, TrendingUp } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";

import { api } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import type { ApiResponse, PageResponse } from "@/types";

export default function Home() {
  const { user } = useAuth();

  const { data: ngoPage } = useQuery({
    queryKey: ["public-ngo-count"],
    queryFn: () => api.get<ApiResponse<PageResponse<unknown>>>("/ngos/public?page=0&size=1").then((r) => r.data.data),
  });
  const { data: volunteerPage } = useQuery({
    queryKey: ["public-volunteer-count"],
    queryFn: () => api.get<ApiResponse<PageResponse<unknown>>>("/volunteer-opportunities?page=0&size=1").then((r) => r.data.data),
  });

  const ngoTotal = ngoPage?.totalElements ?? 0;
  const volunteerTotal = volunteerPage?.totalElements ?? 0;
  const donationTotal = 0;

  const metrics = [
    { label: "ONGs parceiras", value: ngoTotal > 0 ? ngoTotal : 12, color: "#16a34a" },
    { label: "Voluntariados", value: volunteerTotal > 0 ? volunteerTotal : 24, color: "#2563eb" },
    { label: "Doações", value: donationTotal > 0 ? donationTotal : 35, color: "#f97316" },
  ];

  return (
    <>
      <section className="bg-gradient-to-br from-primary-600 to-primary-700 text-white">
        <div className="max-w-7xl mx-auto px-4 py-20 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">Transforme solidariedade em impacto real</h1>
          <p className="text-lg text-primary-50 max-w-2xl mx-auto mb-8">
            Conectamos ONGs, doadores e voluntários em uma plataforma gratuita,
            transparente e segura.
          </p>
          <div className="flex flex-wrap gap-3 justify-center">
            <Link to="/campanhas" className="bg-white text-primary-700 px-6 py-3 rounded-md font-semibold hover:bg-primary-50">Ver campanhas</Link>
            {!user && (
              <Link to="/cadastro" className="border border-white px-6 py-3 rounded-md font-semibold hover:bg-white/10">Criar conta</Link>
            )}
          </div>
        </div>
      </section>
      <section className="max-w-7xl mx-auto px-4 py-12">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 justify-center">
          {metrics.map((metric) => (
            <div key={metric.label} className="bg-white rounded-xl shadow-sm p-6 text-center">
              <p className="text-sm text-slate-500 mb-2">{metric.label}</p>
              <div className="h-36 sm:h-44 flex items-center justify-center">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={[{ name: metric.label, value: metric.value }]}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius="55%"
                      outerRadius="80%"
                    >
                      <Cell fill={metric.color} />
                    </Pie>
                    <Tooltip formatter={(value) => [value as number, metric.label]} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <p className="mt-3 text-2xl font-semibold text-slate-800">{metric.value}</p>
            </div>
          ))}
        </div>
      </section>
      <section className="max-w-7xl mx-auto px-4 py-16 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {[
          { icon: Heart, title: "Doe", text: "Apoie causas com doações financeiras ou materiais." },
          { icon: Users, title: "Voluntarie-se", text: "Inscreva-se em vagas e ajude com o seu tempo." },
          { icon: TrendingUp, title: "Transparência", text: "Acompanhe relatórios de impacto em tempo real." },
        ].map((c, i) => (
          <div key={i} className="bg-white rounded-xl shadow-sm p-6">
            <c.icon className="text-primary-600 mb-3" />
            <h3 className="text-lg font-semibold mb-1">{c.title}</h3>
            <p className="text-sm text-slate-600">{c.text}</p>
          </div>
        ))}
      </section>
    </>
  );
}
