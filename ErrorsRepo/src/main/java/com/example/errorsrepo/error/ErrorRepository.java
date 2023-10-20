package com.example.errorsrepo.error;


import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface ErrorRepository extends JpaRepository<Error,Integer> {
    @Override
    List<Error> findAll();
    List<Error> findByDate(LocalDate data);
    List<Error> findByDateIsBetween(LocalDate data1,LocalDate data2);
    Optional<Error> findById(Integer Id);
    List<Error> findByErrorCode(Integer statusCode);


}
