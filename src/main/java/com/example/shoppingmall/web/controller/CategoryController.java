package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.service.CategoryService;
import com.example.shoppingmall.web.dto.CategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "카테고리", description = "카테고리 관리 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회")
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getList() {
        return ResponseEntity.ok(categoryService.getCategoryList());
    }

    @Operation(summary = "카테고리 등록")
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCategory(@RequestBody Map<String, String> request) {
        try {
            categoryService.saveCategory(request.get("name"));
            return ResponseEntity.ok(Map.of("success", true, "message", "카테고리가 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "카테고리 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "카테고리가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}