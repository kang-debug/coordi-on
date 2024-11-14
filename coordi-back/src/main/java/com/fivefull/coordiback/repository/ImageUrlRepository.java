package com.fivefull.coordiback.repository;

import com.fivefull.coordiback.entity.ImageUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImageUrlRepository extends JpaRepository<ImageUrl, Long> {
    Optional<ImageUrl> findByCategoryAndItemAndGender(String category, String item, String gender);
}
