package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.domain.Member;
import com.example.shoppingmall.service.EmailService;
import com.example.shoppingmall.service.MemberService;
import com.example.shoppingmall.web.dto.MemberFormDto;
import com.example.shoppingmall.web.dto.MemberUpdateDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final EmailService emailService;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "members/signupForm";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processSignUp(
            @RequestBody @Valid MemberFormDto memberFormDto,
            BindingResult bindingResult,
            HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "입력 값을 확인해주세요.", "errors", bindingResult.getAllErrors()));
        }

        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        if (!memberFormDto.getEmail().equals(verifiedEmail)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이메일 인증이 완료되지 않았습니다."));
        }

        try {
            memberService.saveMember(memberFormDto);
            session.removeAttribute("verifiedEmail");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/signup-success")
    public String showSignUpSuccess() {
        return "members/signupSuccess";
    }

    @PostMapping("/send-verification-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(@RequestParam String email, HttpSession session) {
        if (!memberService.checkEmailAvailability(email)) {
            return ResponseEntity.ok(Map.of("success", false, "message", "이미 사용 중인 이메일입니다."));
        }

        String code = createVerificationCode();
        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "인증번호 발송에 실패했습니다."));
        }

        session.setAttribute("verificationCode", code);
        session.setAttribute("verificationEmailRequest", email);
        session.setMaxInactiveInterval(300);

        return ResponseEntity.ok(Map.of("success", true, "message", "인증번호가 발송되었습니다."));
    }

    @PostMapping("/verify-code")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyCode(@RequestParam String email, @RequestParam String code, HttpSession session) {
        String sessionCode = (String) session.getAttribute("verificationCode");
        String sessionEmail = (String) session.getAttribute("verificationEmailRequest");

        if (sessionCode == null || sessionEmail == null || !sessionEmail.equals(email) || !sessionCode.equals(code)) {
            return ResponseEntity.ok(Map.of("verified", false));
        }

        session.setAttribute("verifiedEmail", email);
        session.removeAttribute("verificationCode");
        session.removeAttribute("verificationEmailRequest");

        return ResponseEntity.ok(Map.of("verified", true));
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "members/loginForm";
    }

    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("authenticated", false));
        }

        Member member = memberService.findMember(principal.getName());
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "name", member.getName(),
                "email", member.getEmail(),
                "role", member.getRole()
        ));
    }

    private String createVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "members/forgotPassword";
    }

    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processForgotPassword(@RequestParam String email, @RequestParam String name) {
        try {
            memberService.sendTemporaryPassword(email, name);
            return ResponseEntity.ok(Map.of("success", true, "message", "이메일로 임시 비밀번호를 발송했습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/mypage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> myPage(Principal principal) {
        Member member = memberService.findMember(principal.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("name", member.getName());
        response.put("email", member.getEmail());
        response.put("address", member.getAddress());
        response.put("birthday", member.getBirthday());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/edit/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPassword(@RequestBody Map<String, String> request, Principal principal, HttpSession session) {
        String password = request.get("password");
        if (memberService.checkPassword(principal.getName(), password)) {
            session.setAttribute("editAuth", true);
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "비밀번호가 일치하지 않습니다."));
        }
    }

    @GetMapping("/edit/form")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editForm(Principal principal, HttpSession session) {
        if (session.getAttribute("editAuth") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "auth_required", "message", "비밀번호 확인이 필요합니다."));
        }

        Member member = memberService.findMember(principal.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("name", member.getName());
        response.put("email", member.getEmail());
        response.put("birthday", member.getBirthday());

        String fullAddr = member.getAddress();
        String postcode = "";
        String mainAddress = "";
        String detailAddress = "";

        if (fullAddr != null && !fullAddr.isBlank()) {
            String mainPart = fullAddr;
            String detailPart = "";
            int splitIndex = -1;
            int parenDepth = 0;
            for (int i = 0; i < fullAddr.length() - 1; i++) {
                char c = fullAddr.charAt(i);
                if (c == '(') parenDepth++;
                else if (c == ')') { if (parenDepth > 0) parenDepth--; }
                else if (c == ',' && fullAddr.charAt(i + 1) == ' ' && parenDepth == 0) {
                    splitIndex = i;
                    break;
                }
            }
            if (splitIndex != -1) {
                mainPart = fullAddr.substring(0, splitIndex);
                detailPart = fullAddr.substring(splitIndex + 2);
            }

            mainAddress = mainPart;
            if (mainPart.startsWith("(") && mainPart.indexOf(")") > 0) {
                int closeParenIndex = mainPart.indexOf(")");
                postcode = mainPart.substring(1, closeParenIndex);
                mainAddress = mainPart.substring(closeParenIndex + 1).trim();
            }
            detailAddress = detailPart;
        }

        response.put("postcode", postcode);
        response.put("mainAddress", mainAddress);
        response.put("detailAddress", detailAddress);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/edit/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMember(
            @RequestBody @Valid MemberUpdateDto memberUpdateDto,
            BindingResult bindingResult,
            Principal principal,
            HttpSession session
    ) {
        if (session.getAttribute("editAuth") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "수정 권한이 없습니다. 비밀번호를 다시 확인해주세요."));
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "입력 값을 확인해주세요.", "errors", bindingResult.getAllErrors()));
        }

        try {
            memberService.updateMember(principal.getName(), memberUpdateDto);
            session.removeAttribute("editAuth");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> withdraw(
            @RequestBody Map<String, String> request,
            Principal principal,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String password = request.get("password");
        try {
            memberService.deleteMember(principal.getName(), password);
            new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, SecurityContextHolder.getContext().getAuthentication());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}