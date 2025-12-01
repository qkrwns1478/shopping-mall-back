package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.OrderService;
import com.example.shoppingmall.web.dto.PaymentRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Tag(name = "주문/결제", description = "주문 생성 및 결제 처리 API")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "결제 완료 및 주문 생성", description = "PG사 결제 완료 후 서버 검증 및 주문 처리를 수행합니다.")
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completePayment(
            @RequestBody PaymentRequestDto requestDto,
            Principal principal
    ) {
        try {
            orderService.processOrder(requestDto, principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "주문이 완료되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}