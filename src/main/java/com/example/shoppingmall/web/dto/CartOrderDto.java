package com.example.shoppingmall.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CartOrderDto {
    private Long itemId;
    private int count;
    private String optionName;
    private int optionPrice;
}