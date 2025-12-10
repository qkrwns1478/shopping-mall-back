package com.example.shoppingmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private int discountAmount;
    private int stock;
    private LocalDateTime validUntil;

    public void decreaseStock() {
        if (this.stock <= 0) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
        this.stock--;
    }
}