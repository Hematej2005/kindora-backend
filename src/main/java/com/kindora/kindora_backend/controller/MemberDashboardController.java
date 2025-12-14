package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.dto.DonationHistoryDto;
import com.kindora.kindora_backend.dto.VolunteerDto;
import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.MemberProofImage;
import com.kindora.kindora_backend.model.User;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.repository.MemberProofImageRepository;
import com.kindora.kindora_backend.repository.MemberRepository;
import com.kindora.kindora_backend.repository.UserRepository;
import com.kindora.kindora_backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberDashboardController {

    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MemberProofImageRepository memberProofImageRepository;

    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignments() {
        Long userId = AuthUtil.getLoggedUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error","Unauthenticated"));

        Optional<Member> maybeMember = memberRepository.findByUser_Id(userId);
        if (maybeMember.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Member profile not found"));

        Member member = maybeMember.get();
        List<Donation> assigned = donationRepository.findByAssignedMemberIdOrderByAssignedAtDesc(member.getId());
        List<DonationHistoryDto> out = assigned.stream().map(this::mapToHistoryDto).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        Long userId = AuthUtil.getLoggedUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error","Unauthenticated"));

        Optional<Member> maybeMember = memberRepository.findByUser_Id(userId);
        if (maybeMember.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Member profile not found"));

        Member member = maybeMember.get();
        List<Donation> delivered = donationRepository.findByAssignedMemberIdAndStatusOrderByAssignedAtDesc(member.getId(), "DELIVERED");
        List<DonationHistoryDto> out = delivered.stream().map(this::mapToHistoryDto).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    private DonationHistoryDto mapToHistoryDto(Donation d) {
        DonationHistoryDto dto = new DonationHistoryDto();
        dto.setId(d.getId());
        dto.setType(d.getType());
        dto.setCreatedAt(d.getCreatedAt() == null ? null : d.getCreatedAt().toString());
        dto.setStatus(d.getStatus());

        // volunteer details: member->user
        if (d.getAssignedMemberId() != null) {
            memberRepository.findById(d.getAssignedMemberId()).ifPresent(mm -> {
                VolunteerDto v = new VolunteerDto();
                v.setName(mm.getFullName());
                try {
                    User u = mm.getUser();
                    if (u != null) {
                        v.setEmail(u.getEmail());
                        v.setPhone(u.getPhoneNumber());
                    }
                } catch (Exception ignored) {}
                dto.setVolunteer(v);
            });
        } else {
            dto.setVolunteer(null);
        }

        // proof: latest member proof image
        try {
            MemberProofImage mp = memberProofImageRepository.findTopByDonationIdOrderByUploadedAtDesc(d.getId());
            dto.setProofUrl(mp != null ? mp.getImageUrl() : null);
        } catch (Exception e) {
            dto.setProofUrl(null);
        }

        return dto;
    }

    /**
     * Reject an assignment: unassign donation and move it back to AVAILABLE
     * Request body: { "donationId": 123 }
     *
     * Notes:
     *  - We try to be helpful: use assignedAt, otherwise try updatedAt / createdAt as fallback.
     *  - If no timestamps exist we allow unassigning (this was the main reason rejects silently failed).
     */
    @PostMapping("/reject")
    public ResponseEntity<?> rejectAssignment(@RequestBody Map<String, Object> payload) {
        Long userId = AuthUtil.getLoggedUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error","Unauthenticated"));

        Object donationIdObj = payload == null ? null : payload.get("donationId");
        if (donationIdObj == null) return ResponseEntity.badRequest().body(Map.of("error","donationId required"));

        Long donationId;
        try {
            donationId = Long.valueOf(String.valueOf(donationIdObj));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error","invalid donationId"));
        }

        Optional<Member> maybeMember = memberRepository.findByUser_Id(userId);
        if (maybeMember.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Member profile not found"));
        Member member = maybeMember.get();

        Optional<Donation> opt = donationRepository.findById(donationId);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Donation not found"));

        Donation donation = opt.get();

        // ensure donation is assigned to this member
        if (donation.getAssignedMemberId() == null || !donation.getAssignedMemberId().equals(member.getId())) {
            return ResponseEntity.status(403).body(Map.of("error","Donation not assigned to this member"));
        }

        // check time window: prefer assignedAt, fallback to updatedAt or createdAt
        Instant assignedAt = null;
        try {
            if (donation.getAssignedAt() != null) {
                assignedAt = donation.getAssignedAt();
            } else if (donation.getUpdatedAt() != null) {
                assignedAt = donation.getUpdatedAt();
            } else if (donation.getCreatedAt() != null) {
                assignedAt = donation.getCreatedAt();
            }
        } catch (Exception ignored) { assignedAt = null; }

        if (assignedAt != null) {
            long diff = Math.abs(Instant.now().getEpochSecond() - assignedAt.getEpochSecond());
            if (diff > 3600) {
                // If the record is older than 1 hour, disallow reject to be safe
                return ResponseEntity.status(400).body(Map.of("error","Reject window expired"));
            }
        } else {
            // No timestamps available — accept the reject (practical fix for inconsistent data)
            // (This was the main cause of your "reject didn't persist" issue.)
        }

        // Unassign and set status AVAILABLE
        try {
            donation.setAssignedMemberId(null);

            // Clear assignedAt via setter if present
            try { donation.setAssignedAt(null); } catch (Exception ignored) {}

            donation.setStatus("AVAILABLE");

            // defensively clear any assigned-member cached fields if present:
            String[] candidateSetters = new String[] {
                    "setAssignedMemberName", "setAssignedMemberFullName", "setAssignedMemberEmail",
                    "setAssignedMemberPhone", "setAssignedMemberPhoneNumber", "setAssignedMemberContact"
            };
            for (String setter : candidateSetters) {
                try {
                    Method m = donation.getClass().getMethod(setter, String.class);
                    m.invoke(donation, (Object) null);
                } catch (NoSuchMethodException ignored) {
                    // try clearing similarly named field
                    try {
                        String fieldName = setter.replaceFirst("^set", "");
                        fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                        java.lang.reflect.Field f = null;
                        try {
                            f = donation.getClass().getDeclaredField(fieldName);
                        } catch (NoSuchFieldException nsf) {
                            // try alternative name
                            String alt = fieldName.replaceFirst("^assigned", "assigned");
                            try { f = donation.getClass().getDeclaredField(alt); } catch (NoSuchFieldException ignore2) {}
                        }
                        if (f != null) {
                            f.setAccessible(true);
                            f.set(donation, null);
                        }
                    } catch (Exception ignored2) {}
                } catch (Exception ignored) {}
            }

            donationRepository.save(donation);
            return ResponseEntity.ok(Map.of("status","OK", "id", donation.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error","Could not unassign donation","detail", e.getMessage()));
        }
    }
}
