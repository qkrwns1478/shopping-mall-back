package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.domain.Coupon;
import com.example.shoppingmall.repository.CouponRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "쿠폰", description = "쿠폰 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {

    private final CouponRepository couponRepository;

    @Operation(summary = "쿠폰 생성 (관리자)")
    @PostMapping("/admin/coupons")
    public ResponseEntity<Map<String, Object>> createCoupon(@RequestBody Map<String, Object> request) {
        Coupon coupon = new Coupon();
        coupon.setCode((String) request.get("code"));
        coupon.setName((String) request.get("name"));
        coupon.setDiscountAmount(Integer.parseInt(String.valueOf(request.get("discountAmount"))));
        coupon.setStock(Integer.parseInt(String.valueOf(request.get("stock"))));

        // 유효기간 30일
        coupon.setValidUntil(LocalDateTime.now().plusDays(30));

        couponRepository.save(coupon);

        return ResponseEntity.ok(Map.of("success", true, "message", "쿠폰이 생성되었습니다."));
    }

    @Operation(summary = "쿠폰 목록 조회 (관리자)")
    @GetMapping("/admin/coupons")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @Operation(summary = "쿠폰 코드 확인 (결제 페이지용)")
    @GetMapping("/coupons/check")
    public ResponseEntity<Map<String, Object>> checkCoupon(@RequestParam("code") String code) {
        Optional<Coupon> couponOptional = couponRepository.findByCode(code);

        if (couponOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "존재하지 않는 코드입니다."));
        }

        Coupon coupon = couponOptional.get();

        if (coupon.getStock() <= 0 || (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDateTime.now()))) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "사용할 수 없는 쿠폰입니다."));
        }

        Map<String, Object> response = Map.of(
                "valid", true,
                "id", coupon.getId(),
                "amount", coupon.getDiscountAmount(),
                "name", coupon.getName()
        );

        return ResponseEntity.ok(response);
    }
}