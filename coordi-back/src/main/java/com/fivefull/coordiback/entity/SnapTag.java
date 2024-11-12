package com.fivefull.coordiback.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class SnapTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tagId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Snap snap;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Categori categori;
}