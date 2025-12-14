package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.User;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.repository.MemberRepository;
import com.kindora.kindora_backend.repository.UserRepository;
import com.kindora.kindora_backend.util.AuthUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberAcceptController {

    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @PostMapping("/accept")
    public ResponseEntity<?> acceptDonation(@RequestBody Map<String, Object> body) {

        Long userId = AuthUtil.getCurrentUserId();
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));

        Object oId = body.get("donationId");
        if (oId == null)
            return ResponseEntity.badRequest().body(Map.of("error", "donationId required"));

        Long donationId = Long.valueOf(oId.toString());

        Donation donation = donationRepository.findById(donationId)
                .orElse(null);
        if (donation == null)
            return ResponseEntity.status(404).body(Map.of("error", "Donation not found"));

        if (!"AVAILABLE".equalsIgnoreCase(donation.getStatus()))
            return ResponseEntity.status(409).body(Map.of("error", "Donation not available"));

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));

        Member member = memberRepository.findByUser(user)
                .orElse(memberRepository.findByUser_Id(userId).orElse(null));

        if (member == null)
            return ResponseEntity.status(400).body(Map.of("error", "Member profile not found"));

        donation.setAssignedMemberId(member.getId());
        donation.setAssignedAt(Instant.now());
        donation.setStatus("ASSIGNED");

        Donation saved = donationRepository.save(donation);

        return ResponseEntity.ok(Map.of("updated", saved));
    }
}
