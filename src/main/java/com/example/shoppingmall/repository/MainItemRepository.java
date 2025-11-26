package com.example.shoppingmall.repository;

import com.example.shoppingmall.domain.MainItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MainItemRepository extends JpaRepository<MainItem, Long> {
    @Query("select m from MainItem m join fetch m.item i order by m.orderIndex asc")
    List<MainItem> findAllOrderByOrderIndexAsc();

    boolean existsByItemId(Long itemId);
}