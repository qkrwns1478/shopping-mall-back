package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.domain.MainItem;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MainItemDto {
    private Long id;
    private Long itemId;
    private String itemNm;
    private int price;
    private String imgUrl;
    private String itemDetail;

    public MainItemDto(MainItem mainItem) {
        this.id = mainItem.getId();
        this.itemId = mainItem.getItem().getId();
        this.itemNm = mainItem.getItem().getItemNm();
        this.price = mainItem.getItem().getPrice();
        this.itemDetail = mainItem.getItem().getItemDetail();

        if (!mainItem.getItem().getImgUrlList().isEmpty()) {
            this.imgUrl = mainItem.getItem().getImgUrlList().get(0);
        }
    }
}