package com.manvsclass.t1.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manvsclass.t1.model.ClassUT;

public interface ClassRepository	extends MongoRepository<ClassUT,String>{

}
