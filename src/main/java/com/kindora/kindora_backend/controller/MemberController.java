package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.dto.LocationDto;
import com.kindora.kindora_backend.dto.MemberDto;
import com.kindora.kindora_backend.dto.TimeDto;
import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.MemberLocation;
import com.kindora.kindora_backend.model.MemberTime;
import com.kindora.kindora_backend.model.MemberType;
import com.kindora.kindora_backend.model.User;
import com.kindora.kindora_backend.repository.UserRepository;
import com.kindora.kindora_backend.service.MemberService;
import com.kindora.kindora_backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;
    private final UserRepository userRepo;
    private final Logger log = LoggerFactory.getLogger(MemberController.class);

    // Save/update profile. Accepts MemberDto JSON.
    @PostMapping("/profile")
    public ResponseEntity<?> saveProfile(@RequestBody MemberDto dto) {
        Long userId;
        try {
            userId = AuthUtil.getLoggedUserId();
        } catch (Exception e) {
            log.warn("AuthUtil threw when retrieving user id", e);
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
        }
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));

        log.info("Received /api/profile save request for userId={} payload={}", userId, dto);

        // basic server-side validation
        if (dto == null || (dto.getFullName() == null || dto.getFullName().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(Map.of("error", "fullName is required"));
        }
        if (dto.getLocations() == null || dto.getLocations().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "At least one location is required"));
        }
        LocationDto firstLoc = dto.getLocations().get(0);
        if (firstLoc.getDistrict() == null || firstLoc.getDistrict().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "District is required for primary location"));
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
        try {
            Member saved = memberService.saveOrUpdate(dto, user);
            log.info("Saved member profile for userId={} memberId={}", userId, saved.getId());
            return ResponseEntity.ok(Map.of("status","OK", "memberId", saved.getId()));
        } catch (Exception ex) {
            log.error("Error saving profile for userId={}", userId, ex);
            return ResponseEntity.status(500).body(Map.of("error", "Could not save profile", "detail", ex.getMessage()));
        }
    }

    // Return profile for logged-in user
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Long userId;
        try {
            userId = AuthUtil.getLoggedUserId();
        } catch (Exception e) {
            log.warn("AuthUtil threw when retrieving user id", e);
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
        }
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));

        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
        Member m = memberService.getByUser(user);
        if (m == null) return ResponseEntity.noContent().build();

        MemberDto out = new MemberDto();
        out.setFullName(m.getFullName());
        out.setAge(m.getAge());
        out.setGender(m.getGender());
        out.setDistance(m.getMaxDistance());

        List<LocationDto> locs = new ArrayList<>();
        if (m.getLocations() != null) {
            for (MemberLocation ml : m.getLocations()) {
                LocationDto ld = new LocationDto();
                ld.setPincode(ml.getPincode());
                ld.setState(ml.getState());
                ld.setStreet(ml.getStreet());
                ld.setDistrict(ml.getDistrict());
                ld.setLandmark(ml.getLandmark());
                ld.setLat(ml.getLat());
                ld.setLng(ml.getLng());
                locs.add(ld);
            }
        }
        out.setLocations(locs);

        List<TimeDto> times = new ArrayList<>();
        if (m.getTimes() != null) {
            for (MemberTime mt : m.getTimes()) {
                TimeDto t = new TimeDto();
                t.setFrom(mt.getFromTime());
                t.setTo(mt.getToTime());
                times.add(t);
            }
        }
        out.setTimes(times);

        List<String> types = new ArrayList<>();
        if (m.getTypes() != null) {
            for (MemberType tt : m.getTypes()) types.add(tt.getType());
        }
        out.setTypes(types);

        return ResponseEntity.ok(out);
    }
}
