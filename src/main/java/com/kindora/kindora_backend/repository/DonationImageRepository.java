package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.DonationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationImageRepository extends JpaRepository<DonationImage, Long> {
    List<DonationImage> findByDonationId(Long donationId);
    Optional<DonationImage> findFirstByDonationIdOrderByIdAsc(Long donationId);
}
