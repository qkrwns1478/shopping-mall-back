package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.domain.Category;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryDto {
    private Long id;
    private String name;

    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}