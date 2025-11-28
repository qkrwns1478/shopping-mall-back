package com.example.shoppingmall.repository;

import com.example.shoppingmall.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndItemIdAndOptionName(Long cartId, Long itemId, String optionName);

    List<CartItem> findByCartIdOrderByRegTimeDesc(Long cartId);
}