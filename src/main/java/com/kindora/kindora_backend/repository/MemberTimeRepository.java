package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.MemberTime;
import com.kindora.kindora_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberTimeRepository extends JpaRepository<MemberTime, Long> {
    void deleteByMember(Member member);
    List<MemberTime> findByMember(Member member);
}
