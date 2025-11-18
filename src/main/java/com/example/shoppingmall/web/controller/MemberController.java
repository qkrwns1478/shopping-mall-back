package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.EmailService;
import com.example.shoppingmall.service.MemberService;
import com.example.shoppingmall.web.dto.MemberFormDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
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
    public String processSignUp(
            @Valid MemberFormDto memberFormDto,
            BindingResult bindingResult,
            Model model,
            HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return "members/signupForm";
        }

        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        if (!memberFormDto.getEmail().equals(verifiedEmail)) {
            bindingResult.rejectValue("email", "email.unverified", "이메일 인증이 완료되지 않았습니다.");
            return "members/signupForm";
        }

        try {
            memberService.saveMember(memberFormDto);
            session.removeAttribute("verifiedEmail");
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "members/signupForm";
        }

        return "redirect:/members/signup-success";
    }

    @GetMapping("/signup-success")
    public String showSignUpSuccess() {
        return "members/signupSuccess";
    }

    /* @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        if (email.isBlank()) {
            return ResponseEntity.ok(Map.of("available", false, "invalid", true));
        }
        boolean isAvailable = memberService.checkEmailAvailability(email);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    } */

    @PostMapping("/send-verification-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(
            @RequestParam String email,
            HttpSession session
    ) {
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
        session.setMaxInactiveInterval(300); // 5분

        return ResponseEntity.ok(Map.of("success", true, "message", "인증번호가 발송되었습니다."));
    }

    @PostMapping("/verify-code")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyCode(
            @RequestParam String email,
            @RequestParam String code,
            HttpSession session
    ) {
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
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            model.addAttribute("loginErrorMsg", "이메일 또는 비밀번호를 확인해주세요.");
        }

        return "members/loginForm";
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
    public String processForgotPassword(@RequestParam String email,
                                        @RequestParam String name,
                                        Model model) {
        try {
            memberService.sendTemporaryPassword(email, name);
            model.addAttribute("successMessage", "이메일로 임시 비밀번호를 발송했습니다.");
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "members/forgotPassword";
    }
}