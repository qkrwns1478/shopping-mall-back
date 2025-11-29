package com.example.shoppingmall.domain;

import com.example.shoppingmall.constant.ItemSellStatus;
import com.example.shoppingmall.web.dto.ItemFormDto;
import com.example.shoppingmall.domain.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name="item")
@Getter @Setter
@ToString
public class Item {

    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String itemNm;

    @Column(name="price", nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockNumber;

    @Lob
    @Column(nullable = false)
    private String itemDetail;

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    @ElementCollection
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "img_url")
    private List<String> imgUrlList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private Double rating = 0.0;

    private int reviewCount = 0;

    @ElementCollection
    @CollectionTable(name = "item_options", joinColumns = @JoinColumn(name = "item_id"))
    private List<ItemOption> itemOptions = new ArrayList<>();

    private boolean isDiscount;

    private int discountRate;

    private int viewCount = 0;

    private int salesCount = 0;

    @Column(length = 50)
    private String brand;

    private int deliveryFee;

    @Column(length = 50)
    private String origin;

    private boolean isDeleted = false;

    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    private boolean isPayback;

    public static Item createItem(ItemFormDto itemFormDto, Category category) {
        Item item = new Item();
        item.setItemNm(itemFormDto.getItemNm());
        item.setPrice(itemFormDto.getPrice());
        item.setStockNumber(itemFormDto.getStockNumber());
        item.setItemDetail(itemFormDto.getItemDetail());
        item.setItemSellStatus(itemFormDto.getItemSellStatus());
        item.setImgUrlList(itemFormDto.getImgUrlList());
        item.setCategory(category);

        if (itemFormDto.getItemOptionList() != null) {
            List<ItemOption> options = itemFormDto.getItemOptionList().stream()
                    .map(dto -> new ItemOption(dto.getOptionName(), dto.getExtraPrice()))
                    .collect(Collectors.toList());
            item.setItemOptions(options);
        }

        item.setDiscount(itemFormDto.isDiscount());
        item.setDiscountRate(itemFormDto.getDiscountRate());
        item.setBrand(itemFormDto.getBrand());
        item.setDeliveryFee(itemFormDto.getDeliveryFee());
        item.setOrigin(itemFormDto.getOrigin());
        item.setRating(0.0);
        item.setReviewCount(0);
        item.setViewCount(0);
        item.setSalesCount(0);
        item.setDeleted(false);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        item.setPayback(itemFormDto.isPayback());
        return item;
    }

    public void updateItem(ItemFormDto itemFormDto, Category category) {
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
        this.imgUrlList = itemFormDto.getImgUrlList();
        this.category = category;

        if (itemFormDto.getItemOptionList() != null) {
            List<ItemOption> options = itemFormDto.getItemOptionList().stream()
                    .map(dto -> new ItemOption(dto.getOptionName(), dto.getExtraPrice()))
                    .collect(Collectors.toList());
            this.itemOptions = options;
        }

        this.isDiscount = itemFormDto.isDiscount();
        this.discountRate = itemFormDto.getDiscountRate();
        this.brand = itemFormDto.getBrand();
        this.deliveryFee = itemFormDto.getDeliveryFee();
        this.origin = itemFormDto.getOrigin();
        this.isPayback = itemFormDto.isPayback();
        this.updateTime = LocalDateTime.now();
    }

    public void addViewCount() {
        this.viewCount++;
    }

    public void addSalesCount(int count) {
        this.salesCount += count;
    }
}