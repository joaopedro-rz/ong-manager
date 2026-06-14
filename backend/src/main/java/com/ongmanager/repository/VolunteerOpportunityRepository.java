package com.ongmanager.repository;
import com.ongmanager.entity.VolunteerOpportunity;
import com.ongmanager.entity.OpportunityStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VolunteerOpportunityRepository extends JpaRepository<VolunteerOpportunity, Long> {
    Page<VolunteerOpportunity> findByNgo_Id(Long ngoId, Pageable pageable);
    Optional<VolunteerOpportunity> findFirstByNgo_IdAndStatus(Long ngoId, OpportunityStatus status);
}
