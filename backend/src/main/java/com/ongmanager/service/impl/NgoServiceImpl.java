package com.ongmanager.service.impl;

import com.ongmanager.dto.request.NgoRequest;
import com.ongmanager.dto.response.NgoCategoryResponse;
import com.ongmanager.dto.response.NgoResponse;
import com.ongmanager.entity.*;
import com.ongmanager.exception.AppException;
import com.ongmanager.mapper.NgoMapper;
import com.ongmanager.repository.NgoCategoryRepository;
import com.ongmanager.repository.NgoRepository;
import com.ongmanager.security.SecurityUtils;
import com.ongmanager.service.interfaces.NgoService;
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
public class NgoServiceImpl implements NgoService {

    private final NgoRepository ngoRepository;
    private final NgoCategoryRepository categoryRepository;
    private final NgoMapper mapper;
    private final FileStorageService fileStorageService;

    @Override
    public NgoResponse create(NgoRequest req) {
        User current = SecurityUtils.currentUser();
        if (ngoRepository.findByCnpj(req.getCnpj()).isPresent()) {
            throw AppException.conflict("CNPJ ja cadastrado");
        }
        Ngo ngo = Ngo.builder()
            .name(req.getName()).cnpj(req.getCnpj())
            .description(req.getDescription()).phone(req.getPhone())
            .website(req.getWebsite()).socialMedia(req.getSocialMedia())
            .logoUrl(req.getLogoUrl()).certifications(req.getCertifications())
            .allowVolunteers(Boolean.TRUE.equals(req.getAllowVolunteers()))
            .volunteerSlots(req.getVolunteerSlots() != null ? req.getVolunteerSlots() : 0)
            .status(NgoStatus.PENDING).manager(current)
            .build();

        if (req.getCategoryId() != null) {
            ngo.setCategory(categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> AppException.notFound("Categoria nao encontrada")));
        }
        if (req.getAddress() != null) {
            Address a = ngo.getAddress();
            if (a == null) {
                a = new Address();
                ngo.setAddress(a);
            }
            if (req.getAddress().getStreet() != null) a.setStreet(req.getAddress().getStreet());
            if (req.getAddress().getNumber() != null) a.setNumber(req.getAddress().getNumber());
            if (req.getAddress().getComplement() != null) a.setComplement(req.getAddress().getComplement());
            if (req.getAddress().getDistrict() != null) a.setDistrict(req.getAddress().getDistrict());
            if (req.getAddress().getCity() != null) a.setCity(req.getAddress().getCity());
            if (req.getAddress().getState() != null) a.setState(req.getAddress().getState());
            if (req.getAddress().getZipCode() != null) a.setZipCode(req.getAddress().getZipCode());
            if (req.getAddress().getCountry() != null) a.setCountry(req.getAddress().getCountry());
        }
        return mapper.toResponse(ngoRepository.save(ngo));
    }

    @Override
    public NgoResponse update(Long id, NgoRequest req) {
        Ngo ngo = findOwned(id);
        ngo.setName(req.getName());
        ngo.setDescription(req.getDescription());
        ngo.setPhone(req.getPhone());
        ngo.setWebsite(req.getWebsite());
        ngo.setSocialMedia(req.getSocialMedia());
        ngo.setLogoUrl(req.getLogoUrl());
        ngo.setCertifications(req.getCertifications());
        ngo.setAllowVolunteers(Boolean.TRUE.equals(req.getAllowVolunteers()));
        ngo.setVolunteerSlots(req.getVolunteerSlots() != null ? req.getVolunteerSlots() : 0);
        if (req.getCategoryId() != null) {
            ngo.setCategory(categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> AppException.notFound("Categoria nao encontrada")));
        }
        if (req.getAddress() != null) {
            Address a = ngo.getAddress();
            if (a == null) {
                a = new Address();
                ngo.setAddress(a);
            }
            if (req.getAddress().getStreet() != null) a.setStreet(req.getAddress().getStreet());
            if (req.getAddress().getNumber() != null) a.setNumber(req.getAddress().getNumber());
            if (req.getAddress().getComplement() != null) a.setComplement(req.getAddress().getComplement());
            if (req.getAddress().getDistrict() != null) a.setDistrict(req.getAddress().getDistrict());
            if (req.getAddress().getCity() != null) a.setCity(req.getAddress().getCity());
            if (req.getAddress().getState() != null) a.setState(req.getAddress().getState());
            if (req.getAddress().getZipCode() != null) a.setZipCode(req.getAddress().getZipCode());
            if (req.getAddress().getCountry() != null) a.setCountry(req.getAddress().getCountry());
        }
        return mapper.toResponse(ngoRepository.save(ngo));
    }

    @Override
    public NgoResponse get(Long id) {
        return mapper.toResponse(ngoRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada")));
    }

    @Override
    public NgoResponse getPublicActive(Long id) {
        return mapper.toResponse(ngoRepository.findByIdAndStatus(id, NgoStatus.ACTIVE)
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada")));
    }

    @Override
    public Page<NgoResponse> searchPublic(String q, Long categoryId, Pageable pageable) {
        return ngoRepository.searchPublic(NgoStatus.ACTIVE, q, categoryId, pageable).map(mapper::toResponse);
    }

    @Override
    public Page<NgoResponse> moderationList(String q, Long categoryId, NgoStatus status, Pageable pageable) {
        return ngoRepository.searchModeration(status, q, categoryId, pageable).map(mapper::toResponse);
    }

    @Override
    public List<NgoCategoryResponse> categories() {
        return categoryRepository.findAll().stream()
            .map(c -> new NgoCategoryResponse(c.getId(), c.getName()))
            .toList();
    }

    @Override
    public NgoResponse uploadLogo(Long id, MultipartFile file) {
        Ngo ngo = findOwned(id);
        String logoUrl = fileStorageService.storeNgoLogo(file);
        ngo.setLogoUrl(logoUrl);
        return mapper.toResponse(ngoRepository.save(ngo));
    }

    @Override
    public Page<NgoResponse> myNgos(Pageable pageable) {
        Long uid = SecurityUtils.currentUser().getId();
        return ngoRepository.findByManager_Id(uid, pageable).map(mapper::toResponse);
    }

    @Override
    public NgoResponse changeStatus(Long id, NgoStatus status) {
        Ngo n = ngoRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada"));
        n.setStatus(status);
        return mapper.toResponse(ngoRepository.save(n));
    }

    @Override
    public void delete(Long id) {
        Ngo n = findOwned(id);
        ngoRepository.delete(n);
    }

    private Ngo findOwned(Long id) {
        Ngo n = ngoRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada"));
        Long uid = SecurityUtils.currentUser().getId();
        if (!SecurityUtils.hasRole("ADMIN") && !n.getManager().getId().equals(uid)) {
            throw AppException.forbidden("Sem permissao sobre esta ONG");
        }
        return n;
    }

    @Override
    public Page<NgoResponse> searchPublicWithFilters(NgoStatus status, Boolean allowVolunteers, String search, String city, String state, Long categoryId, Pageable pageable) {
        return ngoRepository.searchPublicWithFilters(NgoStatus.ACTIVE, allowVolunteers, search, city, state, categoryId, pageable).map(mapper::toResponse);
    }
}
