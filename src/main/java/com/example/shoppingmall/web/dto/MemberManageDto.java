package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.domain.Member;
import com.example.shoppingmall.domain.MemberRole;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberManageDto {
    private Long id;
    private String email;
    private String name;
    private String address;
    private MemberRole role;
    private int points;

    public MemberManageDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.address = member.getAddress();
        this.role = member.getRole();
        this.points = member.getPoints();
    }
}