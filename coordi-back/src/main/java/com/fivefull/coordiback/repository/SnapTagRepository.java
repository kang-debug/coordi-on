package com.fivefull.coordiback.repository;

import com.fivefull.coordiback.entity.Snap;
import com.fivefull.coordiback.entity.SnapTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SnapTagRepository extends JpaRepository<SnapTag, Long> {
    List<SnapTag> findByCategori_CategoryName(String categoryName);
    List<SnapTag> findBySnap(Snap snap);
    void deleteAllBySnap(Snap snap);
}
