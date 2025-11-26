package com.example.shoppingmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "main_item")
@Getter @Setter
@NoArgsConstructor
public class MainItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "main_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int orderIndex;

    public static MainItem createMainItem(Item item, int orderIndex) {
        MainItem mainItem = new MainItem();
        mainItem.setItem(item);
        mainItem.setOrderIndex(orderIndex);
        return mainItem;
    }
}