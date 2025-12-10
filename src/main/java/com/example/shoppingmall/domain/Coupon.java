package com.example.shoppingmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter @Setter
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private int discountAmount;
    private LocalDateTime validUntil;

    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        this.regTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}