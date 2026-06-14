package com.ongmanager.repository;

import com.ongmanager.entity.Donation;
import com.ongmanager.entity.DonationStatus;
import com.ongmanager.entity.DonationType;
import com.ongmanager.repository.projection.DonorRankProjection;
import com.ongmanager.repository.projection.MonthlyDonationAgg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Page<Donation> findByDonor_Id(Long donorId, Pageable pageable);
    Page<Donation> findByCampaign_Id(Long campaignId, Pageable pageable);
    Page<Donation> findByCampaign_Ngo_Id(Long ngoId, Pageable pageable);

    long countByStatus(DonationStatus status);
    long countByCampaign_Ngo_IdAndStatus(Long ngoId, DonationStatus status);
    long countByCampaign_Ngo_IdAndStatusAndType(Long ngoId, DonationStatus status, DonationType type);
    long countByDonor_IdAndStatus(Long donorId, DonationStatus status);

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0) FROM Donation d
        WHERE d.campaign.id = :campaignId
          AND d.status = :status
          AND d.type = com.ongmanager.entity.DonationType.FINANCIAL
    """)
    BigDecimal sumConfirmedAmount(@Param("campaignId") Long campaignId,
                                  @Param("status") DonationStatus status);

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0) FROM Donation d
        WHERE d.campaign.ngo.id = :ngoId
          AND d.status = :status
          AND d.type = com.ongmanager.entity.DonationType.FINANCIAL
    """)
    BigDecimal sumConfirmedAmountByNgo(@Param("ngoId") Long ngoId,
                                       @Param("status") DonationStatus status);

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0) FROM Donation d
        WHERE d.donor.id = :donorId
          AND d.status = :status
          AND d.type = com.ongmanager.entity.DonationType.FINANCIAL
    """)
    BigDecimal sumConfirmedAmountByDonor(@Param("donorId") Long donorId,
                                         @Param("status") DonationStatus status);

    @Query(value = """
        SELECT to_char(date_trunc('month', d.donation_date), 'YYYY-MM') AS period,
               COALESCE(SUM(CASE WHEN d.type = 'FINANCIAL' THEN d.amount ELSE 0 END), 0) AS amount,
               COUNT(*) AS count
        FROM donations d
        WHERE d.status = 'CONFIRMED'
          AND (:ngoId IS NULL OR d.campaign_id IN (SELECT c.id FROM campaigns c WHERE c.ngo_id = :ngoId))
          AND (:donorId IS NULL OR d.donor_id = :donorId)
          AND d.donation_date >= COALESCE(:fromDate, TIMESTAMP '1970-01-01')
          AND d.donation_date < COALESCE(:toDate, TIMESTAMP '9999-12-31')
        GROUP BY 1
        ORDER BY 1
    """, nativeQuery = true)
    List<MonthlyDonationAgg> monthlyConfirmedAgg(@Param("ngoId") Long ngoId,
                                                 @Param("donorId") Long donorId,
                                                 @Param("fromDate") LocalDateTime fromDate,
                                                 @Param("toDate") LocalDateTime toDate);

    @Query("""
        SELECT d.donationDate, d.amount
        FROM Donation d
        WHERE d.status = :status
          AND d.type = com.ongmanager.entity.DonationType.FINANCIAL
          AND (:ngoId IS NULL OR d.campaign.ngo.id = :ngoId)
          AND (:donorId IS NULL OR d.donor.id = :donorId)
          AND (:fromDate IS NULL OR d.donationDate >= :fromDate)
          AND (:toDate IS NULL OR d.donationDate < :toDate)
    """)
    List<Object[]> confirmedDonationTimeline(@Param("ngoId") Long ngoId,
                                             @Param("donorId") Long donorId,
                                             @Param("status") DonationStatus status,
                                             @Param("fromDate") LocalDateTime fromDate,
                                             @Param("toDate") LocalDateTime toDate);

    @Query(value = """
        SELECT d.donor_id AS donorId, u.name AS donorName,
               COALESCE(SUM(CASE WHEN d.type = 'FINANCIAL' THEN d.amount ELSE 0 END), 0) AS totalAmount
        FROM donations d
        JOIN users u ON u.id = d.donor_id
        JOIN campaigns c ON c.id = d.campaign_id
        WHERE d.status = 'CONFIRMED'
          AND c.ngo_id = :ngoId
        GROUP BY d.donor_id, u.name
        ORDER BY totalAmount DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<DonorRankProjection> topDonorsByNgo(@Param("ngoId") Long ngoId, @Param("limit") int limit);

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0) FROM Donation d
        WHERE d.status = :status
          AND d.type = com.ongmanager.entity.DonationType.FINANCIAL
    """)
    BigDecimal sumConfirmedAmountAll(@Param("status") DonationStatus status);

    @Query("""
        SELECT COUNT(DISTINCT d.campaign.ngo.id) FROM Donation d
        WHERE d.donor.id = :donorId
          AND d.status = :status
    """)
    long countDistinctNgoByDonor(@Param("donorId") Long donorId,
                                 @Param("status") DonationStatus status);
}
