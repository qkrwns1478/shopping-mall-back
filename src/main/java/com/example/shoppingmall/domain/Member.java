package com.example.shoppingmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String name;

    private String address;

    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private int points = 0;

    public void updateMember(String name, String address, LocalDate birthday) {
        this.name = name;
        this.address = address;
        this.birthday = birthday;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public static Member createMember(String email, String password,
                                      String name, String address, LocalDate birthday) {
        Member member = new Member();
        member.setEmail(email);
        member.setPassword(password);
        member.setName(name);
        member.setAddress(address);
        member.setBirthday(birthday);
        member.setRole(MemberRole.USER);
        member.setPoints(0);
        return member;
    }

    public void usePoints(int amount) {
        if (this.points < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.points -= amount;
    }

    public void addPoints(int amount) {
        if (this.points + amount < 0) {
            throw new IllegalStateException("포인트 잔액이 부족하여 차감할 수 없습니다. (현재: " + this.points + " P)");
        }
        this.points += amount;
    }
}