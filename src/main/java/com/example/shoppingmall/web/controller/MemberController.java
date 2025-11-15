package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.MemberService;
import com.example.shoppingmall.web.dto.MemberFormDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "members/signupForm";
    }

    @PostMapping("/signup")
    public String processSignUp(
            @Valid MemberFormDto memberFormDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "members/signupForm";
        }

        try {
            memberService.saveMember(memberFormDto);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "members/signupForm";
        }

        return "redirect:/";
    }

    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        if (username.length() < 4) {
            return ResponseEntity.ok(Map.of("available", false, "invalid", true));
        }
        boolean isAvailable = memberService.checkUsernameAvailability(username);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
}