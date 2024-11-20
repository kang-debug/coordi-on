package com.fivefull.coordiback.service;

import com.fivefull.coordiback.dto.MemberDto;
import com.fivefull.coordiback.entity.Member;
import com.fivefull.coordiback.repository.MemberRepository;
import com.fivefull.coordiback.util.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GCSService gcsService;

    // 비밀번호 해시로
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 비밀번호 조건 ( 8자리 이상, 특수문자 포함 )
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$");
    // 닉네임 조건 ( 2~16자, 영어,숫자,_ 만 포함 가능 )
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,15}$");


    public void sendVerificationCode(String email) {
        try {
            emailService.sendVerificationEmail(email);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email.", e);
        }
    }

    public boolean verifyCode(String email, String code) {
        return emailService.verifyCode(email, code);
    }

    public boolean emailExists(String email) {
        return memberRepository.existsByEmail(email);
    }



    public Member signUp(MemberDto memberDto) {
        if (!PASSWORD_PATTERN.matcher(memberDto.getPassword()).matches()) {
            throw new IllegalArgumentException("비밀번호는 최소 8자리 이상이며 특수문자를 포함해야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(memberDto.getPassword());
        validateNickname(memberDto.getNickname());

        Member member = new Member();
        member.setEmail(memberDto.getEmail());
        member.setPassword(encodedPassword);
        member.setNickname(memberDto.getNickname());
        member.setGender(memberDto.getGender());
        member.setAge(memberDto.getAge());
        member.setProvider("local");
        member.setLatitude(memberDto.getLatitude());
        member.setLongitude(memberDto.getLongitude());

        String profileImageUrl = memberDto.getProfileImageUrl();
        if (profileImageUrl != null && profileImageUrl.startsWith("data:image")) {
            profileImageUrl = gcsService.uploadBase64Image(profileImageUrl);
        } else if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            profileImageUrl = "https://storage.googleapis.com/cody_bucket_full/defalut%20image.png"; // 기본 이미지 URL
        }

        member.setProfileImageUrl(profileImageUrl);

        return memberRepository.save(member);
    }


    public Member signUpExternal(MemberDto memberDto) {

        validateNickname(memberDto.getNickname());

        Member member = new Member();
        member.setEmail(memberDto.getEmail());
        member.setNickname(memberDto.getNickname());
        member.setGender(memberDto.getGender());
        member.setAge(memberDto.getAge());
        member.setSocialId(memberDto.getSocialId());
        member.setProvider(memberDto.getProvider());
        member.setLatitude(memberDto.getLatitude());
        member.setLongitude(memberDto.getLongitude());

        String profileImageUrl = memberDto.getProfileImageUrl();
        if (profileImageUrl != null && profileImageUrl.startsWith("data:image")) {
            profileImageUrl = gcsService.uploadBase64Image(profileImageUrl);
        } else if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            profileImageUrl = "https://storage.googleapis.com/cody_bucket_full/defalut%20image.png"; // 기본 이미지 URL
        }

        member.setProfileImageUrl(profileImageUrl);

        return memberRepository.save(member);
    }

    private void validateNickname(String nickname) {
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new IllegalArgumentException("닉네임은 2자 이상 16자 이하이며, 영어 소문자 시작, 숫자와 _(언더바)만 허용됩니다.");
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
    }

    public Optional<Member> login(String email, String password) {
        Optional<Member> member = memberRepository.findByEmail(email);

        if (member.isPresent() && passwordMatches(password, member.get().getPassword())) {
            return member;
        }
        return Optional.empty();
    }

    public String createToken(Member member) {
        return jwtUtil.generateToken(member.getEmail());
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
    }

    public MemberDto getUserProfileByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return MemberDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .gender(member.getGender())
                .age(member.getAge())
                .latitude(member.getLatitude())
                .longitude(member.getLongitude())
                .profileImageUrl(member.getProfileImageUrl())
                .createdDate(member.getCreatedDate())
                .build();
    }

    public void resetPassword(String email, String newPassword) {
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalArgumentException("비밀번호는 최소 8자리 이상이며 특수문자를 포함해야 합니다.");
        }

        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        Member member = optionalMember.get();
        member.setPassword(encodedPassword);
        memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(String email, MultipartFile profileImage, String nickname, Integer age, String gender) throws IOException {
        // 현재 로그인된 사용자를 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (nickname != null && !nickname.isEmpty() && !nickname.equals(member.getNickname())) {
            if (memberRepository.existsByNickname(nickname)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            validateNickname(nickname);
            member.setNickname(nickname);
        }
        if (age != null) {
            member.setAge(age);
        }

        if (gender != null && (gender.equals("남성") || gender.equals("여성"))) {
            member.setGender(gender);
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            String uploadedUrl = gcsService.uploadFile(profileImage);
            member.setProfileImageUrl(uploadedUrl);
        }

        return memberRepository.save(member);
    }

}
