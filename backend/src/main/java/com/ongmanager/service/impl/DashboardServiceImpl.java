package com.ongmanager.service.impl;

import com.ongmanager.dto.response.ChartPoint;
import com.ongmanager.dto.response.DashboardSummary;
import com.ongmanager.dto.response.DonorDashboardSummary;
import com.ongmanager.dto.response.DonorRank;
import com.ongmanager.dto.response.NgoDashboardSummary;
import com.ongmanager.entity.*;
import com.ongmanager.exception.AppException;
import com.ongmanager.exception.ErrorCode;
import com.ongmanager.repository.*;
import com.ongmanager.repository.projection.DonorRankProjection;
import com.ongmanager.repository.projection.MonthlyDonationAgg;
import com.ongmanager.security.SecurityUtils;
import com.ongmanager.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final NgoRepository ngoRepository;
    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final VolunteerApplicationRepository volunteerApplicationRepository;

    @Override
    public DashboardSummary adminSummary(LocalDate from, LocalDate to) {
        try {
            long ngos = ngoRepository.count();
            long activeCampaigns = campaignRepository.countByStatus(CampaignStatus.ACTIVE);
            long confirmedDonations = donationRepository.countByStatus(DonationStatus.CONFIRMED);
            long volunteers = userRepository.countByRoles_Name("VOLUNTEER");
            BigDecimal totalRaised = Optional.ofNullable(donationRepository.sumConfirmedAmountAll(DonationStatus.CONFIRMED))
                .orElse(BigDecimal.ZERO);
            List<ChartPoint> monthly = List.of();
            try {
                if (isShortRange(from, to)) {
                    monthly = buildDailyTimeline(null, null, from, to);
                } else {
                    List<MonthlyDonationAgg> rows = Optional.ofNullable(
                        donationRepository.monthlyConfirmedAgg(null, null, toStart(from), toExclusiveEnd(to))
                    ).orElse(List.of());
                    monthly = mapMonthly(rows);
                    if (monthly.isEmpty() && confirmedDonations > 0) {
                        log.warn("Dashboard admin sem dados mensais via SQL; aplicando fallback (confirmadas={})", confirmedDonations);
                        monthly = fallbackMonthly(null, null, from, to);
                    }
                    log.info("Dashboard admin mensal: rows={}, confirmadas={}, totalRaised={}", monthly.size(), confirmedDonations, totalRaised);
                }
            } catch (Exception e) {
                log.error("Erro ao montar dados mensais do dashboard admin", e);
                monthly = isShortRange(from, to) ? buildDailyTimeline(null, null, from, to) : fallbackMonthly(null, null, from, to);
            }
            return new DashboardSummary(ngos, activeCampaigns, confirmedDonations, totalRaised, volunteers, monthly);
        } catch (Exception e) {
            log.error("Erro ao montar dashboard admin", e);
            return new DashboardSummary(0, 0, 0, BigDecimal.ZERO, 0, List.of());
        }
    }

    @Override
    public NgoDashboardSummary ngoSummary(Long ngoId, LocalDate from, LocalDate to) {
        Ngo ngo = ngoRepository.findById(ngoId)
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada"));
        ensureNgoAccess(ngo);
        long totalCampaigns = campaignRepository.countByNgo_Id(ngoId);
        long activeCampaigns = campaignRepository.countByNgo_IdAndStatus(ngoId, CampaignStatus.ACTIVE);
        long totalConfirmedDonations = donationRepository.countByCampaign_Ngo_IdAndStatus(ngoId, DonationStatus.CONFIRMED);
        long totalMaterialDonations = donationRepository.countByCampaign_Ngo_IdAndStatusAndType(ngoId, DonationStatus.CONFIRMED, DonationType.MATERIAL);
        BigDecimal totalRaised = Optional.ofNullable(
            donationRepository.sumConfirmedAmountByNgo(ngoId, DonationStatus.CONFIRMED)
        ).orElse(BigDecimal.ZERO);
        long activeVolunteers = volunteerApplicationRepository.countByOpportunity_Ngo_IdAndStatus(ngoId, ApplicationStatus.APPROVED);
        List<ChartPoint> monthly = List.of();
        try {
            if (isShortRange(from, to)) {
                monthly = buildDailyTimeline(ngoId, null, from, to);
            } else {
                List<MonthlyDonationAgg> rows = Optional.ofNullable(
                    donationRepository.monthlyConfirmedAgg(ngoId, null, toStart(from), toExclusiveEnd(to))
                ).orElse(List.of());
                monthly = mapMonthly(rows);
                if (monthly.isEmpty() && totalConfirmedDonations > 0) {
                    log.warn("Dashboard ONG {} sem dados mensais via SQL; aplicando fallback", ngoId);
                    monthly = fallbackMonthly(ngoId, null, from, to);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao montar dados mensais do dashboard da ONG {}", ngoId, e);
            monthly = isShortRange(from, to) ? buildDailyTimeline(ngoId, null, from, to) : fallbackMonthly(ngoId, null, from, to);
        }
        List<DonorRank> topDonors = donationRepository.topDonorsByNgo(ngoId, 5).stream()
            .map(this::mapDonorRank)
            .collect(Collectors.toList());
        return new NgoDashboardSummary(
            ngo.getId(), ngo.getName(), totalCampaigns, activeCampaigns,
            totalConfirmedDonations, totalMaterialDonations, totalRaised,
            activeVolunteers, monthly, topDonors
        );
    }

    @Override
    public DonorDashboardSummary donorSummary(LocalDate from, LocalDate to) {
        User donor = SecurityUtils.currentUser();
        long totalConfirmedDonations = donationRepository.countByDonor_IdAndStatus(donor.getId(), DonationStatus.CONFIRMED);
        BigDecimal totalDonated = Optional.ofNullable(
            donationRepository.sumConfirmedAmountByDonor(donor.getId(), DonationStatus.CONFIRMED)
        ).orElse(BigDecimal.ZERO);
        long supportedNgos = donationRepository.countDistinctNgoByDonor(donor.getId(), DonationStatus.CONFIRMED);
        List<ChartPoint> monthly = List.of();
        try {
            if (isShortRange(from, to)) {
                monthly = buildDailyTimeline(null, donor.getId(), from, to);
            } else {
                List<MonthlyDonationAgg> rows = Optional.ofNullable(
                    donationRepository.monthlyConfirmedAgg(null, donor.getId(), toStart(from), toExclusiveEnd(to))
                ).orElse(List.of());
                monthly = mapMonthly(rows);
                if (monthly.isEmpty() && totalConfirmedDonations > 0) {
                    log.warn("Dashboard doador {} sem dados mensais via SQL; aplicando fallback", donor.getId());
                    monthly = fallbackMonthly(null, donor.getId(), from, to);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao montar dados mensais do dashboard do doador {}", donor.getId(), e);
            monthly = isShortRange(from, to) ? buildDailyTimeline(null, donor.getId(), from, to) : fallbackMonthly(null, donor.getId(), from, to);
        }
        return new DonorDashboardSummary(donor.getId(), donor.getName(), totalConfirmedDonations, totalDonated, supportedNgos, monthly);
    }

    @Override
    public byte[] ngoReportPdf(Long ngoId, LocalDate from, LocalDate to) {
        NgoDashboardSummary summary = ngoSummary(ngoId, from, to);
        String content = buildNgoReportText(summary);
        return buildSimplePdf(content);
    }

    @Override
    public byte[] ngoReportCsv(Long ngoId, LocalDate from, LocalDate to) {
        NgoDashboardSummary summary = ngoSummary(ngoId, from, to);
        StringBuilder sb = new StringBuilder();
        sb.append("metric,value\n");
        sb.append("ngo_id,").append(summary.getNgoId()).append("\n");
        sb.append("ngo_name,").append(summary.getNgoName()).append("\n");
        sb.append("total_campaigns,").append(summary.getTotalCampaigns()).append("\n");
        sb.append("active_campaigns,").append(summary.getActiveCampaigns()).append("\n");
        sb.append("total_confirmed_donations,").append(summary.getTotalConfirmedDonations()).append("\n");
        sb.append("total_material_donations,").append(summary.getTotalMaterialDonations()).append("\n");
        sb.append("total_raised,").append(summary.getTotalRaised()).append("\n");
        sb.append("active_volunteers,").append(summary.getActiveVolunteers()).append("\n");
        sb.append("\nmonthly_period,amount,count\n");
        for (ChartPoint point : summary.getMonthlyDonations()) {
            sb.append(point.getPeriod()).append(",")
              .append(point.getAmount()).append(",")
              .append(point.getCount()).append("\n");
        }
        sb.append("\nrank,donor_id,donor_name,total_amount\n");
        int rank = 1;
        for (DonorRank donor : summary.getTopDonors()) {
            sb.append(rank++).append(",")
              .append(donor.getDonorId()).append(",")
              .append(donor.getDonorName()).append(",")
              .append(donor.getTotalAmount()).append("\n");
        }
        return sb.toString().getBytes();
    }

    @Override
    public byte[] adminReportPdf(LocalDate from, LocalDate to) {
        DashboardSummary summary = adminSummary(from, to);
        String content = buildAdminReportText(summary);
        return buildSimplePdf(content);
    }

    private List<ChartPoint> mapMonthly(List<MonthlyDonationAgg> rows) {
        return rows.stream()
            .map(r -> new ChartPoint(r.getPeriod(), r.getAmount(), r.getCount()))
            .collect(Collectors.toList());
    }

    private DonorRank mapDonorRank(DonorRankProjection row) {
        return new DonorRank(row.getDonorId(), row.getDonorName(), row.getTotalAmount());
    }

    private void ensureNgoAccess(Ngo ngo) {
        Long uid = SecurityUtils.currentUser().getId();
        if (!SecurityUtils.hasRole("ADMIN") && !ngo.getManager().getId().equals(uid)) {
            throw AppException.forbidden("Sem permissao sobre esta ONG");
        }
    }

    private LocalDateTime toStart(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime toExclusiveEnd(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private String buildNgoReportText(NgoDashboardSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("RELATORIO DE TRANSPARENCIA\n");
        sb.append("ONG: ").append(summary.getNgoName()).append("\n");
        sb.append("Campanhas: ").append(summary.getTotalCampaigns()).append("\n");
        sb.append("Campanhas ativas: ").append(summary.getActiveCampaigns()).append("\n");
        sb.append("Doacoes confirmadas: ").append(summary.getTotalConfirmedDonations()).append("\n");
        sb.append("Doacoes materiais: ").append(summary.getTotalMaterialDonations()).append("\n");
        sb.append("Total arrecadado: R$ ").append(summary.getTotalRaised()).append("\n");
        sb.append("Voluntarios ativos: ").append(summary.getActiveVolunteers()).append("\n\n");
        sb.append("TOP DOADORES\n");
        int idx = 1;
        for (DonorRank donor : summary.getTopDonors()) {
            sb.append(idx++).append(". ")
              .append(donor.getDonorName()).append(" - R$ ")
              .append(donor.getTotalAmount()).append("\n");
        }
        sb.append("\nDOACOES MENSAIS\n");
        for (ChartPoint point : summary.getMonthlyDonations()) {
            sb.append(point.getPeriod()).append(" -> R$ ")
              .append(point.getAmount()).append(" (")
              .append(point.getCount()).append(")\n");
        }
        return sb.toString();
    }

    private String buildAdminReportText(DashboardSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("RELATORIO DO DASHBOARD ADMIN\n");
        sb.append("ONGs: ").append(summary.getTotalNgos()).append("\n");
        sb.append("Campanhas ativas: ").append(summary.getTotalActiveCampaigns()).append("\n");
        sb.append("Doacoes confirmadas: ").append(summary.getTotalConfirmedDonations()).append("\n");
        sb.append("Total arrecadado: R$ ").append(summary.getTotalRaised()).append("\n");
        sb.append("Voluntarios: ").append(summary.getTotalVolunteers()).append("\n\n");
        sb.append("DOACOES MENSAIS\n");
        for (ChartPoint point : summary.getMonthlyDonations()) {
            sb.append(point.getPeriod()).append(" -> R$ ")
              .append(point.getAmount()).append(" (")
              .append(point.getCount()).append(")\n");
        }
        return sb.toString();
    }

    /** PDF minimo (texto embutido) para evitar dependencia extra. */
    private byte[] buildSimplePdf(String content) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<String> lines = wrapLines(content, 90);
            StringBuilder stream = new StringBuilder();
            stream.append("BT /F1 12 Tf 50 750 Td\n");
            boolean first = true;
            for (String line : lines) {
                String safe = escapePdfText(toAscii(line));
                if (!first) {
                    stream.append("0 -14 Td\n");
                }
                stream.append("(").append(safe).append(") Tj\n");
                first = false;
            }
            stream.append("ET\n");

            byte[] streamBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);
            String header = "%PDF-1.4\n" +
                "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                "2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n" +
                "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Contents 4 0 R/Resources<</Font<</F1 5 0 R>>>>>>endobj\n" +
                "4 0 obj<</Length " + streamBytes.length + ">>stream\n";
            String footer = "endstream endobj\n" +
                "5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n" +
                "xref\n0 6\n0000000000 65535 f\n" +
                "trailer<</Size 6/Root 1 0 R>>\nstartxref\n0\n%%EOF";

            out.write(header.getBytes(StandardCharsets.US_ASCII));
            out.write(streamBytes);
            out.write(footer.getBytes(StandardCharsets.US_ASCII));
            return out.toByteArray();
        } catch (Exception e) {
            throw AppException.badRequest(ErrorCode.INTERNAL_ERROR, "Erro ao gerar PDF");
        }
    }

    private List<String> wrapLines(String content, int maxLen) {
        List<String> lines = new ArrayList<>();
        for (String raw : content.split("\r?\n")) {
            String line = raw == null ? "" : raw;
            if (line.length() <= maxLen) {
                lines.add(line);
                continue;
            }
            int idx = 0;
            while (idx < line.length()) {
                int end = Math.min(idx + maxLen, line.length());
                lines.add(line.substring(idx, end));
                idx = end;
            }
        }
        return lines;
    }

    private String toAscii(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private String escapePdfText(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private boolean isShortRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) return false;
        return ChronoUnit.DAYS.between(from, to) <= 31;
    }

    private List<ChartPoint> buildDailyTimeline(Long ngoId, Long donorId, LocalDate from, LocalDate to) {
        LocalDate startDate = from != null ? from : LocalDate.now().minusDays(29);
        LocalDate endDate = to != null ? to : LocalDate.now();
        List<Object[]> rows = donationRepository.confirmedDonationTimeline(
            ngoId,
            donorId,
            DonationStatus.CONFIRMED,
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay()
        );
        Map<LocalDate, BigDecimal> totals = new TreeMap<>();
        Map<LocalDate, Long> counts = new TreeMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 1 || row[0] == null) continue;
            LocalDate date = ((LocalDateTime) row[0]).toLocalDate();
            BigDecimal amount = row.length > 1 && row[1] instanceof BigDecimal
                ? (BigDecimal) row[1]
                : BigDecimal.ZERO;
            totals.merge(date, amount, BigDecimal::add);
            counts.merge(date, 1L, Long::sum);
        }
        List<ChartPoint> points = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            BigDecimal amount = totals.getOrDefault(cursor, BigDecimal.ZERO);
            Long count = counts.getOrDefault(cursor, 0L);
            points.add(new ChartPoint(cursor.format(DateTimeFormatter.ISO_LOCAL_DATE), amount, count));
            cursor = cursor.plusDays(1);
        }
        return points;
    }

    private List<ChartPoint> fallbackMonthly(Long ngoId, Long donorId, LocalDate from, LocalDate to) {
        LocalDateTime start = toStart(from);
        LocalDateTime end = toExclusiveEnd(to);
        if (start == null) {
            start = LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
        }
        if (end == null) {
            end = LocalDate.now().plusDays(1).atStartOfDay();
        }
        List<Object[]> rows = donationRepository.confirmedDonationTimeline(
            ngoId,
            donorId,
            DonationStatus.CONFIRMED,
            start,
            end
        );
        Map<YearMonth, BigDecimal> totals = new TreeMap<>();
        Map<YearMonth, Long> counts = new TreeMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 1 || row[0] == null) continue;
            LocalDateTime date = (LocalDateTime) row[0];
            YearMonth ym = YearMonth.from(date);
            BigDecimal amount = row.length > 1 && row[1] instanceof BigDecimal
                ? (BigDecimal) row[1]
                : BigDecimal.ZERO;
            totals.merge(ym, amount, BigDecimal::add);
            counts.merge(ym, 1L, Long::sum);
        }
        List<ChartPoint> points = new ArrayList<>();
        YearMonth cursor = YearMonth.from(start);
        YearMonth endYm = YearMonth.from(end.minusDays(1));
        while (!cursor.isAfter(endYm)) {
            BigDecimal amount = totals.getOrDefault(cursor, BigDecimal.ZERO);
            Long count = counts.getOrDefault(cursor, 0L);
            String period = String.format("%04d-%02d", cursor.getYear(), cursor.getMonthValue());
            points.add(new ChartPoint(period, amount, count));
            cursor = cursor.plusMonths(1);
        }
        return points;
    }

    private Totals totalsFromTimeline(Long ngoId, Long donorId, LocalDate from, LocalDate to) {
        LocalDateTime start = toStart(from);
        LocalDateTime end = toExclusiveEnd(to);
        if (start == null) {
            start = LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
        }
        if (end == null) {
            end = LocalDate.now().plusDays(1).atStartOfDay();
        }
        List<Object[]> rows = donationRepository.confirmedDonationTimeline(
            ngoId,
            donorId,
            DonationStatus.CONFIRMED,
            start,
            end
        );
        long count = 0;
        BigDecimal amount = BigDecimal.ZERO;
        for (Object[] row : rows) {
            if (row == null || row.length == 0) continue;
            count += 1;
            if (row.length > 1 && row[1] instanceof BigDecimal) {
                amount = amount.add((BigDecimal) row[1]);
            }
        }
        return new Totals(count, amount);
    }

    private record Totals(long count, BigDecimal amount) {}
}
