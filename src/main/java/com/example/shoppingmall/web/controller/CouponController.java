package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.domain.Coupon;
import com.example.shoppingmall.domain.Member;
import com.example.shoppingmall.domain.MemberCoupon;
import com.example.shoppingmall.repository.CouponRepository;
import com.example.shoppingmall.repository.MemberCouponRepository;
import com.example.shoppingmall.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "쿠폰(Coupon)", description = "쿠폰 생성, 발급, 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Operation(summary = "쿠폰 생성 (관리자)", description = "관리자가 새로운 쿠폰을 생성합니다.")
    @PostMapping("/admin/coupons")
    public ResponseEntity<Map<String, Object>> createCoupon(@RequestBody Map<String, Object> request) {
        Coupon coupon = new Coupon();
        coupon.setCode((String) request.get("code"));
        coupon.setName((String) request.get("name"));
        coupon.setDiscountAmount(Integer.parseInt(String.valueOf(request.get("discountAmount"))));

        String validUntilStr = (String) request.get("validUntil");
        if (validUntilStr != null && !validUntilStr.isEmpty()) {
            LocalDate date = LocalDate.parse(validUntilStr, DateTimeFormatter.ISO_DATE);
            coupon.setValidUntil(date.atTime(LocalTime.MAX));
        } else {
            coupon.setValidUntil(LocalDateTime.now().plusDays(30));
        }

        couponRepository.save(coupon);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "쿠폰이 생성되었습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "쿠폰 목록 조회 (관리자)", description = "관리자가 등록된 모든 쿠폰 목록을 조회합니다.")
    @GetMapping("/admin/coupons")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @Operation(summary = "쿠폰 일괄 지급 (관리자)", description = "선택한 회원들에게 특정 쿠폰을 일괄 지급합니다.")
    @PostMapping("/admin/coupons/bulk-issue")
    public ResponseEntity<Map<String, Object>> bulkIssueCoupon(@RequestBody Map<String, Object> request) {
        Long couponId = Long.parseLong(String.valueOf(request.get("couponId")));
        List<Integer> memberIdsInt = (List<Integer>) request.get("memberIds");
        List<Long> memberIds = memberIdsInt.stream().map(Integer::longValue).collect(Collectors.toList());

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        int successCount = 0;
        int failCount = 0;

        for (Long memberId : memberIds) {
            if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
                failCount++;
                continue;
            }

            try {
                Member member = memberRepository.findById(memberId).orElse(null);
                if (member != null) {
                    MemberCoupon mc = MemberCoupon.createMemberCoupon(member, coupon);
                    memberCouponRepository.save(mc);
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", String.format("총 %d명 중 %d명 지급 성공 (중복/실패 %d명)", memberIds.size(), successCount, failCount));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "쿠폰 발급 (사용자)", description = "사용자가 특정 쿠폰을 다운로드(발급) 받습니다.")
    @PostMapping("/api/coupons/{couponId}/issue")
    public ResponseEntity<Map<String, Object>> issueCoupon(@PathVariable("couponId") Long couponId, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Member member = memberRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 없음"));

        if (memberCouponRepository.existsByMemberIdAndCouponId(member.getId(), couponId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미 발급받은 쿠폰입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        MemberCoupon mc = MemberCoupon.createMemberCoupon(member, coupon);
        memberCouponRepository.save(mc);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "쿠폰이 발급되었습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 쿠폰 목록 조회", description = "사용자가 보유한 쿠폰 목록을 조회합니다.")
    @GetMapping("/api/my-coupons")
    public ResponseEntity<List<Map<String, Object>>> getMyCoupons(
            Principal principal,
            @RequestParam(value = "type", defaultValue = "active") String type
    ) {
        if (principal == null) return ResponseEntity.status(401).build();
        Member member = memberRepository.findByEmail(principal.getName()).orElseThrow();

        List<MemberCoupon> allCoupons = memberCouponRepository.findAllByMemberId(member.getId());

        LocalDateTime now = LocalDateTime.now();

        List<Map<String, Object>> result = allCoupons.stream()
                .filter(mc -> {
                    boolean isExpired = mc.getExpireAt() != null && mc.getExpireAt().isBefore(now);
                    boolean isUsed = mc.isUsed();

                    if ("active".equals(type)) {
                        return !isUsed && !isExpired;
                    } else {
                        return isUsed || isExpired;
                    }
                })
                .map(mc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberCouponId", mc.getId());
                    map.put("name", mc.getCoupon().getName());
                    map.put("amount", mc.getCoupon().getDiscountAmount());
                    map.put("validUntil", mc.getExpireAt());
                    map.put("used", mc.isUsed());
                    map.put("expired", mc.getExpireAt() != null && mc.getExpireAt().isBefore(now));
                    if (mc.isUsed()) {
                        map.put("usedDate", mc.getUsedDate());
                    }
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}