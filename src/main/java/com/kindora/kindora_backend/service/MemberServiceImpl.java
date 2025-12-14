package com.kindora.kindora_backend.service;

import com.kindora.kindora_backend.dto.LocationDto;
import com.kindora.kindora_backend.dto.MemberDto;
import com.kindora.kindora_backend.dto.TimeDto;
import com.kindora.kindora_backend.model.*;
import com.kindora.kindora_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepo;
    private final MemberLocationRepository locRepo;
    private final MemberTimeRepository timeRepo;
    private final MemberTypeRepository typeRepo;

    @Transactional
    public Member saveOrUpdate(MemberDto dto, User user) {

        Member member = memberRepo.findByUser(user)
                .orElseGet(() -> {
                    Member m = new Member();
                    m.setUser(user);
                    return m;
                });

        member.setFullName(dto.getFullName());
        member.setAge(dto.getAge());
        member.setGender(dto.getGender());
        member.setMaxDistance(dto.getDistance());

        // persist member first (insert or update)
        member = memberRepo.save(member);

        // use a final reference for lambdas
        final Member finalMember = member;

        // --- Remove old children by member (clean slate) ---
        locRepo.deleteByMember(finalMember);
        timeRepo.deleteByMember(finalMember);
        typeRepo.deleteByMember(finalMember);

        // ensure deletes executed immediately within this transaction before inserts
        locRepo.flush();
        timeRepo.flush();
        typeRepo.flush();

        // --- Locations: validate + prepare ---
        List<MemberLocation> newLocs = new ArrayList<>();
        if (dto.getLocations() != null) {
            int idx = 0;
            for (LocationDto l : dto.getLocations()) {
                if (l == null) continue;
                boolean hasAny = (l.getPincode() != null && !l.getPincode().trim().isEmpty())
                        || (l.getStreet() != null && !l.getStreet().trim().isEmpty())
                        || (l.getState() != null && !l.getState().trim().isEmpty())
                        || (l.getDistrict() != null && !l.getDistrict().trim().isEmpty())
                        || (l.getLat() != null && l.getLng() != null);
                if (!hasAny) continue;

                // enforce district (DB requires it)
                if (l.getDistrict() == null || l.getDistrict().trim().isEmpty()) continue;

                MemberLocation ml = new MemberLocation();
                ml.setMember(finalMember);
                ml.setPincode(trimOrNull(l.getPincode()));
                ml.setState(trimOrNull(l.getState()));
                ml.setStreet(trimOrNull(l.getStreet()));
                ml.setDistrict(trimOrNull(l.getDistrict()));
                ml.setLandmark(trimOrNull(l.getLandmark()));
                ml.setLat(l.getLat());
                ml.setLng(l.getLng());
                ml.setIsPrimary(idx == 0);
                newLocs.add(ml);
                idx++;
            }
            if (!newLocs.isEmpty()) locRepo.saveAll(newLocs);
        }

        // --- Times ---
        List<MemberTime> newTimes = new ArrayList<>();
        if (dto.getTimes() != null) {
            for (TimeDto t : dto.getTimes()) {
                if (t == null) continue;
                String from = t.getFrom();
                String to = t.getTo();
                if (from == null || from.isBlank() || to == null || to.isBlank()) continue;
                MemberTime mt = new MemberTime();
                mt.setMember(finalMember);
                mt.setFromTime(from);
                mt.setToTime(to);
                newTimes.add(mt);
            }
            if (!newTimes.isEmpty()) timeRepo.saveAll(newTimes);
        }

        // --- Types: dedupe and insert ---
        if (dto.getTypes() != null) {
            // preserve order while deduping
            Set<String> dedup = dto.getTypes().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            List<MemberType> newTypes = dedup.stream()
                    .map(t -> {
                        MemberType mt = new MemberType();
                        mt.setMember(finalMember);
                        mt.setType(t);
                        return mt;
                    })
                    .collect(Collectors.toList());

            try {
                if (!newTypes.isEmpty()) typeRepo.saveAll(newTypes);
            } catch (DataIntegrityViolationException dive) {
                throw new IllegalStateException("Could not save profile — duplicate member_type detected", dive);
            }
        }

        // reload to get up-to-date relations
        return memberRepo.findById(finalMember.getId()).orElse(finalMember);
    }

    public Member getByUser(User user) {
        return memberRepo.findByUser(user).orElse(null);
    }

    private String trimOrNull(String s){
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
