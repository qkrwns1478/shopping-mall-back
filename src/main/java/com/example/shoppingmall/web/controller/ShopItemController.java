package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.ItemService;
import com.example.shoppingmall.web.dto.ItemFormDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "쇼핑몰 상품", description = "일반 사용자용 상품 조회 API")
@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ShopItemController {

    private final ItemService itemService;

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{itemId}")
    public ResponseEntity<Map<String, Object>> getItemDetail(@PathVariable Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            return ResponseEntity.ok(Map.of("success", true, "data", itemFormDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "존재하지 않는 상품입니다."));
        }
    }
}