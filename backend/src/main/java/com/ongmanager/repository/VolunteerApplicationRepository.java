package com.ongmanager.repository;
import com.ongmanager.entity.ApplicationStatus;
import com.ongmanager.entity.User;
import com.ongmanager.entity.VolunteerApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface VolunteerApplicationRepository extends JpaRepository<VolunteerApplication, Long> {
    Page<VolunteerApplication> findByVolunteer_Id(Long volunteerId, Pageable pageable);
    Page<VolunteerApplication> findByVolunteer_IdAndStatus(Long volunteerId, ApplicationStatus status, Pageable pageable);
    Page<VolunteerApplication> findByOpportunity_Id(Long opportunityId, Pageable pageable);
    boolean existsByOpportunity_IdAndVolunteer_Id(Long opportunityId, Long volunteerId);
    long countByOpportunity_Ngo_IdAndStatus(Long ngoId, ApplicationStatus status);
    java.util.List<VolunteerApplication> findByOpportunity_Ngo_Id(Long ngoId);
    java.util.List<VolunteerApplication> findByOpportunity_Ngo_IdAndStatus(Long ngoId, ApplicationStatus status);

    @Query("""
        SELECT DISTINCT a.volunteer FROM VolunteerApplication a
        WHERE a.opportunity.ngo.id = :ngoId AND a.status = :status
    """)
    java.util.List<User> findVolunteersByNgoIdAndStatus(@Param("ngoId") Long ngoId, @Param("status") ApplicationStatus status);
}
