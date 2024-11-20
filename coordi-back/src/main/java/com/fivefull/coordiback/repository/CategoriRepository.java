package com.fivefull.coordiback.repository;

import com.fivefull.coordiback.entity.Categori;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriRepository extends JpaRepository<Categori, Integer> {
    Optional<Categori> findByCategoryName(String categoryName);
    boolean existsByCategoryName(String categoryName);
}
