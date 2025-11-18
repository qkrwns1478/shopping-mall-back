package com.example.shoppingmall.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[MUNSIKSA] 회원가입 인증번호");
        message.setText("인증번호는 [" + code + "] 입니다.");
        mailSender.send(message);
    }

    public void sendTemporaryPassword(String to, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[MUNSIKSA] 임시 비밀번호 발급 안내");
        message.setText("회원님의 임시 비밀번호는 [" + tempPassword + "] 입니다.\n로그인 후 반드시 비밀번호를 변경해주세요.");
        mailSender.send(message);
    }
}