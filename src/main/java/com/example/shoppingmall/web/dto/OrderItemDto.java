package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.domain.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemDto {
    private String itemNm;
    private int count;
    private int orderPrice;
    private String imgUrl;
    private String optionName;

    public OrderItemDto(OrderItem orderItem, String imgUrl){
        this.itemNm = orderItem.getItem().getItemNm();
        this.count = orderItem.getCount();
        this.orderPrice = orderItem.getOrderPrice();
        this.imgUrl = imgUrl;
        this.optionName = orderItem.getOptionName();
    }
}