package com.fivefull.coordiback.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SnapDto {
    private Long snapId;
    private List<String> snapImageUrls;
    private String snapDescription;
    private LocalDateTime snapCreatedDate;
    private List<String> categories;
    private AuthorDto author;
}
