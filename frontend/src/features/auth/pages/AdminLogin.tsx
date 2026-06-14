import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate, Link } from "react-router-dom";
import { toast } from "sonner";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import { useAuth } from "@/hooks/useAuth";

const schema = z.object({
  email: z.string().email("E-mail inválido"),
  password: z.string().min(8, "Mínimo 8 caracteres"),
});

type Form = z.infer<typeof schema>;

export default function AdminLogin() {
  const { login } = useAuth();
  const nav = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (d: Form) => {
    try {
      await login(d.email, d.password);
      toast.success("Bem-vindo, admin!");
      nav("/admin/ongs");
    } catch (e: any) {
      toast.error(e.response?.data?.error?.message ?? "Falha no login");
    }
  };

  return (
    <div className="max-w-md mx-auto px-4 py-12">
      <div className="mb-3">
        <Link to="/" className="text-sm text-slate-600 hover:text-primary-600">Voltar ao início</Link>
      </div>
      <h1 className="text-2xl font-bold mb-6">Acesso Admin</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-xl shadow-sm p-6 space-y-4">
        <Input label="E-mail" type="email" {...register("email")} error={errors.email?.message} />
        <Input label="Senha" type="password" {...register("password")} error={errors.password?.message} />
        <Button type="submit" loading={isSubmitting} className="w-full">Entrar</Button>
        <p className="text-sm text-center text-slate-600">
          Não é admin? <Link to="/login" className="text-primary-600 hover:underline">Ir para login</Link>
        </p>
      </form>
    </div>
  );
}

