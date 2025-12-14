package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    // allow fetching donations by status
    List<Donation> findByStatus(String status);

    @Query(value = "SELECT *,(6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * " +
            "cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(lat)))) AS distance " +
            "FROM donations WHERE status = 'AVAILABLE' HAVING distance <= :radius " +
            "ORDER BY distance LIMIT :limit", nativeQuery = true)
    List<Donation> findNearby(@Param("lat") double lat,
                              @Param("lng") double lng,
                              @Param("radius") double radius,
                              @Param("limit") int limit);
    // Add these two methods to the existing DonationRepository interface
    List<Donation> findByAssignedMemberIdOrderByAssignedAtDesc(Long memberId);

    List<Donation> findByAssignedMemberIdAndStatusOrderByAssignedAtDesc(Long memberId, String status);

}
