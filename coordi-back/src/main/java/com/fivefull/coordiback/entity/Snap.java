package com.fivefull.coordiback.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Snap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snapId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    @ElementCollection
    @CollectionTable(name = "snap_images", joinColumns = @JoinColumn(name = "snap_id"))
    @Column(name = "image_url")
    private List<String> snapImageUrls;

    @Column(columnDefinition = "TEXT")
    private String snapDescription;

    @Column(nullable = false)
    private LocalDateTime snapCreatedDate;

    private LocalDateTime snapUpdateDate;
}
