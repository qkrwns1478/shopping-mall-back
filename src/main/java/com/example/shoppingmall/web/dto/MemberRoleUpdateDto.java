package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.domain.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberRoleUpdateDto {
    @NotNull(message = "변경할 권한은 필수입니다.")
    private MemberRole role;

    @NotBlank(message = "인증 코드는 필수입니다.")
    private String code;
}