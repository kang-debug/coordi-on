package com.fivefull.coordiback.controller;

import com.fivefull.coordiback.dto.MemberDto;
import com.fivefull.coordiback.entity.Member;
import com.fivefull.coordiback.repository.MemberRepository;
import com.fivefull.coordiback.service.KakaoService;
import com.fivefull.coordiback.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = "http://localhost:5173")
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private KakaoService kakaoService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody MemberDto memberDto) {
        Member member = memberService.signUp(memberDto);
        return ResponseEntity.ok(Map.of("success", true, "memberId", member.getMemberId()));
    }

    @PostMapping("/signup/ex")
    public ResponseEntity<?> signUpExternal(@RequestBody MemberDto memberDto) {
        Member member = memberService.signUpExternal(memberDto);
        return ResponseEntity.ok(Map.of("success", true, "memberId", member.getMemberId()));
    }


    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> codeData) {
        String code = codeData.get("code");
        Map<String, String> kakaoUserInfo = kakaoService.getKakaoUserInfo(code);
        String email = kakaoUserInfo.get("email");
        String socialId = kakaoUserInfo.get("socialId");
        String provider = "kakao";

        Optional<Member> member = memberRepository.findBySocialId(socialId);
        if (member.isPresent()) {
            String token = memberService.createToken(member.get());

            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", "token=" + token + "; Path=/; Max-Age=86400");

            return ResponseEntity.ok().headers(headers).body(Map.of("isMember", true));
        } else {
            return ResponseEntity.ok(Map.of("isMember", false, "socialId", socialId, "provider", provider, "email", email));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<Member> member = memberService.login(email, password);
        if (member.isPresent()) {
            String token = memberService.createToken(member.get());

            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", "token=" + token + "; Path=/; Max-Age=86400");

            return ResponseEntity.ok().headers(headers).body(Map.of("success", true, "memberId", member.get().getMemberId()));
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인 실패"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<MemberDto> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MemberDto memberDto = memberService.getUserProfileByEmail(principal.getName());
        return ResponseEntity.ok(memberDto);
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> requestData) {
        String email = requestData.get("email");
        String purpose = requestData.get("purpose");

        if ("signup".equals(purpose)) {
            // 회원가입용 이메일 중복 체크
            if (memberService.emailExists(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미 존재하는 이메일입니다."));
            }
        } else if ("reset-password".equals(purpose)) {
            // 비밀번호 찾기용 이메일 존재 확인
            if (!memberService.emailExists(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "해당 이메일이 존재하지 않습니다."));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "유효하지 않은 요청입니다."));
        }

        memberService.sendVerificationCode(email);
        return ResponseEntity.ok(Map.of("success", true, "message", "인증 코드가 전송되었습니다."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> verificationData) {
        String email = verificationData.get("email");
        String code = verificationData.get("code");

        System.out.println("Received email: " + email);
        System.out.println("Received code: " + code);

        boolean isValid = memberService.verifyCode(email, code);
        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "인증되었습니다."));
        } else {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "인증 코드가 유효하지 않습니다."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        memberService.resetPassword(email, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
