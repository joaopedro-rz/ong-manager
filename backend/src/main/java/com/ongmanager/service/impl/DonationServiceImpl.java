package com.ongmanager.service.impl;

import com.ongmanager.dto.request.DonationRequest;
import com.ongmanager.dto.response.DonationResponse;
import com.ongmanager.entity.*;
import com.ongmanager.exception.AppException;
import com.ongmanager.exception.ErrorCode;
import com.ongmanager.mapper.DonationMapper;
import com.ongmanager.repository.CampaignRepository;
import com.ongmanager.repository.CampaignItemRepository;
import com.ongmanager.repository.DonationRepository;
import com.ongmanager.repository.UserRepository;
import com.ongmanager.security.CustomUserDetails;
import com.ongmanager.security.SecurityUtils;
import com.ongmanager.service.interfaces.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignItemRepository campaignItemRepository;
    private final DonationMapper mapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_DONOR_EMAIL = "anon@demo.local";

    @Override
    public DonationResponse create(DonationRequest req) {
        Campaign c = campaignRepository.findById(req.getCampaignId())
            .orElseThrow(() -> AppException.notFound("Campanha nao encontrada"));
        if (SecurityUtils.hasRole("ONG_MANAGER")) {
            throw AppException.forbidden("Gestores de ONG nao podem doar");
        }
        DonationType type = DonationType.valueOf(req.getType());
        if (type == DonationType.FINANCIAL && (req.getAmount() == null || req.getAmount().signum() <= 0)) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Valor obrigatorio");
        }
        if (type == DonationType.MATERIAL && (req.getItemName() == null || req.getItemQuantity() == null)) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Item e quantidade obrigatorios");
        }
        User donor = resolveDonor();
        Donation d = Donation.builder()
            .donor(donor).campaign(c)
            .type(type).status(DonationStatus.PENDING)
            .donationDate(LocalDateTime.now())
            .amount(req.getAmount()).paymentMethod(req.getPaymentMethod())
            .receiptUrl(req.getReceiptUrl())
            .itemName(req.getItemName()).itemQuantity(req.getItemQuantity())
            .itemUnit(req.getItemUnit()).deliveryDate(req.getDeliveryDate())
            .notes(req.getNotes())
            .build();
        if (Boolean.TRUE.equals(req.getDemoConfirm()) && type == DonationType.FINANCIAL) {
            d.setStatus(DonationStatus.CONFIRMED);
            d.setConfirmedAt(LocalDateTime.now());
            if (d.getAmount() != null) {
                BigDecimal current = c.getRaisedAmount() == null ? BigDecimal.ZERO : c.getRaisedAmount();
                c.setRaisedAmount(current.add(d.getAmount()));
                campaignRepository.save(c);
            }
        }
        return mapper.toResponse(donationRepository.save(d));
    }

    @Override
    public DonationResponse confirm(Long id) {
        Donation d = findOrThrow(id);
        ensureNgoManager(d);
        d.setStatus(DonationStatus.CONFIRMED);
        d.setConfirmedAt(LocalDateTime.now());

        if (d.getType() == DonationType.FINANCIAL && d.getAmount() != null) {
            Campaign c = d.getCampaign();
            BigDecimal current = c.getRaisedAmount() == null ? BigDecimal.ZERO : c.getRaisedAmount();
            c.setRaisedAmount(current.add(d.getAmount()));
            campaignRepository.save(c);
        }

        if (d.getType() == DonationType.MATERIAL && d.getItemName() != null && d.getItemQuantity() != null) {
            campaignItemRepository.findByCampaign_IdAndNameIgnoreCase(d.getCampaign().getId(), d.getItemName())
                .ifPresent(item -> {
                    int received = item.getQuantityReceived() == null ? 0 : item.getQuantityReceived();
                    item.setQuantityReceived(received + d.getItemQuantity());
                    campaignItemRepository.save(item);
                });
        }
        return mapper.toResponse(donationRepository.save(d));
    }

    @Override
    public DonationResponse reject(Long id) {
        Donation d = findOrThrow(id);
        ensureNgoManager(d);
        d.setStatus(DonationStatus.REJECTED);
        return mapper.toResponse(donationRepository.save(d));
    }

    @Override
    public DonationResponse get(Long id) { return mapper.toResponse(findOrThrow(id)); }

    @Override
    public Page<DonationResponse> myDonations(Pageable pageable) {
        Long uid = SecurityUtils.currentUser().getId();
        return donationRepository.findByDonor_Id(uid, pageable).map(mapper::toResponse);
    }

    @Override
    public Page<DonationResponse> byCampaign(Long campaignId, Pageable pageable) {
        return donationRepository.findByCampaign_Id(campaignId, pageable).map(mapper::toResponse);
    }

    @Override
    public Page<DonationResponse> byNgo(Long ngoId, Pageable pageable) {
        return donationRepository.findByCampaign_Ngo_Id(ngoId, pageable).map(mapper::toResponse);
    }

    @Override
    public byte[] receiptPdf(Long id) {
        Donation d = findOrThrow(id);
        // PDF simplificado em texto (substituivel por iText). Geramos um PDF minimo.
        String text = "RECIBO DE DOACAO\n" +
            "Doador: " + d.getDonor().getName() + "\n" +
            "Campanha: " + d.getCampaign().getTitle() + "\n" +
            "ONG: " + d.getCampaign().getNgo().getName() + "\n" +
            "Tipo: " + d.getType() + "\n" +
            (d.getAmount() != null ? "Valor: R$ " + d.getAmount() + "\n" : "") +
            (d.getItemName() != null ? "Item: " + d.getItemName() + " x" + d.getItemQuantity() + "\n" : "") +
            "Data: " + d.getDonationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
            "Status: " + d.getStatus() + "\n";
        return buildSimplePdf(text);
    }

    private Donation findOrThrow(Long id) {
        return donationRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Doacao nao encontrada"));
    }

    private void ensureNgoManager(Donation d) {
        Long uid = SecurityUtils.currentUser().getId();
        if (!SecurityUtils.hasRole("ADMIN")
            && !d.getCampaign().getNgo().getManager().getId().equals(uid)) {
            throw AppException.forbidden("Sem permissao");
        }
    }

    /** PDF mínimo (text/plain dentro de PDF) para evitar dependência pesada nesta versão. */
    private byte[] buildSimplePdf(String content) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String pdf = "%PDF-1.4\n" +
                "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                "2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n" +
                "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Contents 4 0 R/Resources<</Font<</F1 5 0 R>>>>>>endobj\n" +
                "4 0 obj<</Length " + (content.length() + 60) + ">>stream\nBT /F1 12 Tf 50 750 Td (" +
                content.replace("(", "\\(").replace(")", "\\)").replace("\n", ") Tj T* (") + ") Tj ET\nendstream endobj\n" +
                "5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n" +
                "xref\n0 6\n0000000000 65535 f\n" +
                "trailer<</Size 6/Root 1 0 R>>\nstartxref\n0\n%%EOF";
            out.write(pdf.getBytes());
            return out.toByteArray();
        } catch (Exception e) {
            throw AppException.badRequest(ErrorCode.INTERNAL_ERROR, "Erro ao gerar PDF");
        }
    }

    private User resolveDonor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUser();
        }
        return userRepository.findByEmail(DEMO_DONOR_EMAIL).orElseGet(() -> {
            User demo = User.builder()
                .name("Doacao anonima")
                .email(DEMO_DONOR_EMAIL)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .enabled(true)
                .active(true)
                .build();
            return userRepository.save(demo);
        });
    }
}
