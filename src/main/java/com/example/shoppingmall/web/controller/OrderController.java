package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.OrderService;
import com.example.shoppingmall.web.dto.OrderHistDto;
import com.example.shoppingmall.web.dto.PaymentRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Tag(name = "주문/결제", description = "주문 생성, 결제 처리 및 내역 조회 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "결제 완료 및 주문 생성")
    @PostMapping("/payment/complete")
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

    @Operation(summary = "주문 내역 조회")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderHistDto>> getOrderList(Principal principal) {
        List<OrderHistDto> orderHistDtoList = orderService.getOrderList(principal.getName());
        return ResponseEntity.ok(orderHistDtoList);
    }

    @Operation(summary = "주문 목록 조회 (관리자)")
    @GetMapping("/admin/orders")
    public ResponseEntity<Map<String, Object>> getAdminOrderList(@RequestParam(value = "page", defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("orderDate").descending());
        Page<OrderHistDto> orderHistDtoPage = orderService.getAdminOrderPage(pageable);

        return ResponseEntity.ok(Map.of(
                "content", orderHistDtoPage.getContent(),
                "totalPages", orderHistDtoPage.getTotalPages(),
                "totalElements", orderHistDtoPage.getTotalElements(),
                "number", orderHistDtoPage.getNumber()
        ));
    }

    @Operation(summary = "주문 취소 (관리자)")
    @PostMapping("/admin/orders/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable("orderId") Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true, "message", "주문이 취소되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}