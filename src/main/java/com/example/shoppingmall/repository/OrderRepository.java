package com.example.shoppingmall.repository;

import com.example.shoppingmall.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMemberIdOrderByOrderDateDesc(Long memberId);
}