package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.CartService;
import com.example.shoppingmall.web.dto.CartItemDto;
import com.example.shoppingmall.web.dto.CartOrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Tag(name = "장바구니", description = "장바구니 관리 API")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 담기")
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCart(@RequestBody CartOrderDto cartOrderDto, Principal principal){
        try {
            cartService.addCart(cartOrderDto, principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "장바구니에 담았습니다."));
        } catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "장바구니 조회")
    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCartList(Principal principal){
        return ResponseEntity.ok(cartService.getCartList(principal.getName()));
    }

    @Operation(summary = "장바구니 수량 수정")
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(@PathVariable Long cartItemId, @RequestBody Map<String, Integer> request){
        try {
            cartService.updateCartItemCount(cartItemId, request.get("count"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "장바구니 삭제")
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Map<String, Object>> deleteCartItem(@PathVariable Long cartItemId){
        try {
            cartService.deleteCartItem(cartItemId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "비회원 장바구니 병합")
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeCart(@RequestBody List<CartOrderDto> localCartItems, Principal principal) {
        try {
            cartService.mergeCart(principal.getName(), localCartItems);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}