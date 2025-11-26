package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.MainItemService;
import com.example.shoppingmall.web.dto.MainItemDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "메인 페이지", description = "메인 상품 관리 API")
@RestController
@RequiredArgsConstructor
public class MainItemController {

    private final MainItemService mainItemService;

    @Operation(summary = "메인 상품 목록 조회")
    @GetMapping("/api/main/items")
    public ResponseEntity<List<MainItemDto>> getMainItems() {
        return ResponseEntity.ok(mainItemService.getMainItemList());
    }

    @Operation(summary = "메인 상품 추가")
    @PostMapping("/admin/main/items")
    public ResponseEntity<Map<String, Object>> addMainItem(@RequestBody Map<String, Long> request) {
        try {
            mainItemService.addMainItem(request.get("itemId"));
            return ResponseEntity.ok(Map.of("success", true, "message", "메인 상품으로 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "메인 상품 삭제")
    @DeleteMapping("/admin/main/items/{mainItemId}")
    public ResponseEntity<Map<String, Object>> deleteMainItem(@PathVariable Long mainItemId) {
        try {
            mainItemService.deleteMainItem(mainItemId);
            return ResponseEntity.ok(Map.of("success", true, "message", "메인 상품에서 제외되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}