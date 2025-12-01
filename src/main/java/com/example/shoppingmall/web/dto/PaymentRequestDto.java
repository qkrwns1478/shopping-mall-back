package com.example.shoppingmall.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter @Setter
@ToString
public class PaymentRequestDto {
    private String paymentId;
    private String orderId;
    private int amount;
    private int usedPoints;
    private List<OrderItemDto> orderItems;

    @Getter @Setter
    @ToString
    public static class OrderItemDto {
        private Long cartItemId;
        private Long itemId;
        private int count;
        private String optionName;
        private int price;
    }
}