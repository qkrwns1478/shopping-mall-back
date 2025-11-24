package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Item;
import com.example.shoppingmall.repository.ItemRepository;
import com.example.shoppingmall.web.dto.ItemFormDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Long saveItem(ItemFormDto itemFormDto) {
        Item item = Item.createItem(itemFormDto);
        itemRepository.save(item);
        return item.getId();
    }

    public Long updateItem(Long itemId, ItemFormDto itemFormDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        return item.getId();
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        ItemFormDto itemFormDto = new ItemFormDto();
        itemFormDto.setItemNm(item.getItemNm());
        itemFormDto.setPrice(item.getPrice());
        itemFormDto.setStockNumber(item.getStockNumber());
        itemFormDto.setItemDetail(item.getItemDetail());
        itemFormDto.setItemSellStatus(item.getItemSellStatus());

        return itemFormDto;
    }

    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        itemRepository.delete(item);
    }
}