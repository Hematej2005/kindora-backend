package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.DonationImage;
import com.kindora.kindora_backend.model.User;
import com.kindora.kindora_backend.repository.DonationImageRepository;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Canonical donation query controller.
 * Exposes several GET routes for fetching donation details so frontend fallbacks work.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donations")
public class DonationQueryController {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final DonationImageRepository donationImageRepository;

    /**
     * Primary numeric-only get by id.
     * Example: GET /api/donations/123?includeImages=true
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<?> getDonationByPath(
            @PathVariable Long id,
            @RequestParam(name = "includeImages", required = false, defaultValue = "false") boolean includeImages
    ) {
        return buildDonationResponse(id, includeImages);
    }

    /**
     * Maintain your existing /info/{id} route for any internal tooling.
     * Example: GET /api/donations/info/123?includeImages=true
     */
    @GetMapping("/info/{id}")
    public ResponseEntity<?> getDonationInfo(
            @PathVariable Long id,
            @RequestParam(name = "includeImages", required = false, defaultValue = "false") boolean includeImages
    ) {
        return buildDonationResponse(id, includeImages);
    }

    /**
     * Fallback route expected by some frontends: /api/donations/get?id=123
     */
    @GetMapping("/get")
    public ResponseEntity<?> getDonationByQuery(@RequestParam("id") Long id,
                                                @RequestParam(name = "includeImages", required = false, defaultValue = "false") boolean includeImages) {
        return buildDonationResponse(id, includeImages);
    }

    /** Shared builder for response object (map) */
    private ResponseEntity<?> buildDonationResponse(Long id, boolean includeImages) {
        Optional<Donation> od = donationRepository.findById(id);
        if (od.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Donation not found"));
        }

        Donation donation = od.get();
        Map<String, Object> m = buildDonationForClient(donation);

        if (includeImages) {
            List<DonationImage> imgs = donationImageRepository.findByDonationId(donation.getId());
            List<Map<String, Object>> imgList = new ArrayList<>();
            for (DonationImage img : imgs) {
                Map<String, Object> im = new HashMap<>();
                im.put("id", img.getId());
                im.put("imageUrl", img.getImageUrl());
                im.put("createdAt", img.getCreatedAt());
                imgList.add(im);
            }
            m.put("images", imgList);
        }

        return ResponseEntity.ok(m);
    }

    /** Convert Donation -> Map for frontend */
    private Map<String, Object> buildDonationForClient(Donation d) {
        Map<String, Object> m = new HashMap<>();

        m.put("id", d.getId());
        m.put("donorId", d.getDonorId());
        m.put("type", d.getType());
        m.put("subTypes", d.getSubTypes());
        m.put("description", d.getDescription());
        m.put("totalCount", d.getTotalCount());
        m.put("clothGroups", d.getClothGroups());
        m.put("animalSuitableFor", d.getAnimalSuitableFor());
        m.put("animalFoodType", d.getAnimalFoodType());
        m.put("pinCode", d.getPinCode());
        m.put("street", d.getStreet());
        m.put("district", d.getDistrict());
        m.put("state", d.getState());
        m.put("landmark", d.getLandmark());
        m.put("lat", d.getLat());
        m.put("lng", d.getLng());
        m.put("availableFrom", d.getAvailableFrom());
        m.put("availableTo", d.getAvailableTo());
        m.put("status", d.getStatus());
        m.put("assignedMemberId", d.getAssignedMemberId());
        m.put("assignedAt", d.getAssignedAt());

        // donor lookup (name + fallback to email + phone)
        try {
            Optional<User> uopt = userRepository.findById(d.getDonorId());
            if (uopt.isPresent()) {
                User u = uopt.get();
                String name = u.getFullName() != null && !u.getFullName().isBlank()
                        ? u.getFullName()
                        : (u.getEmail() != null ? u.getEmail() : null);
                m.put("donorName", name);
                m.put("donorPhone", u.getPhoneNumber());
                m.put("donorEmail", u.getEmail());
            } else {
                m.put("donorName", null);
                m.put("donorPhone", null);
                m.put("donorEmail", null);
            }
        } catch (Exception ignored) {
            m.put("donorName", null);
            m.put("donorPhone", null);
            m.put("donorEmail", null);
        }

        // images (first image as thumbnail)
        donationImageRepository.findFirstByDonationIdOrderByIdAsc(d.getId())
                .ifPresent(img -> m.put("imageUrl", img.getImageUrl()));

        return m;
    }
}
