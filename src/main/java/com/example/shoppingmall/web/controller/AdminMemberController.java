package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.MemberService;
import com.example.shoppingmall.web.dto.MemberManageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "관리자(Admin)", description = "관리자 전용 회원 관리 API")
@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

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
}