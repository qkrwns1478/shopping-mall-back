package com.example.shoppingmall.config;

import com.example.shoppingmall.domain.Member;
import com.example.shoppingmall.domain.MemberRole;
import com.example.shoppingmall.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${adminEmail}")
    private String adminEmail;

    @Value("${adminPassword}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (memberRepository.findByEmail(adminEmail).isEmpty()) {
            Member admin = new Member();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setName("문식사");
            admin.setAddress("문식광역시 문식구 문식동 22");
            admin.setBirthday(LocalDate.now());
            admin.setRole(MemberRole.ADMIN);
            admin.setPoints(100000);

            memberRepository.save(admin);
            System.out.println("관리자 계정 생성 완료: " + adminEmail);
        }
    }
}