package com.fivefull.coordiback.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Categori {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoriId;

    @Column(length = 50, nullable = false)
    private String categoryName;
}
