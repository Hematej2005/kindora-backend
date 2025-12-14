package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.MemberType;
import com.kindora.kindora_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberTypeRepository extends JpaRepository<MemberType, Long> {
    void deleteByMember(Member member);
    List<MemberType> findByMember(Member member);
}
