package com.manvsclass.t1.model.repository;

import java.util.List;


import com.manvsclass.t1.model.ClassUT;

public interface SearchRepository {

	List<ClassUT> findByText(String text);
}
