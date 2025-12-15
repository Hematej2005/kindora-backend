package com.kindora.kindora_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.MemberProofImage;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.repository.MemberProofImageRepository;
import com.kindora.kindora_backend.repository.MemberRepository;
import com.kindora.kindora_backend.util.AuthUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class DonationUpdateService {

    private final DonationRepository donationRepository;
    private final MemberProofImageRepository memberProofImageRepository;
    private final MemberRepository memberRepository;
    private final Cloudinary cloudinary;

    public DonationUpdateService(DonationRepository donationRepository,
                                 MemberProofImageRepository memberProofImageRepository,
                                 MemberRepository memberRepository,
                                 Cloudinary cloudinary) {
        this.donationRepository = donationRepository;
        this.memberProofImageRepository = memberProofImageRepository;
        this.memberRepository = memberRepository;
        this.cloudinary = cloudinary;
    }

    /**
     * Update donation status and optionally upload member proof image to Cloudinary.
     */
    @Transactional
    public Donation updateDonationStatusAndProof(Long donationId, String statusRaw, String proofBase64) throws Exception {

        if (donationId == null)
            throw new IllegalArgumentException("donation id required");

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateException("Donation not found: " + donationId));

        String status = (statusRaw == null || statusRaw.isBlank())
                ? null
                : statusRaw.trim().toUpperCase();

        Long userId = AuthUtil.getCurrentUserId();
        if (userId == null)
            throw new IllegalStateException("No authenticated user");

        // -----------------------------
        // CASE 1 — UNASSIGN (AVAILABLE)
        // -----------------------------
        if ("AVAILABLE".equalsIgnoreCase(status)) {
            donation.setAssignedMemberId(null);
            donation.setAssignedAt(null);
            donation.setStatus("AVAILABLE");
            return donationRepository.save(donation);
        }

        // -----------------------------
        // CASE 2 — ALL OTHER STATUSES
        // -----------------------------
        Member member = memberRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalStateException("Member profile required"));

        if ("PICKED".equals(status) || "ASSIGNED".equals(status)) {
            donation.setAssignedMemberId(member.getId());
            donation.setAssignedAt(Instant.now());
        }

        if ("DELIVERED".equals(status)) {
            if (donation.getAssignedMemberId() == null) {
                donation.setAssignedMemberId(member.getId());
            }
            donation.setAssignedAt(Instant.now());
        }

        donation.setStatus(status);

        // -----------------------------
        // PROOF IMAGE HANDLING (CLOUDINARY)
        // -----------------------------
        if (proofBase64 != null && !proofBase64.isBlank()) {

            String data = proofBase64;
            if (!data.startsWith("data:")) {
                data = "data:image/jpeg;base64," + data;
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    data,
                    ObjectUtils.asMap(
                            "folder", "kindora/member-proofs",
                            "resource_type", "image"
                    )
            );

            String imageUrl = uploadResult.get("secure_url").toString();

            MemberProofImage mpi = new MemberProofImage();
            mpi.setDonationId(donationId);
            mpi.setMemberId(member.getId());
            mpi.setImageUrl(imageUrl);
            mpi.setUploadedAt(Instant.now());

            memberProofImageRepository.save(mpi);
        }

        return donationRepository.save(donation);
    }
}

