package com.example.shoppingmall.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CartItemDto {
    private Long cartItemId;
    private Long itemId;
    private String itemNm;
    private int price;
    private int count;
    private String imgUrl;
    private String optionName;
    private int optionPrice;

    @JsonProperty("isDiscount")
    private boolean isDiscount;
    private int discountRate;
    private int deliveryFee;
    private boolean isPayback;

    public CartItemDto(Long cartItemId, Long itemId, String itemNm, int price, int count, String imgUrl,
                       String optionName, int optionPrice, boolean isDiscount, int discountRate, int deliveryFee, boolean isPayback) {
        this.cartItemId = cartItemId;
        this.itemId = itemId;
        this.itemNm = itemNm;
        this.price = price;
        this.count = count;
        this.imgUrl = imgUrl;
        this.optionName = optionName;
        this.optionPrice = optionPrice;
        this.isDiscount = isDiscount;
        this.discountRate = discountRate;
        this.deliveryFee = deliveryFee;
        this.isPayback = isPayback;
    }
}