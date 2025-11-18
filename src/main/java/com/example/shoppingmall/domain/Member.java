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

    /* @Column(unique = true, nullable = false, length = 50)
    private String username; */

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
        // member.setUsername(username);
        member.setEmail(email);
        member.setPassword(password);
        member.setName(name);
        member.setAddress(address);
        member.setBirthday(birthday);
        member.setRole(MemberRole.USER);
        return member;
    }
}