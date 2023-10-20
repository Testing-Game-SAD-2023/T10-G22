package com.example.errorsrepo;

import ch.qos.logback.classic.encoder.JsonEncoder;
import com.example.errorsrepo.error.Error;
import com.example.errorsrepo.error.ErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@CrossOrigin
@RestController
public class MainController {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ErrorRepository errorRepo;
    @GetMapping("/reportPage")
    public ModelAndView showReportPage(){
        ModelAndView m_v = new ModelAndView();
        m_v.setViewName("index");
        return m_v;
    }

    @GetMapping("/getAllErrors")
    public ResponseEntity<String> getAllError(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*"); // Consentire da tutte le origini (* per qualsiasi origine)
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE"); // Metodi consentiti
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header consentiti
        headers.add("Access-Control-Max-Age", "3600"); // Durata massima della cache delle risposte CORS
        return ResponseEntity.ok()
                .headers(headers)
                .body(errorRepo.findAll().toString());
    }

    @GetMapping("/getErrorById")
    public ResponseEntity<String> getAllError(@RequestParam("id") String identifier){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*"); // Consentire da tutte le origini (* per qualsiasi origine)
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE"); // Metodi consentiti
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header consentiti
        headers.add("Access-Control-Max-Age", "3600"); // Durata massima della cache delle risposte CORS
        return ResponseEntity.ok()
                .headers(headers)
                .body(errorRepo.findById(Integer.parseInt(identifier)).toString());
        //return ResponseEntity.ok(errorRepo.findById(Integer.parseInt(identifier)).toString());
    }

    @GetMapping("/getErrorByStatusCode")
    public ResponseEntity<String> getErrorByStatusCode(@RequestParam("statusCode") String statusCode){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*"); // Consentire da tutte le origini (* per qualsiasi origine)
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE"); // Metodi consentiti
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header consentiti
        headers.add("Access-Control-Max-Age", "3600"); // Durata massima della cache delle risposte CORS
        return ResponseEntity.ok()
                .headers(headers)
                .body(errorRepo.findByErrorCode(Integer.parseInt(statusCode)).toString());
    }

    @GetMapping("/getErrorsByDate")
    public ResponseEntity<String> getErrorByDate(@RequestParam("date")String s_data){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*"); // Consentire da tutte le origini (* per qualsiasi origine)
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE"); // Metodi consentiti
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header consentiti
        headers.add("Access-Control-Max-Age", "3600"); // Durata massima della cache delle risposte CORS
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try{
            LocalDate date_to_pass = LocalDate.parse(s_data,formatter);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(errorRepo.findByDate(date_to_pass).toString());
        }catch (Exception exp){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exp.getMessage());
        }

    }

    @GetMapping("/errorBetween")
    public ResponseEntity<String> findErrorBetween(@RequestParam("date1")String date1,
                                                   @RequestParam("date2")String date2){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*"); // Consentire da tutte le origini (* per qualsiasi origine)
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE"); // Metodi consentiti
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header consentiti
        headers.add("Access-Control-Max-Age", "3600"); // Durata massima della cache delle risposte CORS
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try{
            LocalDate date1_p = LocalDate.parse(date1,formatter);
            LocalDate date2_p = LocalDate.parse(date2,formatter);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(errorRepo.findByDateIsBetween(date1_p,date2_p).toString());
        }catch (Exception exp){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exp.getMessage());
        }

    }
    @PostMapping("/insertError")
    public ResponseEntity<String> insertError(@RequestParam("description") String desc,
                                              @RequestParam("statusCode") String statusCode,
                                              @RequestParam("fromReq") String fromReq){
        Error e = new Error();
        System.out.println("Errore ricevuto con parametri " + desc + "&&&& "+statusCode + "&&&&" + fromReq);
        try{
            e.setErrorCode(Integer.parseInt(statusCode));
            e.setDescription(desc);
            e.setFrom(fromReq);
            e.setDate(LocalDate.now());
            errorRepo.save(e);
            return ResponseEntity.status(HttpStatus.OK).body("Errore riportato");
        }catch (Exception except){
            System.out.println(except.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(except.getMessage());
        }
    }

}
