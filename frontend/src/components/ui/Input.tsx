import { InputHTMLAttributes, forwardRef } from "react";
import { cn } from "@/lib/utils";

interface Props extends InputHTMLAttributes<HTMLInputElement> {
  label: string; error?: string;
}
const Input = forwardRef<HTMLInputElement, Props>(({ label, error, id, className, ...rest }, ref) => {
  const inputId = id || rest.name;
  return (
    <div className="w-full">
      <label htmlFor={inputId} className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      <input id={inputId} ref={ref} aria-invalid={!!error} aria-describedby={error ? `${inputId}-err` : undefined}
        className={cn("w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-100", error && "border-danger", className)}
        {...rest} />
      {error && <p id={`${inputId}-err`} className="mt-1 text-xs text-danger">{error}</p>}
    </div>
  );
});
export default Input;
