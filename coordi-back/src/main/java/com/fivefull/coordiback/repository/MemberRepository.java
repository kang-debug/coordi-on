package com.fivefull.coordiback.repository;

import com.fivefull.coordiback.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findBySocialId(String socialId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
}
