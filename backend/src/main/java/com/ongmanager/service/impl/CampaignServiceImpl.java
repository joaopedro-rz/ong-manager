package com.ongmanager.service.impl;

import com.ongmanager.dto.request.CampaignItemRequest;
import com.ongmanager.dto.request.CampaignRequest;
import com.ongmanager.dto.request.CampaignUpdateRequest;
import com.ongmanager.dto.response.CampaignItemResponse;
import com.ongmanager.dto.response.CampaignResponse;
import com.ongmanager.dto.response.CampaignUpdateResponse;
import com.ongmanager.entity.*;
import com.ongmanager.exception.AppException;
import com.ongmanager.exception.ErrorCode;
import com.ongmanager.mapper.CampaignMapper;
import com.ongmanager.repository.*;
import com.ongmanager.security.SecurityUtils;
import com.ongmanager.service.interfaces.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final NgoRepository ngoRepository;
    private final CampaignItemRepository itemRepository;
    private final CampaignUpdateRepository updateRepository;
    private final CampaignMapper mapper;
    private final FileStorageService fileStorageService;

    @Override
    public CampaignResponse create(CampaignRequest req) {
        Ngo ngo = ngoRepository.findById(req.getNgoId())
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada"));
        ensureManager(ngo);
        Campaign c = Campaign.builder()
            .ngo(ngo).title(req.getTitle()).description(req.getDescription())
            .financialGoal(req.getFinancialGoal())
            .startDate(req.getStartDate()).endDate(req.getEndDate())
            .coverImageUrl(req.getCoverImageUrl())
            .status(req.getStatus() != null ? CampaignStatus.valueOf(req.getStatus()) : CampaignStatus.ACTIVE)
            .urgent(Boolean.TRUE.equals(req.getUrgent()))
            .build();
        return mapper.toResponse(campaignRepository.save(c));
    }

    @Override
    public CampaignResponse update(Long id, CampaignRequest req) {
        Campaign c = campaignRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        c.setTitle(req.getTitle());
        c.setDescription(req.getDescription());
        c.setFinancialGoal(req.getFinancialGoal());
        c.setStartDate(req.getStartDate());
        c.setEndDate(req.getEndDate());
        if (req.getCoverImageUrl() != null) {
            c.setCoverImageUrl(req.getCoverImageUrl());
        }
        c.setStatus(CampaignStatus.valueOf(req.getStatus()));
        c.setUrgent(Boolean.TRUE.equals(req.getUrgent()));
        return mapper.toResponse(campaignRepository.save(c));
    }

    @Override
    public CampaignResponse get(Long id) {
        return mapper.toResponse(campaignRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada")));
    }

    @Override
    public void delete(Long id) {
        Campaign c = campaignRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        campaignRepository.delete(c);
    }

    @Override
    public Page<CampaignResponse> search(CampaignStatus status, Long ngoId, Long categoryId, String city, String state, Boolean urgent, String q, Pageable pageable) {
        return campaignRepository.search(status, ngoId, categoryId, city, state, urgent, q, pageable).map(mapper::toResponse);
    }

    @Override
    public CampaignItemResponse addItem(Long campaignId, CampaignItemRequest req) {
        Campaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        CampaignItem i = CampaignItem.builder()
            .campaign(c).name(req.getName()).category(req.getCategory())
            .quantityNeeded(req.getQuantityNeeded()).quantityReceived(0)
            .unit(req.getUnit() != null ? req.getUnit() : "un")
            .build();
        return toItemResponse(itemRepository.save(i));
    }

    @Override
    public List<CampaignItemResponse> listItems(Long campaignId) {
        return itemRepository.findByCampaign_Id(campaignId).stream().map(this::toItemResponse).toList();
    }

    @Override
    public CampaignItemResponse updateItem(Long campaignId, Long itemId, CampaignItemRequest req) {
        Campaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        CampaignItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> AppException.notFound("Item nao encontrado"));
        if (!item.getCampaign().getId().equals(campaignId)) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Item nao pertence a campanha");
        }
        item.setName(req.getName());
        item.setCategory(req.getCategory());
        item.setQuantityNeeded(req.getQuantityNeeded());
        item.setUnit(req.getUnit() != null ? req.getUnit() : "un");
        return toItemResponse(itemRepository.save(item));
    }

    @Override
    public void deleteItem(Long campaignId, Long itemId) {
        Campaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        CampaignItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> AppException.notFound("Item nao encontrado"));
        if (!item.getCampaign().getId().equals(campaignId)) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Item nao pertence a campanha");
        }
        itemRepository.delete(item);
    }

    @Override
    public CampaignUpdateResponse publishUpdate(Long campaignId, CampaignUpdateRequest req) {
        Campaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        CampaignUpdate update = updateRepository.save(CampaignUpdate.builder()
            .campaign(c).title(req.getTitle()).content(req.getContent()).build());
        return toUpdateResponse(update);
    }

    @Override
    public List<CampaignUpdateResponse> listUpdates(Long campaignId) {
        return updateRepository.findByCampaign_IdOrderByCreatedAtDesc(campaignId).stream().map(this::toUpdateResponse).toList();
    }

    @Override
    public CampaignUpdateResponse updateUpdate(Long campaignId, Long updateId, CampaignUpdateRequest req) {
        Campaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        CampaignUpdate update = updateRepository.findById(updateId)
            .orElseThrow(() -> AppException.notFound("Atualizacao nao encontrada"));
        if (!update.getCampaign().getId().equals(campaignId)) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Atualizacao nao pertence a campanha");
        }
        update.setTitle(req.getTitle());
        update.setContent(req.getContent());
        return toUpdateResponse(updateRepository.save(update));
    }

    @Override
    public void deleteUpdate(Long campaignId, Long updateId) {
        Campaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        CampaignUpdate update = updateRepository.findById(updateId)
            .orElseThrow(() -> AppException.notFound("Atualizacao nao encontrada"));
        if (!update.getCampaign().getId().equals(campaignId)) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Atualizacao nao pertence a campanha");
        }
        updateRepository.delete(update);
    }

    @Override
    public CampaignResponse uploadCover(Long campaignId, MultipartFile file) {
        Campaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        ensureManager(c.getNgo());
        String coverUrl = fileStorageService.storeCampaignCover(file);
        c.setCoverImageUrl(coverUrl);
        return mapper.toResponse(campaignRepository.save(c));
    }

    private void ensureManager(Ngo ngo) {
        Long uid = SecurityUtils.currentUser().getId();
        if (!SecurityUtils.hasRole("ADMIN") && !ngo.getManager().getId().equals(uid)) {
            throw AppException.forbidden("Sem permissao sobre esta ONG");
        }
    }

    private CampaignItemResponse toItemResponse(CampaignItem item) {
        return new CampaignItemResponse(item.getId(), item.getName(), item.getCategory(), item.getQuantityNeeded(), item.getQuantityReceived(), item.getUnit());
    }

    private CampaignUpdateResponse toUpdateResponse(CampaignUpdate update) {
        return new CampaignUpdateResponse(update.getId(), update.getTitle(), update.getContent(), update.getCreatedAt());
    }
}
