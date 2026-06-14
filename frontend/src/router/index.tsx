import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import Layout from "@/components/shared/Layout";
import Home from "@/pages/Home";
import Login from "@/features/auth/pages/Login";
import AdminLogin from "@/features/auth/pages/AdminLogin";
import Register from "@/features/auth/pages/Register";
import CampaignsPage from "@/features/campaigns/pages/CampaignsPage";
import CampaignDetail from "@/features/campaigns/pages/CampaignDetail";
import CampaignManagePage from "@/features/campaigns/pages/CampaignManagePage";
import CampaignCreatePage from "@/features/campaigns/pages/CampaignCreatePage";
import NgosPage from "@/features/ngos/pages/NgosPage";
import NgoPublicProfile from "@/features/ngos/pages/NgoPublicProfile";
import NgoManagePage from "@/features/ngos/pages/NgoManagePage";
import NgoModerationPage from "@/features/ngos/pages/NgoModerationPage";
import VolunteerPage from "@/features/volunteers/pages/VolunteerPage";
import Dashboard from "@/features/dashboard/pages/Dashboard";
import CheckoutPage from "@/features/donations/pages/CheckoutPage";
import NotFound from "@/pages/NotFound";

function Protected({ children, role }: { children: JSX.Element; role?: string | string[] }) {
  const { user, hasRole, loading } = useAuth();
  if (loading) return null;
  if (!user) return <Navigate to="/login" replace />;
  if (role) {
    const roles = Array.isArray(role) ? role : [role];
    if (!roles.some((r) => hasRole(r))) return <Navigate to="/" replace />;
  }
  return children;
}

export default function AppRouter() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/cadastro" element={<Register />} />
        <Route path="/campanhas" element={<CampaignsPage />} />
        <Route path="/campanhas/:id" element={<CampaignDetail />} />
        <Route path="/campanhas/criar" element={<Protected role={["ONG_MANAGER", "ADMIN"]}><CampaignCreatePage /></Protected>} />
        <Route path="/campanhas/gestao" element={<Protected role={["ONG_MANAGER", "ADMIN"]}><CampaignManagePage /></Protected>} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/ongs" element={<NgosPage />} />
        <Route path="/ongs/:id" element={<NgoPublicProfile />} />
        <Route path="/ongs/gestao" element={<Protected role={["ONG_MANAGER", "ADMIN"]}><NgoManagePage /></Protected>} />
        <Route path="/admin/ongs" element={<Protected role="ADMIN"><NgoModerationPage /></Protected>} />
        <Route path="/admin/campanhas" element={<Protected role="ADMIN"><CampaignManagePage /></Protected>} />
        <Route path="/voluntariado" element={<VolunteerPage />} />
        <Route path="/dashboard" element={<Protected role="ADMIN"><Dashboard /></Protected>} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}
