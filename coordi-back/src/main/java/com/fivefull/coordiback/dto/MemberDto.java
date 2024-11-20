package com.fivefull.coordiback.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private Long memberId; // 멤버 ID
    private String email; // 이메일
    private String password; // 비번
    private String nickname; // 닉네임
    private String gender; // 성별
    private Integer age; // 나이
    private String provider; // 카카오,네이버, 구글 등
    private String socialId; // 소셜아이디
    private Double latitude;  // 위도
    private Double longitude; // 경도
    private String profileImageUrl; // 프로필 이미지
    private LocalDateTime createdDate;
}