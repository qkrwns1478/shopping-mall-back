package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Member;
import com.example.shoppingmall.repository.MemberRepository;
import com.example.shoppingmall.web.dto.MemberFormDto;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member saveMember(MemberFormDto memberFormDto) {
        validateDuplicateMember(memberFormDto.getUsername());

        Member member = Member.createMember(
                memberFormDto.getUsername(),
                memberFormDto.getEmail(),
                passwordEncoder.encode(memberFormDto.getPassword()),
                memberFormDto.getName(),
                memberFormDto.getAddress(),
                memberFormDto.getBirthday()
        );

        return memberRepository.save(member);
    }

    private void validateDuplicateMember(String username) {
        Optional<Member> findMember = memberRepository.findByUsername(username);
        if (findMember.isPresent()) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Transactional(readOnly = true)
    public boolean checkUsernameAvailability(String username) {
        return memberRepository.findByUsername(username).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디를 찾을 수 없습니다: " + username));

        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}