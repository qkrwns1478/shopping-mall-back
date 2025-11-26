package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.constant.ItemSellStatus;
import com.example.shoppingmall.domain.Item;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ItemListDto {

    private Long id;
    private String itemNm;
    private ItemSellStatus itemSellStatus;
    private int price;
    private int stockNumber;
    private LocalDateTime regTime;

    public ItemListDto(Item item) {
        this.id = item.getId();
        this.itemNm = item.getItemNm();
        this.itemSellStatus = item.getItemSellStatus();
        this.price = item.getPrice();
        this.stockNumber = item.getStockNumber();
        this.regTime = item.getRegTime();
    }
}