package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.MemberProofImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProofImageRepository extends JpaRepository<MemberProofImage, Long> {
    // returns latest proof for donation (or null)
    MemberProofImage findTopByDonationIdOrderByUploadedAtDesc(Long donationId);
}
