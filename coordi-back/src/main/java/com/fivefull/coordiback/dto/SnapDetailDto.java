package com.fivefull.coordiback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SnapDetailDto {
    private Integer snapId;
    private String snapDescription;
    private String snapImageUrl;
    private LocalDateTime snapCreatedDate;
    private String memberNickname;
    private List<CommentDto> comments;
    private boolean liked;
}
