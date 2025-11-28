// src/main/java/com/example/shoppingmall/service/CartService.java
package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.*;
import com.example.shoppingmall.repository.*;
import com.example.shoppingmall.web.dto.CartItemDto;
import com.example.shoppingmall.web.dto.CartOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public Long addCart(CartOrderDto cartOrderDto, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByMemberId(member.getId());

        if(cart == null){
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        Item item = itemRepository.findById(cartOrderDto.getItemId()).orElseThrow();
        String optionName = cartOrderDto.getOptionName() == null ? "" : cartOrderDto.getOptionName();

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemIdAndOptionName(cart.getId(), item.getId(), optionName);

        if(savedCartItem != null){
            savedCartItem.addCount(cartOrderDto.getCount());
            return savedCartItem.getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartOrderDto.getCount(), optionName, cartOrderDto.getOptionPrice());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartList(String email){
        List<CartItemDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByMemberId(member.getId());

        if(cart == null){
            return cartDetailDtoList;
        }

        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByRegTimeDesc(cart.getId());

        for(CartItem ci : cartItems){
            String imgUrl = "";
            if(ci.getItem().getImgUrlList() != null && !ci.getItem().getImgUrlList().isEmpty()){
                imgUrl = ci.getItem().getImgUrlList().get(0);
            }

            CartItemDto dto = new CartItemDto(
                    ci.getId(),
                    ci.getItem().getId(),
                    ci.getItem().getItemNm(),
                    ci.getItem().getPrice(),
                    ci.getCount(),
                    imgUrl,
                    ci.getOptionName(),
                    ci.getOptionPrice(),
                    ci.getItem().isDiscount(),
                    ci.getItem().getDiscountRate()
            );
            cartDetailDtoList.add(dto);
        }

        return cartDetailDtoList;
    }

    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
        cartItemRepository.delete(cartItem);
    }

    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
        cartItem.updateCount(count);
    }

    public void mergeCart(String email, List<CartOrderDto> localCartItems) {
        if(localCartItems == null || localCartItems.isEmpty()) return;

        for (CartOrderDto item : localCartItems) {
            addCart(item, email);
        }
    }
}