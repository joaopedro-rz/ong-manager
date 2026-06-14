import { ButtonHTMLAttributes } from "react";
import { cn } from "@/lib/utils";

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "outline" | "ghost" | "danger";
  loading?: boolean;
}
export default function Button({ variant="primary", loading, className, children, disabled, ...rest }: Props) {
  const base = "inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium px-4 py-2 transition focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 disabled:opacity-50";
  const variants = {
    primary: "bg-primary-600 text-white hover:bg-primary-700",
    outline: "border border-slate-300 text-slate-700 hover:bg-slate-50",
    ghost: "text-slate-700 hover:bg-slate-100",
    danger: "bg-danger text-white hover:bg-red-700",
  } as const;
  return (
    <button {...rest} disabled={disabled || loading} className={cn(base, variants[variant], className)}>
      {loading ? "Carregando…" : children}
    </button>
  );
}
