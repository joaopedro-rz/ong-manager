package com.ongmanager.service.interfaces;

import com.ongmanager.dto.request.CampaignItemRequest;
import com.ongmanager.dto.request.CampaignRequest;
import com.ongmanager.dto.request.CampaignUpdateRequest;
import com.ongmanager.dto.response.CampaignItemResponse;
import com.ongmanager.dto.response.CampaignResponse;
import com.ongmanager.dto.response.CampaignUpdateResponse;
import com.ongmanager.entity.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CampaignService {
    CampaignResponse create(CampaignRequest req);
    CampaignResponse update(Long id, CampaignRequest req);
    CampaignResponse get(Long id);
    void delete(Long id);
    Page<CampaignResponse> search(CampaignStatus status, Long ngoId, Long categoryId, String city, String state, Boolean urgent, String q, Pageable pageable);
    CampaignItemResponse addItem(Long campaignId, CampaignItemRequest req);
    List<CampaignItemResponse> listItems(Long campaignId);
    CampaignItemResponse updateItem(Long campaignId, Long itemId, CampaignItemRequest req);
    void deleteItem(Long campaignId, Long itemId);
    CampaignUpdateResponse publishUpdate(Long campaignId, CampaignUpdateRequest req);
    List<CampaignUpdateResponse> listUpdates(Long campaignId);
    CampaignUpdateResponse updateUpdate(Long campaignId, Long updateId, CampaignUpdateRequest req);
    void deleteUpdate(Long campaignId, Long updateId);
    CampaignResponse uploadCover(Long campaignId, MultipartFile file);
}
