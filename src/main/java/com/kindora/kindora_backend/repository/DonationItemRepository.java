package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.DonationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {
    List<DonationItem> findByDonationId(Long donationId);
}
