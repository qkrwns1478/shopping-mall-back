package com.example.shoppingmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class MemberCoupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private boolean isUsed;
    private LocalDateTime usedDate;
    private LocalDateTime issuedAt;
    private LocalDateTime expireAt;

    public static MemberCoupon createMemberCoupon(Member member, Coupon coupon) {
        MemberCoupon mc = new MemberCoupon();
        mc.setMember(member);
        mc.setCoupon(coupon);
        mc.setUsed(false);
        mc.setIssuedAt(LocalDateTime.now());
        mc.setExpireAt(coupon.getValidUntil());
        return mc;
    }

    public void use() {
        this.isUsed = true;
        this.usedDate = LocalDateTime.now();
    }

    public void restore() {
        this.isUsed = false;
        this.usedDate = null;
    }
}