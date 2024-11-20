package com.fivefull.coordiback.repository;

import com.fivefull.coordiback.entity.Like;
import com.fivefull.coordiback.entity.Member;
import com.fivefull.coordiback.entity.Snap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    boolean existsBySnapAndMember(Snap snap, Member member);
    Optional<Like> findBySnapAndMember(Snap snap, Member member);
    int countBySnap(Snap snap);
    List<Like> findByMember(Member member);
    void deleteAllBySnap(Snap snap);
}
