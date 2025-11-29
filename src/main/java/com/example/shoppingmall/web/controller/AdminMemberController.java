package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.domain.MemberRole;
import com.example.shoppingmall.service.EmailService;
import com.example.shoppingmall.service.MemberService;
import com.example.shoppingmall.web.dto.MemberManageDto;
import com.example.shoppingmall.web.dto.MemberRoleUpdateDto;
import com.example.shoppingmall.web.dto.RoleChangeEmailRequestDto;
import com.example.shoppingmall.web.dto.MemberPointUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Random;

@Tag(name = "관리자(Admin)", description = "관리자 전용 회원 관리 API")
@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    @Operation(summary = "회원 목록 조회", description = "관리자 권한으로 모든 회원 목록을 페이징하여 조회합니다.")
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> memberList(@RequestParam(value = "page", defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<MemberManageDto> members = memberService.getAdminMemberPage(pageable);

        return ResponseEntity.ok(Map.of(
                "content", members.getContent(),
                "totalPages", members.getTotalPages(),
                "totalElements", members.getTotalElements(),
                "number", members.getNumber()
        ));
    }

    @Operation(summary = "회원 강제 탈퇴", description = "관리자 권한으로 특정 회원을 강제 탈퇴시킵니다.")
    @DeleteMapping("/{memberId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMember(@PathVariable("memberId") Long memberId) {
        try {
            memberService.deleteMember(memberId);
            return ResponseEntity.ok(Map.of("success", true, "message", "회원이 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "권한 변경 인증 메일 발송", description = "관리자 이메일로 권한 변경을 위한 인증 코드를 발송합니다.")
    @PostMapping("/role/send-verification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendRoleVerificationEmail(
            @RequestBody RoleChangeEmailRequestDto requestDto,
            Principal principal,
            HttpSession session
    ) {
        String adminEmail = principal.getName();
        String code = String.valueOf(100000 + new Random().nextInt(900000));

        try {
            emailService.sendRoleVerificationCode(
                    adminEmail,
                    code,
                    requestDto.getEmail(),
                    requestDto.getName(),
                    requestDto.getCurrentRole(),
                    requestDto.getNewRole()
            );
            session.setAttribute("roleChangeCode", code);
            return ResponseEntity.ok(Map.of("success", true, "message", "관리자 이메일로 인증 코드가 발송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "메일 발송 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "회원 권한 변경", description = "인증 코드 확인 후 회원의 권한을 변경합니다.")
    @PostMapping("/{memberId}/role")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMemberRole(
            @PathVariable("memberId") Long memberId,
            @RequestBody @Valid MemberRoleUpdateDto dto,
            HttpSession session
    ) {
        String sessionCode = (String) session.getAttribute("roleChangeCode");

        if (sessionCode == null || !sessionCode.equals(dto.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "인증 코드가 일치하지 않거나 만료되었습니다."));
        }

        try {
            memberService.updateMemberRole(memberId, dto.getRole());
            session.removeAttribute("roleChangeCode");
            return ResponseEntity.ok(Map.of("success", true, "message", "회원 권한이 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "회원 포인트 수정", description = "특정 회원의 포인트를 지급하거나 회수합니다.")
    @PostMapping("/{memberId}/points")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMemberPoints(
            @PathVariable("memberId") Long memberId,
            @RequestBody MemberPointUpdateDto dto
    ) {
        try {
            memberService.updateMemberPoints(memberId, dto.getPoint());
            return ResponseEntity.ok(Map.of("success", true, "message", "포인트가 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}