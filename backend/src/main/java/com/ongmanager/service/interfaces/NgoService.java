package com.ongmanager.service.interfaces;

import com.ongmanager.dto.request.NgoRequest;
import com.ongmanager.dto.response.NgoCategoryResponse;
import com.ongmanager.dto.response.NgoResponse;
import com.ongmanager.entity.NgoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NgoService {
    NgoResponse create(NgoRequest req);
    NgoResponse update(Long id, NgoRequest req);
    NgoResponse get(Long id);
    NgoResponse getPublicActive(Long id);
    Page<NgoResponse> searchPublic(String q, Long categoryId, Pageable pageable);
    Page<NgoResponse> moderationList(String q, Long categoryId, NgoStatus status, Pageable pageable);
    Page<NgoResponse> searchPublicWithFilters(NgoStatus status, Boolean allowVolunteers, String search, String city, String state, Long categoryId, Pageable pageable);
    List<NgoCategoryResponse> categories();
    NgoResponse uploadLogo(Long id, MultipartFile file);
    Page<NgoResponse> myNgos(Pageable pageable);
    NgoResponse changeStatus(Long id, NgoStatus status);
    void delete(Long id);
}
