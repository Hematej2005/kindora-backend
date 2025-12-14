package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.MemberProofImage;
import com.kindora.kindora_backend.repository.MemberRepository;
import com.kindora.kindora_backend.repository.MemberProofImageRepository;
import com.kindora.kindora_backend.service.DonationUpdateService;
import com.kindora.kindora_backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DonationUpdateController {

    private final DonationUpdateService donationUpdateService;
    private final MemberRepository memberRepository;
    private final MemberProofImageRepository memberProofImageRepository;

    /**
     * Endpoint used by member to update status or upload proof.
     * Expects JSON: { id: 123, status: "Delivered", proofBase64: "data:image/png;base64,..." }
     *
     * Returns JSON that includes the updated donation and saved member proof (if any).
     *
     * Note: to support frontend fallback when /api/member/reject fails, we allow unassign
     * (status = "AVAILABLE") even if a Member profile lookup returns empty. Other updates still
     * require a member profile.
     */
    @PostMapping("/donations/update")
    public ResponseEntity<?> updateDonation(@RequestBody Map<String, Object> body) {
        try {
            Long userId;
            try {
                userId = AuthUtil.getLoggedUserId();
            } catch (Exception ex) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
            }

            Object idObj = body.get("id");
            if (idObj == null) return ResponseEntity.badRequest().body(Map.of("error", "id required"));

            Long id;
            try {
                id = Long.valueOf(idObj.toString());
            } catch (NumberFormatException nfe) {
                return ResponseEntity.badRequest().body(Map.of("error", "id must be numeric"));
            }

            String status = body.get("status") == null ? null : body.get("status").toString();
            String proofBase64 = body.get("proofBase64") == null ? null : body.get("proofBase64").toString();

            // If status is AVAILABLE (unassign), allow even if the user doesn't have a Member record.
            // For all other operations require a member profile.
            Optional<Member> maybeMember = memberRepository.findByUser_Id(userId);
            if (maybeMember.isEmpty()) {
                if (status == null || !"AVAILABLE".equalsIgnoreCase(status.trim())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Member profile required"));
                }
                // else: proceed with unassign operation (AVAILABLE)
            }

            Donation updated = donationUpdateService.updateDonationStatusAndProof(id, status, proofBase64);

            // fetch latest member proof for this donation (if any)
            MemberProofImage mpi = memberProofImageRepository.findTopByDonationIdOrderByUploadedAtDesc(id);

            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "donation", updated,
                    "memberProof", mpi
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "server_error", "detail", ex.getMessage()));
        }
    }
}
