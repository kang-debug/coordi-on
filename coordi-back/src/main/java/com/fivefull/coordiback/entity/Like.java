package com.fivefull.coordiback.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Snap snap;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;
}
