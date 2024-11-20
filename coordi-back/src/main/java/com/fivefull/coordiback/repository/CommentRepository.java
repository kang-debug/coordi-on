package com.fivefull.coordiback.repository;

import com.fivefull.coordiback.entity.Comment;
import com.fivefull.coordiback.entity.Snap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findBySnap_SnapIdOrderByCreateDateAsc(Long snapId);
    void deleteAllBySnap(Snap snap);
}
