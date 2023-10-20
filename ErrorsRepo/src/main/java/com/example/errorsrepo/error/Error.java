package com.example.errorsrepo.error;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Setter
@Data
@Table(name = "ErrorTbl", schema = "errorDb")
@Entity

public class Error {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer id;
    @Column(nullable = false)
    private Integer errorCode;

    private String description;
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String from_req;

    public Integer getId() {
        return id;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getFrom() {return from_req;}

    public void setId(Integer id) {this.id = id;}

    public void setErrorCode(Integer errorCode) {this.errorCode = errorCode;}

    public void setDescription(String description) throws Exception {
        if(description.isEmpty() || description.isBlank()){
            throw new Exception("Description is empty");
        }else{
            this.description = description;
        }
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setFrom(String from) throws Exception {
        if(from.isEmpty() || from.isBlank() ){
            throw new Exception("From Req is empty");
        }else{
            this.from_req = from;
        }
    }
}
