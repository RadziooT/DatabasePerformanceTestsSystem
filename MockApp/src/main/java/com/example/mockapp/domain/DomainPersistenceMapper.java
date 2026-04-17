package com.example.mockapp.domain;

public interface DomainPersistenceMapper<D, E> {

    E toEntity(D domain);

    D toDomain(E entity);
}
