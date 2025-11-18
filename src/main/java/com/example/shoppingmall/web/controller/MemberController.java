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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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

    @GetMapping("/mypage")
    public String myPage(Model model, Principal principal, @RequestParam(required = false) String error) {
        Member member = memberService.findMember(principal.getName());

        MemberUpdateDto dto = new MemberUpdateDto();
        dto.setName(member.getName());
        dto.setAddress(member.getAddress());
        dto.setBirthday(member.getBirthday());

        model.addAttribute("memberUpdateDto", dto);
        model.addAttribute("email", member.getEmail());

        String fullAddr = member.getAddress();
        /* 괄호 안의 콤마는 무시하고 괄호 밖의 콤마를 기준으로 파싱해야 함 */
        if (fullAddr != null && !fullAddr.isBlank()) {
            String mainPart = fullAddr;
            String detailPart = "";

            int splitIndex = -1;
            int parenDepth = 0;
            for (int i = 0; i < fullAddr.length() - 1; i++) {
                char c = fullAddr.charAt(i);
                if (c == '(') {
                    parenDepth++;
                } else if (c == ')') {
                    if (parenDepth > 0) parenDepth--;
                } else if (c == ',' && fullAddr.charAt(i + 1) == ' ' && parenDepth == 0) {
                    splitIndex = i;
                    break;
                }
            }

            if (splitIndex != -1) {
                mainPart = fullAddr.substring(0, splitIndex);
                detailPart = fullAddr.substring(splitIndex + 2);
            }

            String postcode = "";
            String roadAddress = mainPart;

            if (mainPart.startsWith("(") && mainPart.contains(")")) {
                int closeParenIndex = mainPart.indexOf(")");
                postcode = mainPart.substring(1, closeParenIndex);
                roadAddress = mainPart.substring(closeParenIndex + 1).trim();
            }

            model.addAttribute("postcode", postcode);
            model.addAttribute("mainAddress", roadAddress);
            model.addAttribute("detailAddress", detailPart);
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "members/myPage";
    }

    @PostMapping("/mypage/update")
    public String updateMember(@Valid MemberUpdateDto memberUpdateDto, BindingResult bindingResult, Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("email", principal.getName());
            return "members/myPage";
        }
        try {
            memberService.updateMember(principal.getName(), memberUpdateDto);
            model.addAttribute("successMessage", "회원 정보가 수정되었습니다.");

            model.addAttribute("email", principal.getName());
            memberUpdateDto.setCurrentPassword("");
            memberUpdateDto.setNewPassword("");
            memberUpdateDto.setNewPasswordConfirm("");

            return "members/myPage";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", principal.getName());
            return "members/myPage";
        }
    }

    @PostMapping("/mypage/delete")
    public String deleteMember(@RequestParam String password, Principal principal, HttpServletRequest request, HttpServletResponse response) {
        try {
            memberService.deleteMember(principal.getName(), password);
            new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/";
        } catch (IllegalStateException e) {
            String encodedMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/members/mypage?error=" + encodedMsg;
        }
    }
}