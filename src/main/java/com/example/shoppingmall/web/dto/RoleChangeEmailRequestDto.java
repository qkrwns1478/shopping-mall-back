package com.example.shoppingmall.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RoleChangeEmailRequestDto {
    private String email;
    private String name;
    private String currentRole;
    private String newRole;
}