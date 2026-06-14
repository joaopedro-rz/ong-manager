package com.ongmanager.service.impl;

import com.ongmanager.exception.AppException;
import com.ongmanager.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String storeNgoLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Arquivo de logo obrigatorio");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Formato invalido. Use JPG, PNG, WEBP ou GIF");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = (ext == null || ext.isBlank()) ? "png" : ext.toLowerCase();
        String fileName = "ngo-logo-" + UUID.randomUUID() + "." + safeExt;
        Path logoDir = Paths.get(uploadDir, "logos");
        try {
            Files.createDirectories(logoDir);
            Files.copy(file.getInputStream(), logoDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw AppException.badRequest(ErrorCode.INTERNAL_ERROR, "Falha ao salvar arquivo");
        }
        return "/uploads/logos/" + fileName;
    }

    public String storeCampaignCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Arquivo de capa obrigatorio");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Formato invalido. Use JPG, PNG, WEBP ou GIF");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = (ext == null || ext.isBlank()) ? "png" : ext.toLowerCase();
        String fileName = "campaign-cover-" + UUID.randomUUID() + "." + safeExt;
        Path coverDir = Paths.get(uploadDir, "covers");
        try {
            Files.createDirectories(coverDir);
            Files.copy(file.getInputStream(), coverDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw AppException.badRequest(ErrorCode.INTERNAL_ERROR, "Falha ao salvar arquivo");
        }
        return "/uploads/covers/" + fileName;
    }
}
