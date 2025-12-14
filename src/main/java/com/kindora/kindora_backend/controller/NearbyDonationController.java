package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.DonationImage;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.repository.DonationImageRepository;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.repository.MemberRepository;
import com.kindora.kindora_backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donations")
public class NearbyDonationController {

    private final DonationRepository donationRepository;
    private final DonationImageRepository donationImageRepository;
    private final MemberRepository memberRepository;

    /**
     * Primary nearby endpoint
     * Example: /api/donations/nearby?lat=13.0&lng=79.0&limit=20
     */
    @GetMapping("/nearby")
    public ResponseEntity<?> nearby(@RequestParam(required = false) Double lat,
                                    @RequestParam(required = false) Double lng,
                                    @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) Double radiusKm) {
        return ResponseEntity.ok(buildNearbyResponse(lat, lng, limit, radiusKm));
    }

    /**
     * Alias for older frontend calls: /api/donations/near
     */
    @GetMapping("/near")
    public ResponseEntity<?> near(@RequestParam(required = false) Double lat,
                                  @RequestParam(required = false) Double lng,
                                  @RequestParam(required = false) Integer limit,
                                  @RequestParam(required = false) Double radiusKm) {
        return ResponseEntity.ok(buildNearbyResponse(lat, lng, limit, radiusKm));
    }

    /**
     * Provide open donations for /api/donations/open
     */
    @GetMapping("/open")
    public ResponseEntity<?> open(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(buildNearbyResponse(null, null, limit, null));
    }

    // --- helper building logic reused by endpoints ---
    private List<Map<String,Object>> buildNearbyResponse(Double lat, Double lng, Integer limit, Double radiusKm) {
        int lim = (limit == null || limit <= 0) ? 50 : limit;
        double radius = (radiusKm == null || radiusKm <= 0) ? 50.0 : radiusKm;

        // If lat/lng not provided, attempt to get member primary location
        if ((lat == null || lng == null)) {
            Long userId = null;
            try { userId = AuthUtil.getLoggedUserId(); } catch (Exception ignored) {}
            if (userId != null) {
                Optional<Member> mopt = memberRepository.findByUser_Id(userId);
                if (mopt.isPresent() && mopt.get().getLocations() != null && !mopt.get().getLocations().isEmpty()) {
                    var primary = mopt.get().getLocations().get(0);
                    if (primary != null && primary.getLat() != null && primary.getLng() != null) {
                        lat = primary.getLat().doubleValue();
                        lng = primary.getLng().doubleValue();
                    }
                }
            }
        }

        List<Donation> found;
        if (lat != null && lng != null) {
            try {
                found = donationRepository.findNearby(lat, lng, radius, lim);
            } catch (Exception ex) {
                found = donationRepository.findAll().stream()
                        .filter(d -> "AVAILABLE".equalsIgnoreCase(d.getStatus()))
                        .collect(Collectors.toList());
            }
        } else {
            found = donationRepository.findAll().stream()
                    .filter(d -> "AVAILABLE".equalsIgnoreCase(d.getStatus()))
                    .sorted(Comparator.comparing(Donation::getCreatedAt).reversed())
                    .limit(lim)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Donation d : found) {
            out.add(donationToMap(d, false));
        }

        // If we have lat/lng, compute distances server-side and sort
        if (lat != null && lng != null) {
            final double qLat = lat;
            final double qLng = lng;
            out.sort(Comparator.comparingDouble(a -> {
                Object la = a.get("lat");
                Object lo = a.get("lng");
                if (la == null || lo == null) return Double.POSITIVE_INFINITY;
                try {
                    double da = Double.parseDouble(String.valueOf(la));
                    double db = Double.parseDouble(String.valueOf(lo));
                    // Haversine:
                    double R = 6371;
                    double dLat = Math.toRadians(da - qLat);
                    double dLon = Math.toRadians(db - qLng);
                    double sinDLat = Math.sin(dLat / 2);
                    double sinDLon = Math.sin(dLon / 2);
                    double aVal = sinDLat * sinDLat + Math.cos(Math.toRadians(qLat)) * Math.cos(Math.toRadians(da)) * sinDLon * sinDLon;
                    double c = 2 * Math.atan2(Math.sqrt(aVal), Math.sqrt(1 - aVal));
                    double distance = R * c;
                    return distance;
                } catch (Exception e) {
                    return Double.POSITIVE_INFINITY;
                }
            }));
        }

        if (out.size() > lim) out = out.subList(0, lim);
        return out;
    }

    private Map<String,Object> donationToMap(Donation d, boolean includeImages) {
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

        // donor lookup (name, phone, email) - best effort
        try {
            Optional<com.kindora.kindora_backend.model.User> uopt = java.util.Optional.ofNullable(null);
            // leave donor fields null by default; front-end may later fetch full donation.
        } catch (Exception ignored) {}

        // images
        List<Map<String,Object>> imgs = new ArrayList<>();
        try {
            List<DonationImage> dbimgs = donationImageRepository.findByDonationId(d.getId());
            for (DonationImage di : dbimgs) {
                Map<String,Object> im = new HashMap<>();
                im.put("id", di.getId());
                im.put("donationId", di.getDonationId());
                im.put("imageUrl", di.getImageUrl());
                im.put("createdAt", di.getCreatedAt());
                imgs.add(im);
            }
        } catch (Exception ignored) {}
        m.put("images", imgs);
        m.put("imageUrl", imgs.isEmpty() ? null : imgs.get(0).get("imageUrl"));

        return m;
    }
}
