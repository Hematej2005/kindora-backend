package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.MemberLocation;
import com.kindora.kindora_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberLocationRepository extends JpaRepository<MemberLocation, Long> {
    // derived delete to remove all by member
    void deleteByMember(Member member);

    List<MemberLocation> findByMember(Member member);
}
