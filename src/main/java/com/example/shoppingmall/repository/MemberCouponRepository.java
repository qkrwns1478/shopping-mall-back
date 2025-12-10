package com.example.shoppingmall.repository;

import com.example.shoppingmall.domain.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    List<MemberCoupon> findAllByMemberId(Long memberId);

    @Query("select mc from MemberCoupon mc join fetch mc.coupon c " +
            "where mc.member.id = :memberId and mc.isUsed = false " +
            "order by c.discountAmount desc")
    List<MemberCoupon> findMyAvailableCoupons(@Param("memberId") Long memberId);
}