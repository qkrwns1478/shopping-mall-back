package com.example.shoppingmall.web.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class PaymentRequestDto {
    private String paymentId;
    private String orderId;
    private int amount;
    private int usedPoints;
    private List<OrderItemDto> orderItems;

    @Getter @Setter
    public static class OrderItemDto {
        private Long cartItemId;
        private Long itemId;
        private int count;
        private String optionName;
        private int price;
    }
}