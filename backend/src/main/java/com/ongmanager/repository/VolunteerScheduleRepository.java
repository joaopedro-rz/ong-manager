package com.ongmanager.repository;

import com.ongmanager.entity.VolunteerSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerScheduleRepository extends JpaRepository<VolunteerSchedule, Long> {
    Page<VolunteerSchedule> findByApplication_Opportunity_Id(Long opportunityId, Pageable pageable);
    Page<VolunteerSchedule> findByApplication_Volunteer_Id(Long volunteerId, Pageable pageable);
}

