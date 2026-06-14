package com.ongmanager.repository;
import com.ongmanager.entity.CampaignUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CampaignUpdateRepository extends JpaRepository<CampaignUpdate, Long> {
    List<CampaignUpdate> findByCampaign_IdOrderByCreatedAtDesc(Long campaignId);
}
