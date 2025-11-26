package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Item;
import com.example.shoppingmall.domain.MainItem;
import com.example.shoppingmall.repository.ItemRepository;
import com.example.shoppingmall.repository.MainItemRepository;
import com.example.shoppingmall.web.dto.MainItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MainItemService {

    private final MainItemRepository mainItemRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<MainItemDto> getMainItemList() {
        return mainItemRepository.findAllOrderByOrderIndexAsc().stream()
                .map(MainItemDto::new)
                .collect(Collectors.toList());
    }

    public void addMainItem(Long itemId) {
        if (mainItemRepository.existsByItemId(itemId)) {
            throw new IllegalStateException("이미 메인 상품으로 등록되어 있습니다.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));

        int nextOrder = (int) mainItemRepository.count() + 1;
        MainItem mainItem = MainItem.createMainItem(item, nextOrder);
        mainItemRepository.save(mainItem);
    }

    public void deleteMainItem(Long mainItemId) {
        mainItemRepository.deleteById(mainItemId);
    }
}