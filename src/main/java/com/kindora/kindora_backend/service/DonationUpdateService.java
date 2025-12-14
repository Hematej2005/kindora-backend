package com.kindora.kindora_backend.service;

import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.MemberProofImage;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.repository.MemberProofImageRepository;
import com.kindora.kindora_backend.repository.MemberRepository;
import com.kindora.kindora_backend.util.AuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class DonationUpdateService {

    private final DonationRepository donationRepository;
    private final MemberProofImageRepository memberProofImageRepository;
    private final MemberRepository memberRepository;

    @Value("${kindora.upload-dir:uploads}")
    private String uploadDir;

    public DonationUpdateService(DonationRepository donationRepository,
                                 MemberProofImageRepository memberProofImageRepository,
                                 MemberRepository memberRepository) {
        this.donationRepository = donationRepository;
        this.memberProofImageRepository = memberProofImageRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Update donation status and optionally save a member proof image.
     * IMPORTANT CHANGE:
     *  - When status = AVAILABLE (reject), member profile is NOT required.
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
        // Member profile required
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
        // PROOF IMAGE HANDLING
        // -----------------------------
        if (proofBase64 != null && !proofBase64.isBlank()) {

            String data = proofBase64;
            if (data.startsWith("data:")) {
                int comma = data.indexOf(',');
                if (comma > 0) data = data.substring(comma + 1);
            }

            byte[] bytes = Base64.getDecoder().decode(data);

            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String ext = ".jpg";
            if (proofBase64.startsWith("data:image/png")) ext = ".png";
            if (proofBase64.startsWith("data:image/webp")) ext = ".webp";
            if (proofBase64.startsWith("data:image/gif")) ext = ".gif";

            String filename = System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + ext;

            Path savedFile = dir.resolve(filename);
            Files.write(savedFile, bytes);

            String url = "/uploads/" + filename;

            MemberProofImage mpi = new MemberProofImage();
            mpi.setDonationId(donationId);
            mpi.setMemberId(member.getId());
            mpi.setImageUrl(url);
            mpi.setUploadedAt(Instant.now());
            memberProofImageRepository.save(mpi);
        }

        return donationRepository.save(donation);
    }
}
