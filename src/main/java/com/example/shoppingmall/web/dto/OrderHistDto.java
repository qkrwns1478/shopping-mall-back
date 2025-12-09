package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.constant.OrderStatus;
import com.example.shoppingmall.domain.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class OrderHistDto {
    private Long orderId;
    private String orderDate;
    private OrderStatus orderStatus;
    private int totalAmount;
    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    public OrderHistDto(Order order){
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getStatus();
        this.totalAmount = order.getTotalAmount();
    }

    public void addOrderItemDto(OrderItemDto orderItemDto){
        orderItemDtoList.add(orderItemDto);
    }
}