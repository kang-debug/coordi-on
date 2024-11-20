package com.fivefull.coordiback;

import com.fivefull.coordiback.entity.Categori;
import com.fivefull.coordiback.repository.CategoriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class CategoriData {

    @Autowired
    private CategoriRepository categoriRepository;

    @PostConstruct
    public void initializeCategories() {
        List<String> predefinedCategories = List.of(
                "캐주얼", "스트릿", "빈티지", "모던", "댄디", "스포티", "꾸안꾸", "꾸꾸꾸"
        );

        for (String categoryName : predefinedCategories) {
            if (!categoriRepository.existsByCategoryName(categoryName)) {
                Categori categori = new Categori();
                categori.setCategoryName(categoryName);
                categoriRepository.save(categori);
            }
        }
    }
}