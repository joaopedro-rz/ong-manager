export interface User {
  id: number; name: string; email: string; phone?: string;
  profileImageUrl?: string; enabled: boolean; roles: string[];
}
export interface AuthData {
  accessToken: string; refreshToken: string;
  tokenType: string; expiresIn: number; user: User;
}
export interface ApiResponse<T> { success: boolean; data: T; message: string; timestamp: string; }
export interface PageResponse<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }
export interface NgoCategory { id: number; name: string; }
export interface Ngo { id: number; name: string; cnpj: string; description?: string; phone?: string; website?: string; logoUrl?: string; status: string; categoryName?: string; city?: string; state?: string; managerId: number; allowVolunteers?: boolean; volunteerSlots?: number; }
export interface CampaignItem { id: number; name: string; category?: string; quantityNeeded: number; quantityReceived: number; unit: string; }
export interface CampaignUpdate { id: number; title: string; content: string; createdAt: string; }
export interface Campaign { id: number; ngoId: number; ngoName: string; title: string; description?: string; financialGoal?: number; raisedAmount: number; startDate: string; endDate?: string; coverImageUrl?: string; status: string; urgent: boolean; categoryName?: string; ngoCity?: string; ngoState?: string; items?: CampaignItem[]; updates?: CampaignUpdate[]; }
export interface Donation { id: number; donorId: number; donorName: string; campaignId: number; campaignTitle: string; type: "FINANCIAL"|"MATERIAL"; status: "PENDING"|"CONFIRMED"|"REJECTED"; donationDate: string; amount?: number; itemName?: string; itemQuantity?: number; itemUnit?: string; }
export interface VolunteerApplication {
  id: number;
  opportunityId: number;
  opportunityTitle: string;
  volunteerId: number;
  volunteerName: string;
  volunteerEmail: string;
  volunteerPhone?: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  motivation?: string;
  appliedAt: string;
}
