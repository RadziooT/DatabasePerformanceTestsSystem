package com.example.mockapp.domain.item.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.item.mapper.ItemDomainMapper;
import com.example.mockapp.domain.item.model.Item;
import com.example.mockapp.persistence.item.entity.ItemEntity;
import com.example.mockapp.persistence.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemDomainMapper mapper;

    public Item create(Item item) {
        ItemEntity saved = itemRepository.save(mapper.toEntity(item));
        return mapper.toDomain(saved);
    }

    public Item getById(Long id) {
        ItemEntity entity = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + id));
        return mapper.toDomain(entity);
    }
}
