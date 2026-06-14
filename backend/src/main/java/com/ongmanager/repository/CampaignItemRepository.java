package com.ongmanager.repository;
import com.ongmanager.entity.CampaignItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CampaignItemRepository extends JpaRepository<CampaignItem, Long> {
    List<CampaignItem> findByCampaign_Id(Long campaignId);
    Optional<CampaignItem> findByCampaign_IdAndNameIgnoreCase(Long campaignId, String name);
}
