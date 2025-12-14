package com.kindora.kindora_backend.repository;

import com.kindora.kindora_backend.model.Member;
import com.kindora.kindora_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUser(User user);
    Optional<Member> findByUser_Id(Long userId);
}
