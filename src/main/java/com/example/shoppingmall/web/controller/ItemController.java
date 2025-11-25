package com.example.shoppingmall.web.controller;

import com.example.shoppingmall.domain.Item;
import com.example.shoppingmall.service.ItemService;
import com.example.shoppingmall.web.dto.ItemFormDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "상품(Item)", description = "상품 등록, 수정, 삭제 및 조회 API")
@Controller
@RequestMapping("/admin/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "이미지 업로드", description = "이미지를 업로드하고 URL을 반환합니다.")
    @PostMapping("/image/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imgUrl = itemService.uploadImage(file);
            return ResponseEntity.ok(Map.of("success", true, "url", imgUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이미지 업로드 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @PostMapping("/new")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> itemNew(@RequestBody @Valid ItemFormDto itemFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return getErrorResponse(bindingResult);
        }
        try {
            itemService.saveItem(itemFormDto);
            return ResponseEntity.ok(Map.of("success", true, "message", "상품이 성공적으로 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "상품 목록 조회", description = "관리자용 상품 목록을 페이징하여 조회합니다.")
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> itemList(@RequestParam(value = "page", defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Item> items = itemService.getAdminItemPage(pageable);
        return ResponseEntity.ok(Map.of(
                "content", items.getContent(),
                "totalPages", items.getTotalPages(),
                "totalElements", items.getTotalElements(),
                "number", items.getNumber()
        ));
    }

    @Operation(summary = "상품 상세 조회", description = "수정을 위해 특정 상품의 상세 정보를 조회합니다.")
    @GetMapping("/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> itemDtl(@PathVariable("itemId") Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            return ResponseEntity.ok(Map.of("success", true, "data", itemFormDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "존재하지 않는 상품입니다."));
        }
    }

    @Operation(summary = "상품 수정", description = "기존 상품의 정보를 수정합니다.")
    @PostMapping("/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> itemUpdate(@PathVariable("itemId") Long itemId, @RequestBody @Valid ItemFormDto itemFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return getErrorResponse(bindingResult);
        }
        try {
            itemService.updateItem(itemId, itemFormDto);
            return ResponseEntity.ok(Map.of("success", true, "message", "상품이 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "상품 삭제", description = "특정 상품을 삭제합니다.")
    @DeleteMapping("/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> itemDelete(@PathVariable("itemId") Long itemId) {
        try {
            itemService.deleteItem(itemId);
            return ResponseEntity.ok(Map.of("success", true, "message", "상품이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, Object>> getErrorResponse(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(", ");
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "입력 값을 확인해주세요: " + sb.toString()));
    }
}