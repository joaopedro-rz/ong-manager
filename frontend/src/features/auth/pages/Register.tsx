import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate, Link } from "react-router-dom";
import { toast } from "sonner";
import { api } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";

const schema = z.object({
  name: z.string().trim().min(2, "Nome obrigatório").max(150),
  email: z.string().email("E-mail inválido").max(180),
  password: z.string().min(8, "Mínimo 8 caracteres"),
  phone: z.string().optional(),
  role: z.enum(["VOLUNTEER","ONG_MANAGER"]),
});
type Form = z.infer<typeof schema>;

export default function Register() {
  const nav = useNavigate();
  const { login } = useAuth();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema), defaultValues: { role: "VOLUNTEER" }
  });
  const onSubmit = async (d: Form) => {
    try {
      await api.post("/auth/register", { ...d, roles: [d.role] });
      await login(d.email, d.password);
      toast.success("Conta criada com sucesso!");
      nav("/");
    } catch (e: any) { toast.error(e.response?.data?.error?.message ?? "Falha ao registrar"); }
  };
  return (
    <div className="max-w-md mx-auto px-4 py-12">
      <div className="mb-3">
        <Link to="/" className="text-sm text-slate-600 hover:text-primary-600">Voltar ao início</Link>
      </div>
      <h1 className="text-2xl font-bold mb-6">Criar conta</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-xl shadow-sm p-6 space-y-4">
        <Input label="Nome" {...register("name")} error={errors.name?.message} />
        <Input label="E-mail" type="email" {...register("email")} error={errors.email?.message} />
        <Input label="Senha" type="password" {...register("password")} error={errors.password?.message} />
        <Input label="Telefone (opcional)" {...register("phone")} />
        <div>
          <label htmlFor="role" className="block text-sm font-medium text-slate-700 mb-1">Tipo de conta</label>
          <select id="role" {...register("role")} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
            <option value="VOLUNTEER">Voluntário</option>
            <option value="ONG_MANAGER">Gestor de ONG</option>
          </select>
        </div>
        <Button type="submit" loading={isSubmitting} className="w-full">Cadastrar</Button>
        <p className="text-sm text-center text-slate-600">
          Já tem conta? <Link to="/login" className="text-primary-600 hover:underline">Entrar</Link>
        </p>
      </form>
    </div>
  );
}
