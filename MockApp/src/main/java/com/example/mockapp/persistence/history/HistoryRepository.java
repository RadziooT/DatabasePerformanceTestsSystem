package com.example.mockapp.persistence.history;

import com.example.mockapp.persistence.history.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {
}
