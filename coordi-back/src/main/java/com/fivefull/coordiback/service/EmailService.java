package com.fivefull.coordiback.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    
    private final Map<String, VerificationCodeInfo> verificationCodes = new HashMap<>();
    
    private final long CODE_EXPIRATION_TIME = 3 * 60 * 1000; // 3분 유효 시간

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendVerificationEmail(String email) throws MessagingException {
        String verificationCode = generateVerificationCode();
        verificationCodes.put(email, new VerificationCodeInfo(verificationCode, System.currentTimeMillis()));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom("noreply@cody-on.com", "cody-on"); // 보내는 이메일, 이름
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        helper.setTo(email);
        helper.setSubject("cody-on 인증 코드");
        helper.setText("<p>인증 코드: <b>" + verificationCode + "</b> (3분 이내에 입력해 주세요.)</p>", true);

        mailSender.send(message);
        return verificationCode;
    }

    public boolean verifyCode(String email, String inputCode) {
        VerificationCodeInfo codeInfo = verificationCodes.get(email);
        if (codeInfo == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        // 유효 시간 확인
        if ((currentTime - codeInfo.getTimestamp()) > CODE_EXPIRATION_TIME) {
            verificationCodes.remove(email); // 3분 경과해서 만료된 코드 삭제
            return false;
        }

        return codeInfo.getCode().equals(inputCode);
    }

    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));  // 6자리 코드 생성
    }

    // 코드와 생성 시간을 저장하는 내부 클래스
    private static class VerificationCodeInfo {
        private final String code;
        private final long timestamp;

        public VerificationCodeInfo(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }

        public String getCode() {
            return code;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
