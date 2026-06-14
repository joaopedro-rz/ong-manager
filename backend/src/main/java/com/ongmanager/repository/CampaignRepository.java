package com.ongmanager.repository;

import com.ongmanager.entity.Campaign;
import com.ongmanager.entity.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Query("""
        SELECT c FROM Campaign c
        LEFT JOIN c.ngo n
        LEFT JOIN n.category cat
        LEFT JOIN n.address addr
        WHERE (:status IS NULL OR c.status = :status)
          AND (:ngoId IS NULL OR n.id = :ngoId)
          AND (:categoryId IS NULL OR cat.id = :categoryId)
          AND (COALESCE(:city, '') = '' OR LOWER(addr.city) LIKE LOWER(CONCAT('%', COALESCE(:city, ''), '%')))
          AND (COALESCE(:state, '') = '' OR LOWER(addr.state) = LOWER(COALESCE(:state, '')))
          AND (:urgent IS NULL OR c.urgent = :urgent)
          AND (COALESCE(:q, '') = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', COALESCE(:q, ''), '%')))
    """)
    Page<Campaign> search(@Param("status") CampaignStatus status,
                          @Param("ngoId") Long ngoId,
                          @Param("categoryId") Long categoryId,
                          @Param("city") String city,
                          @Param("state") String state,
                          @Param("urgent") Boolean urgent,
                          @Param("q") String q,
                          Pageable pageable);

    Page<Campaign> findByNgo_Id(Long ngoId, Pageable pageable);
    long countByStatus(CampaignStatus status);
    long countByNgo_Id(Long ngoId);
    long countByNgo_IdAndStatus(Long ngoId, CampaignStatus status);
}
