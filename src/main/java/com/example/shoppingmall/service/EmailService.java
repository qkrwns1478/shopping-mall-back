package com.example.shoppingmall.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.properties.mail.from.name}")
    private String fromName;

    public void sendVerificationCode(String to, String code) {
        sendEmail(to, "[MUNSIKSA] 회원가입 인증번호", "인증번호는 [" + code + "] 입니다.");
    }

    public void sendRoleVerificationCode(String to, String code, String targetEmail, String targetName, String currentRole, String newRole) {
        StringBuilder sb = new StringBuilder();
        sb.append("아래 계정에 대한 권한 변경 요청이 발생했습니다.\n");
        sb.append("계정: ").append(targetEmail).append(" (").append(targetName).append(") ");
        sb.append(currentRole).append(" -> ").append(newRole).append("\n");
        sb.append("인증번호: ").append(code);

        sendEmail(to, "[MUNSIKSA] 권한 변경 요청 알림", sb.toString());
    }

    public void sendTemporaryPassword(String to, String tempPassword) {
        sendEmail(to, "[MUNSIKSA] 임시 비밀번호 발급 안내", "회원님의 임시 비밀번호는 [" + tempPassword + "] 입니다.\n로그인 후 반드시 비밀번호를 변경해주세요.");
    }

    private void sendEmail(String to, String subject, String text) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}