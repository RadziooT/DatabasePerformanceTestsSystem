package com.example.mockapp.domain.item.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.item.model.Item;
import com.example.mockapp.persistence.item.entity.ItemEntity;
import org.springframework.stereotype.Component;

@Component
public class ItemDomainMapper implements DomainPersistenceMapper<Item, ItemEntity> {

    @Override
    public ItemEntity toEntity(Item item) {
        if (item == null) {
            return null;
        }
        return ItemEntity.builder()
                .id(item.getId())
                .imageId(item.getImageId())
                .name(item.getName())
                .price(item.getPrice())
                .data(item.getData())
                .build();
    }

    @Override
    public Item toDomain(ItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return Item.builder()
                .id(entity.getId())
                .imageId(entity.getImageId())
                .name(entity.getName())
                .price(entity.getPrice())
                .data(entity.getData())
                .build();
    }
}
