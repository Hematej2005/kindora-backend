package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.dto.DonationHistoryDto;
import com.kindora.kindora_backend.dto.VolunteerDto;
import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.DonationImage;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.MemberProofImage;
import com.kindora.kindora_backend.model.User;
import com.kindora.kindora_backend.repository.DonationImageRepository;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.repository.MemberProofImageRepository;
import com.kindora.kindora_backend.repository.MemberRepository;
import com.kindora.kindora_backend.repository.UserRepository;
import com.kindora.kindora_backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/donor")
@RequiredArgsConstructor
public class DonorController {

    private final DonationRepository donationRepository;
    // keep DonationImageRepository if you still need donor-upload images elsewhere (no change),
    // but for "proof" we will use MemberProofImageRepository
    private final DonationImageRepository donationImageRepository;
    private final MemberProofImageRepository memberProofImageRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<?> donorHistory() {
        Long donorId;
        try {
            donorId = AuthUtil.getLoggedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
        }
        if (donorId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));

        List<Donation> list = donationRepository.findByDonorIdOrderByCreatedAtDesc(donorId);

        List<DonationHistoryDto> out = list.stream().map(d -> {
            DonationHistoryDto dto = new DonationHistoryDto();
            dto.setId(d.getId());
            // type/date/status - try common getters
            dto.setType(safeStr(d, "getType", "getTypeValue", "getDonationType"));
            Object createdAtObj = safeGet(d, "getCreatedAt", "getCreated_at", "getCreated");
            dto.setCreatedAt(createdAtObj == null ? null : createdAtObj.toString());
            dto.setStatus(safeStr(d, "getStatus", "getStatusValue", "getDonationStatus"));

            // volunteer - try multiple strategies
            VolunteerDto volDto = null;

            // 1) If Donation has an assignedMemberId (Long) -> load Member -> User contact
            Long assignedMemberId = safeLong(d,
                    "getAssignedMemberId", "getAssigned_member_id", "getAssignedMember", "getMemberId", "getAssigned_id");
            if (assignedMemberId != null) {
                Optional<Member> mm = memberRepository.findById(assignedMemberId);
                if (mm.isPresent()) {
                    volDto = new VolunteerDto();
                    volDto.setName(mm.get().getFullName());
                    try {
                        User u = mm.get().getUser();
                        if (u != null) {
                            volDto.setEmail(safeStr(u, "getEmail", "getEmailAddress"));
                            volDto.setPhone(safeStr(u, "getPhoneNumber", "getPhone"));
                        }
                    } catch (Exception ignored) {}
                }
            } else {
                // 2) maybe donation stores volunteer name/email directly
                String volunteerName = safeStr(d, "getVolunteerName", "getVolunteer_name", "getVolunteer");
                if (volunteerName != null) {
                    volDto = new VolunteerDto();
                    volDto.setName(volunteerName);
                    volDto.setEmail(safeStr(d, "getVolunteerEmail", "getVolunteer_email"));
                    volDto.setPhone(safeStr(d, "getVolunteerPhone", "getVolunteer_phone"));
                }
            }

            dto.setVolunteer(volDto);

            // PROOF: fetch latest member-uploaded proof image (not donor-uploaded)
            try {
                MemberProofImage proof = memberProofImageRepository.findTopByDonationIdOrderByUploadedAtDesc(d.getId());
                dto.setProofUrl(proof != null ? proof.getImageUrl() : null);
            } catch (Exception e) {
                // fallback: null
                dto.setProofUrl(null);
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }

    // ---------- Helper reflection-based getters (fail-safe: return null if not present) ----------

    private static Object safeGet(Object obj, String... methodNames) {
        if (obj == null) return null;
        Class<?> cls = obj.getClass();
        for (String m : methodNames) {
            try {
                Method method = cls.getMethod(m);
                return method.invoke(obj);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String safeStr(Object obj, String... methodNames) {
        Object v = safeGet(obj, methodNames);
        return v == null ? null : v.toString();
    }

    private static Long safeLong(Object obj, String... methodNames) {
        Object v = safeGet(obj, methodNames);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}
