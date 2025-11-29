package com.example.shoppingmall.web.dto;

import com.example.shoppingmall.constant.ItemSellStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class ItemFormDto {

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "상품 상세 설명은 필수 입력 값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    private List<String> imgUrlList = new ArrayList<>();

    @NotNull(message = "카테고리는 필수 선택 값입니다.")
    private Long categoryId;

    private List<ItemOptionDto> itemOptionList = new ArrayList<>();

    @JsonProperty("isDiscount")
    private Boolean discount;

    private int discountRate;

    private String brand;

    @NotNull(message = "배송비는 필수 입력 값입니다. (무료배송인 경우 0)")
    private Integer deliveryFee;

    private String origin;

    public boolean isDiscount() {
        return discount != null && discount;
    }

    @JsonProperty("isPayback")
    private boolean isPayback;
}