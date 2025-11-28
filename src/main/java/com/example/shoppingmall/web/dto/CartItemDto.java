package com.example.shoppingmall.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CartItemDto {
    private Long cartItemId;
    private String itemNm;
    private int price;
    private int count;
    private String imgUrl;
    private String optionName;
    private int optionPrice;

    public CartItemDto(Long cartItemId, String itemNm, int price, int count, String imgUrl, String optionName, int optionPrice) {
        this.cartItemId = cartItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.count = count;
        this.imgUrl = imgUrl;
        this.optionName = optionName;
        this.optionPrice = optionPrice;
    }
}