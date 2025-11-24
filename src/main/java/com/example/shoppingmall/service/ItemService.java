package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Item;
import com.example.shoppingmall.repository.ItemRepository;
import com.example.shoppingmall.web.dto.ItemFormDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final FileService fileService;

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    public Long saveItem(ItemFormDto itemFormDto, MultipartFile itemImgFile) throws Exception {
        Item item = Item.createItem(itemFormDto);

        if(itemImgFile != null && !itemImgFile.isEmpty()){
            String oriImgName = itemImgFile.getOriginalFilename();
            if(!StringUtils.isEmpty(oriImgName)){
                String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
                item.setImgUrl("/images/" + imgName);
            }
        }

        itemRepository.save(item);
        return item.getId();
    }

    public Long updateItem(Long itemId, ItemFormDto itemFormDto, MultipartFile itemImgFile, boolean deleteImage) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        if (deleteImage || (itemImgFile != null && !itemImgFile.isEmpty())) {
            if (!StringUtils.isEmpty(item.getImgUrl())) {
                String savedFileName = item.getImgUrl().replace("/images/", "");
                fileService.deleteFile(itemImgLocation + "/" + savedFileName);
                item.setImgUrl(null);
            }
        }

        if (itemImgFile != null && !itemImgFile.isEmpty()) {
            String oriImgName = itemImgFile.getOriginalFilename();
            if (!StringUtils.isEmpty(oriImgName)) {
                String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
                item.setImgUrl("/images/" + imgName);
            }
        }

        item.updateItem(itemFormDto);
        return item.getId();
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = new ItemFormDto();
        itemFormDto.setItemNm(item.getItemNm());
        itemFormDto.setPrice(item.getPrice());
        itemFormDto.setStockNumber(item.getStockNumber());
        itemFormDto.setItemDetail(item.getItemDetail());
        itemFormDto.setItemSellStatus(item.getItemSellStatus());
        itemFormDto.setImgUrl(item.getImgUrl());
        return itemFormDto;
    }

    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public void deleteItem(Long itemId) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        if (!StringUtils.isEmpty(item.getImgUrl())) {
            String savedFileName = item.getImgUrl().replace("/images/", "");
            fileService.deleteFile(itemImgLocation + "/" + savedFileName);
        }

        itemRepository.delete(item);
    }
}