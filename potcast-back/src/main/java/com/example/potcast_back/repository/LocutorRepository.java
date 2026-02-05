package com.example.potcast_back.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.potcast_back.model.Locutor;

public interface LocutorRepository extends CrudRepository<Locutor, String> {
}