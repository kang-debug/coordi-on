package com.fivefull.coordiback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Integer commentId;
    private String content; // 댓글 내용
    private String memberNickname; // 댓글 작성자 닉네임
    private LocalDateTime createDate; // 작성 날짜
}