package com.ongmanager.controller;

import com.ongmanager.dto.request.CampaignItemRequest;
import com.ongmanager.dto.request.CampaignRequest;
import com.ongmanager.dto.request.CampaignUpdateRequest;
import com.ongmanager.dto.response.ApiResponse;
import com.ongmanager.dto.response.CampaignItemResponse;
import com.ongmanager.dto.response.CampaignResponse;
import com.ongmanager.dto.response.CampaignUpdateResponse;
import com.ongmanager.entity.CampaignStatus;
import com.ongmanager.service.interfaces.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService service;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> publicList(
        @RequestParam(required=false) CampaignStatus status,
        @RequestParam(required=false) Long ngoId,
        @RequestParam(required=false) Long categoryId,
        @RequestParam(required=false) String city,
        @RequestParam(required=false) String state,
        @RequestParam(required=false) Boolean urgent,
        @RequestParam(required=false) String q,
        Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.search(status, ngoId, categoryId, city, state, urgent, q, pageable)));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> publicGet(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> create(@Valid @RequestBody CampaignRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(req), "Campanha criada"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> update(@PathVariable Long id, @Valid @RequestBody CampaignRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignItemResponse>> addItem(@PathVariable Long id, @Valid @RequestBody CampaignItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.addItem(id, req), "Item adicionado"));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<CampaignItemResponse>>> listItems(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.listItems(id)));
    }

    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignItemResponse>> updateItem(@PathVariable Long id, @PathVariable Long itemId, @Valid @RequestBody CampaignItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateItem(id, itemId, req), "Item atualizado"));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
        service.deleteItem(id, itemId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Item removido"));
    }

    @PostMapping("/{id}/updates")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignUpdateResponse>> publishUpdate(@PathVariable Long id, @Valid @RequestBody CampaignUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.publishUpdate(id, req), "Atualizacao publicada"));
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<ApiResponse<List<CampaignUpdateResponse>>> listUpdates(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.listUpdates(id)));
    }

    @PutMapping("/{id}/updates/{updateId}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignUpdateResponse>> updateUpdate(@PathVariable Long id, @PathVariable Long updateId, @Valid @RequestBody CampaignUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateUpdate(id, updateId, req), "Atualizacao atualizada"));
    }

    @DeleteMapping("/{id}/updates/{updateId}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteUpdate(@PathVariable Long id, @PathVariable Long updateId) {
        service.deleteUpdate(id, updateId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Atualizacao removida"));
    }

    @PostMapping("/{id}/cover")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(service.uploadCover(id, file), "Capa atualizada"));
    }
}
