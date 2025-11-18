package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Member;
import com.example.shoppingmall.repository.MemberRepository;
import com.example.shoppingmall.web.dto.MemberFormDto;
import com.example.shoppingmall.web.dto.MemberUpdateDto;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public Member saveMember(MemberFormDto memberFormDto) {
        if (!memberFormDto.getPassword().equals(memberFormDto.getPasswordConfirm())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        validateDuplicateMember(memberFormDto.getEmail());

        Member member = Member.createMember(
                memberFormDto.getEmail(),
                passwordEncoder.encode(memberFormDto.getPassword()),
                memberFormDto.getName(),
                memberFormDto.getAddress(),
                memberFormDto.getBirthday()
        );

        return memberRepository.save(member);
    }

    private void validateDuplicateMember(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    public void sendTemporaryPassword(String email, String name) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("가입되지 않은 이메일입니다."));
        if (!member.getName().equals(name)) {
            throw new IllegalStateException("이름이 일치하지 않습니다.");
        }
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        member.setPassword(passwordEncoder.encode(tempPassword));
        emailService.sendTemporaryPassword(email, tempPassword);
    }

    @Transactional(readOnly = true)
    public boolean checkEmailAvailability(String email) {
        return memberRepository.findByEmail(email).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("이메일을 찾을 수 없습니다: " + email));

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public Member findMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));
    }

    @Transactional(readOnly = true)
    public boolean checkPassword(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));
        return passwordEncoder.matches(password, member.getPassword());
    }

    public void updateMember(String email, MemberUpdateDto updateDto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));

        /* if (!passwordEncoder.matches(updateDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
        } */

        member.updateMember(updateDto.getName(), updateDto.getAddress(), updateDto.getBirthday());

        if (updateDto.getNewPassword() != null && !updateDto.getNewPassword().isBlank()) {
            if (!updateDto.getNewPassword().equals(updateDto.getNewPasswordConfirm())) {
                throw new IllegalStateException("새 비밀번호가 일치하지 않습니다.");
            }
            member.updatePassword(passwordEncoder.encode(updateDto.getNewPassword()));
        }
    }

    public void deleteMember(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
    }
}