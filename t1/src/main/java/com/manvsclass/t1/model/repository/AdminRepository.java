package com.manvsclass.t1.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manvsclass.t1.model.Admin;


public interface AdminRepository	extends MongoRepository<Admin,String>{
	
}
