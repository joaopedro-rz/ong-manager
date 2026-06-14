package com.ongmanager.repository;

import com.ongmanager.entity.Ngo;
import com.ongmanager.entity.NgoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NgoRepository extends JpaRepository<Ngo, Long> {

    @Query("""
        SELECT n FROM Ngo n
        LEFT JOIN n.category cat
        WHERE (:status IS NULL OR n.status = :status)
          AND (COALESCE(:q, '') = '' OR LOWER(n.name) LIKE LOWER(CONCAT('%', COALESCE(:q, ''), '%')))
          AND (:categoryId IS NULL OR cat.id = :categoryId)
    """)
    Page<Ngo> searchPublic(@Param("status") NgoStatus status,
                           @Param("q") String q,
                           @Param("categoryId") Long categoryId,
                           Pageable pageable);

    @Query("""
        SELECT n FROM Ngo n
        LEFT JOIN n.category cat
        LEFT JOIN n.address addr
        WHERE (:status IS NULL OR n.status = :status)
          AND (:allowVolunteers IS NULL OR n.allowVolunteers = :allowVolunteers)
          AND (COALESCE(:search, '') = '' OR LOWER(n.name) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')))
          AND (:categoryId IS NULL OR cat.id = :categoryId)
          AND (COALESCE(:city, '') = '' OR LOWER(addr.city) = LOWER(:city))
          AND (COALESCE(:state, '') = '' OR LOWER(addr.state) = LOWER(:state))
    """)
    Page<Ngo> searchPublicWithFilters(@Param("status") NgoStatus status,
                                     @Param("allowVolunteers") Boolean allowVolunteers,
                                     @Param("search") String search,
                                     @Param("city") String city,
                                     @Param("state") String state,
                                     @Param("categoryId") Long categoryId,
                                     Pageable pageable);

    Page<Ngo> findByManager_Id(Long managerId, Pageable pageable);

    Optional<Ngo> findByIdAndStatus(Long id, NgoStatus status);

    @Query("""
        SELECT n FROM Ngo n
        LEFT JOIN n.category cat
        WHERE (:status IS NULL OR n.status = :status)
          AND (COALESCE(:q, '') = '' OR LOWER(n.name) LIKE LOWER(CONCAT('%', COALESCE(:q, ''), '%')))
          AND (:categoryId IS NULL OR cat.id = :categoryId)
    """)
    Page<Ngo> searchModeration(@Param("status") NgoStatus status,
                               @Param("q") String q,
                               @Param("categoryId") Long categoryId,
                               Pageable pageable);

    Optional<Ngo> findByCnpj(String cnpj);
}
