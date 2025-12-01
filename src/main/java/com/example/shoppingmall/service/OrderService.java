package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Member;
import com.example.shoppingmall.repository.MemberRepository;
import com.example.shoppingmall.web.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final CartService cartService;

    public void processOrder(PaymentRequestDto request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        // TODO: 결제 금액 검증

        if (request.getUsedPoints() > 0) {
            member.usePoints(request.getUsedPoints());
        }

        // TODO: 주문 정보 저장

        List<Long> orderCartItemIds = request.getOrderItems().stream()
                .map(PaymentRequestDto.OrderItemDto::getCartItemId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (!orderCartItemIds.isEmpty()) {
            cartService.deleteCartItems(orderCartItemIds, email);
        }
    }
}