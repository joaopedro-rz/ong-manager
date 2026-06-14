package com.ongmanager.service.interfaces;

import com.ongmanager.dto.request.DonationRequest;
import com.ongmanager.dto.response.DonationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DonationService {
    DonationResponse create(DonationRequest req);
    DonationResponse confirm(Long id);
    DonationResponse reject(Long id);
    DonationResponse get(Long id);
    Page<DonationResponse> myDonations(Pageable pageable);
    Page<DonationResponse> byCampaign(Long campaignId, Pageable pageable);
    Page<DonationResponse> byNgo(Long ngoId, Pageable pageable);
    byte[] receiptPdf(Long id);
}
