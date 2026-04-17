package com.example.mockapp.persistence.item;

import com.example.mockapp.persistence.item.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
}
