package com.example.shoppingmall.service;

import com.example.shoppingmall.domain.Category;
import com.example.shoppingmall.domain.Item;
import com.example.shoppingmall.repository.CategoryRepository;
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

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    public String uploadImage(MultipartFile file) throws Exception {
        String oriImgName = file.getOriginalFilename();
        String imgName = fileService.uploadFile(itemImgLocation, oriImgName, file.getBytes());
        return "/images/" + imgName;
    }

    public Long saveItem(ItemFormDto itemFormDto) {
        Category category = categoryRepository.findById(itemFormDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        Item item = Item.createItem(itemFormDto, category);
        itemRepository.save(item);
        return item.getId();
    }

    public Long updateItem(Long itemId, ItemFormDto itemFormDto) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        Category category = categoryRepository.findById(itemFormDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        List<String> oldUrls = new ArrayList<>(item.getImgUrlList());
        List<String> newUrls = itemFormDto.getImgUrlList();

        for (String oldUrl : oldUrls) {
            if (!newUrls.contains(oldUrl)) {
                String savedFileName = oldUrl.replace("/images/", "");
                fileService.deleteFile(itemImgLocation + "/" + savedFileName);
            }
        }

        item.updateItem(itemFormDto, category);
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
        itemFormDto.setImgUrlList(item.getImgUrlList());
        if (item.getCategory() != null) {
            itemFormDto.setCategoryId(item.getCategory().getId());
        }
        return itemFormDto;
    }

    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public void deleteItem(Long itemId) throws Exception {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);

        for (String imgUrl : item.getImgUrlList()) {
            String savedFileName = imgUrl.replace("/images/", "");
            fileService.deleteFile(itemImgLocation + "/" + savedFileName);
        }

        itemRepository.delete(item);
    }
}