package com.fivefull.coordiback.repository;

import com.fivefull.coordiback.entity.Member;
import com.fivefull.coordiback.entity.Snap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SnapRepository extends JpaRepository<Snap, Long> {
    List<Snap> findAllByOrderBySnapCreatedDateDesc();
    List<Snap> findByMember(Member member);
}
