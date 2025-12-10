package com.example.shoppingmall.service;

import com.example.shoppingmall.constant.OrderStatus;
import com.example.shoppingmall.domain.*;
import com.example.shoppingmall.repository.*;
import com.example.shoppingmall.web.dto.OrderHistDto;
import com.example.shoppingmall.web.dto.OrderItemDto;
import com.example.shoppingmall.web.dto.PaymentRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MemberCouponRepository memberCouponRepository;

    @Value("${portone.api.secret}")
    private String apiSecret;

    private static final String PORTONE_API_URL = "https://api.portone.io";

    public void processOrder(PaymentRequestDto request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        List<OrderItem> orderItemList = new ArrayList<>();
        int dbTotalItemPrice = 0;
        int totalDeliveryFee = 0;

        int couponDiscount = 0;
        if (request.getMemberCouponId() != null) {
            MemberCoupon memberCoupon = memberCouponRepository.findById(request.getMemberCouponId())
                    .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 쿠폰입니다."));

            if (!memberCoupon.getMember().getEmail().equals(email)) {
                throw new IllegalStateException("본인의 쿠폰만 사용할 수 있습니다.");
            }
            if (memberCoupon.isUsed()) {
                throw new IllegalStateException("이미 사용된 쿠폰입니다.");
            }
            if (memberCoupon.getExpireAt() != null && memberCoupon.getExpireAt().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("유효기간이 만료된 쿠폰입니다.");
            }

            couponDiscount = memberCoupon.getCoupon().getDiscountAmount();
            memberCoupon.use();
        }

        for (PaymentRequestDto.OrderItemDto itemDto : request.getOrderItems()) {
            Item item = itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new IllegalStateException("상품을 찾을 수 없습니다. ID: " + itemDto.getItemId()));

            int price = item.isDiscount()
                    ? (int)(item.getPrice() * (1 - item.getDiscountRate() / 100.0))
                    : item.getPrice();

            int optionPrice = 0;
            if (itemDto.getOptionName() != null && !itemDto.getOptionName().isEmpty()) {
                optionPrice = item.getItemOptions().stream()
                        .filter(opt -> opt.getOptionName().equals(itemDto.getOptionName()))
                        .findFirst()
                        .map(ItemOption::getExtraPrice)
                        .orElse(0);
            }

            int finalPrice = price + optionPrice;

            totalDeliveryFee += item.getDeliveryFee();

            OrderItem orderItem = OrderItem.createOrderItem(item, itemDto.getCount(), finalPrice, itemDto.getOptionName());
            orderItemList.add(orderItem);

            dbTotalItemPrice += (finalPrice * itemDto.getCount());
        }

        int expectedAmount = dbTotalItemPrice + totalDeliveryFee - request.getUsedPoints() - couponDiscount;
        if (expectedAmount < 0) expectedAmount = 0;

        if (request.getAmount() > 0) {
            verifyPaymentWithPortOne(request.getPaymentId(), request.getAmount());
        }

        if (request.getAmount() != expectedAmount) {
            throw new IllegalStateException("결제 금액 정보가 일치하지 않습니다. (요청: " + request.getAmount() + ", 계산: " + expectedAmount + ")");
        }

        if (request.getUsedPoints() > 0) {
            member.usePoints(request.getUsedPoints());
        }

        int totalPayback = 0;
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getItem().isPayback()) {
                totalPayback += (int)(orderItem.getOrderPrice() * orderItem.getCount() * 0.1);
            }
        }
        if (totalPayback > 0) {
            member.addPoints(totalPayback);
        }

        Order order = Order.createOrder(member, orderItemList, request.getPaymentId(), request.getUsedPoints(), request.getAmount());
        orderRepository.save(order);

        List<Long> orderCartItemIds = request.getOrderItems().stream()
                .map(PaymentRequestDto.OrderItemDto::getCartItemId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (!orderCartItemIds.isEmpty()) {
            cartService.deleteCartItems(orderCartItemIds, email);
        }
    }

    private void verifyPaymentWithPortOne(String paymentId, int amount) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "PortOne " + apiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    PORTONE_API_URL + "/payments/" + paymentId,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                String status = root.path("status").asText();
                int paidAmount = root.path("amount").path("total").asInt();

                if (!"PAID".equals(status)) {
                    throw new IllegalStateException("결제가 완료되지 않았습니다. 상태: " + status);
                }
                if (paidAmount != amount) {
                    throw new IllegalStateException("결제 금액이 일치하지 않습니다. (실결제: " + paidAmount + ", 요청: " + amount + ")");
                }
            } else {
                throw new IllegalStateException("결제 정보를 조회할 수 없습니다. 응답 코드: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException("결제 검증에 실패했습니다: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderHistDto> getOrderList(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        List<Order> orders = orderRepository.findByMemberIdOrderByOrderDateDesc(member.getId());
        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                String imgUrl = "";
                if (!orderItem.getItem().getImgUrlList().isEmpty()) {
                    imgUrl = orderItem.getItem().getImgUrlList().get(0);
                }
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, imgUrl);
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }

        return orderHistDtos;
    }

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getAdminOrderPage(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(order -> {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                String imgUrl = "";
                if (!orderItem.getItem().getImgUrlList().isEmpty()) {
                    imgUrl = orderItem.getItem().getImgUrlList().get(0);
                }
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, imgUrl);
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            return orderHistDto;
        });
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));
        if (order.getStatus() == OrderStatus.CANCEL) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        order.cancelOrder();
    }
}