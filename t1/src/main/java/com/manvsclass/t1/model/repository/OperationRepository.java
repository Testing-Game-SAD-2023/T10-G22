package com.manvsclass.t1.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manvsclass.t1.model.Operation;

public interface OperationRepository	extends MongoRepository<Operation,String>{

}
