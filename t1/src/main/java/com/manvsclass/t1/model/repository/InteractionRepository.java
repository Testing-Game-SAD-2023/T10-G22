package com.manvsclass.t1.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manvsclass.t1.model.interaction;

public interface InteractionRepository	extends MongoRepository<interaction, String> {}
