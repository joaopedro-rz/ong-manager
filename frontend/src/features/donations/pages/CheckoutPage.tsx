import { useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import { toast } from "sonner";
import Button from "@/components/ui/Button";
import { api } from "@/lib/api";
import type { ApiResponse, Campaign } from "@/types";
import { useAuth } from "@/hooks/useAuth";

const paymentMethods = ["PIX", "CARD", "BOLETO"] as const;

type PaymentMethod = typeof paymentMethods[number];

export default function CheckoutPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const { user } = useAuth();
  const campaignId = params.get("campaignId");
  const campaignTitle = params.get("campaignTitle") ?? "";

  const [amount, setAmount] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("PIX");
  const [cardNumber, setCardNumber] = useState("");
  const [cardExpiry, setCardExpiry] = useState("");
  const [cardCvv, setCardCvv] = useState("");
  const isOngManager = user?.roles.includes("ONG_MANAGER");

  const { data: campaign } = useQuery({
    queryKey: ["campaign-checkout", campaignId],
    queryFn: () => api.get<ApiResponse<Campaign>>(`/campaigns/public/${campaignId}`).then((r) => r.data.data),
    enabled: !!campaignId,
  });

  const ngoName = useMemo(() => campaign?.ngoName ?? "", [campaign?.ngoName]);

  const donationMutation = useMutation({
    mutationFn: async () => {
      if (!campaignId) throw new Error("Campanha inválida.");
      if (isOngManager) throw new Error("Gestores de ONG não podem doar.");
      const value = Number(amount);
      if (!value || value <= 0) throw new Error("Informe um valor válido.");
      if (!paymentMethod) throw new Error("Selecione um método de pagamento.");
      return api.post("/donations/financial", {
        campaignId: Number(campaignId),
        amount: value,
        paymentMethod,
        demoConfirm: true,
      });
    },
    onSuccess: () => {
      toast.success("Doação registrada com sucesso!");
      if (window.history.length > 1) {
        navigate(-1);
      } else {
        navigate(`/campanhas/${campaignId}`);
      }
    },
    onError: (e: any) => toast.error(e.response?.data?.error?.message ?? e.message ?? "Erro ao registrar doação"),
  });

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <div className="flex items-center justify-between mb-6">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-800"
        >
          ← Voltar
        </button>
        <h1 className="text-xl font-semibold">Fazer Doação</h1>
        <div />
      </div>

      <div className="bg-white rounded-xl shadow-sm p-6 space-y-6">
        <div className="text-sm text-slate-600">
          <p>Campanha: <span className="font-medium text-slate-800">{campaign?.title ?? decodeURIComponent(campaignTitle)}</span></p>
          <p>ONG: <span className="font-medium text-slate-800">{ngoName || "-"}</span></p>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-2">Valor da doação</label>
          <div className="flex items-center gap-2">
            <span className="text-slate-600">R$</span>
            <input
              type="number"
              min={1}
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              placeholder="0,00"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-2">Método de pagamento</label>
          <div className="flex flex-wrap gap-4 text-sm">
            {paymentMethods.map((method) => (
              <label key={method} className="flex items-center gap-2">
                <input
                  type="radio"
                  name="paymentMethod"
                  value={method}
                  checked={paymentMethod === method}
                  onChange={() => setPaymentMethod(method)}
                />
                {method === "CARD" ? "Cartão" : method}
              </label>
            ))}
          </div>
        </div>

        {paymentMethod === "PIX" && (
          <div className="rounded-lg border border-dashed border-slate-300 p-4 text-sm text-slate-600">
            QR Code simulado: <span className="font-medium">000201010212</span>
          </div>
        )}

        {paymentMethod === "CARD" && (
          <div className="grid gap-3">
            <input
              value={cardNumber}
              onChange={(e) => setCardNumber(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm"
              placeholder="Número do cartão"
            />
            <div className="grid grid-cols-2 gap-3">
              <input
                value={cardExpiry}
                onChange={(e) => setCardExpiry(e.target.value)}
                className="rounded-md border border-slate-300 px-3 py-2 text-sm"
                placeholder="Validade"
              />
              <input
                value={cardCvv}
                onChange={(e) => setCardCvv(e.target.value)}
                className="rounded-md border border-slate-300 px-3 py-2 text-sm"
                placeholder="CVV"
              />
            </div>
          </div>
        )}

        {paymentMethod === "BOLETO" && (
          <div className="rounded-lg border border-dashed border-slate-300 p-4 text-sm text-slate-600">
            Código de barras simulado: <span className="font-medium">34191.79001 01043.510047 91020.150008 2 87430000010000</span>
          </div>
        )}

        <div className="flex justify-end">
          <Button onClick={() => donationMutation.mutate()} loading={donationMutation.isPending} disabled={isOngManager}>
            Confirmar Doação
          </Button>
        </div>
      </div>
    </div>
  );
}
