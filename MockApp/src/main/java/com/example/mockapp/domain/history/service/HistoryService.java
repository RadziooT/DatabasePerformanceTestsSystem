package com.example.mockapp.domain.history.service;

import com.example.mockapp.domain.history.mapper.HistoryDomainMapper;
import com.example.mockapp.domain.history.model.History;
import com.example.mockapp.persistence.history.HistoryRepository;
import com.example.mockapp.persistence.history.entity.HistoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryService {

	private final HistoryRepository historyRepository;
	private final HistoryDomainMapper mapper;

	public History create(History history) {
		HistoryEntity saved = historyRepository.save(mapper.toEntity(history));
		return mapper.toDomain(saved);
	}
}
