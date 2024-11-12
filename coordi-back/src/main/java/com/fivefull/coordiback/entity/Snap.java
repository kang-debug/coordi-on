package com.fivefull.coordiback.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Snap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer snapId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private String snapImageUrl;

    @Column(columnDefinition = "TEXT")
    private String snapDescription;

    @Column(nullable = false)
    private LocalDateTime snapCreatedDate;

    private LocalDateTime snapUpdateDate;
}