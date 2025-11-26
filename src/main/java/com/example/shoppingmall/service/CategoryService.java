package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Category;
import com.example.shoppingmall.repository.CategoryRepository;
import com.example.shoppingmall.web.dto.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Long saveCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalStateException("이미 존재하는 카테고리입니다.");
        }
        Category category = Category.createCategory(name);
        categoryRepository.save(category);
        return category.getId();
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoryList() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    public void deleteCategory(Long id) {
        // TODO: 해당 카테고리를 사용하는 상품이 있는지 확인 후 예외 처리 필요
        categoryRepository.deleteById(id);
    }
}