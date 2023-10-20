package com.example.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthJwtToken {

    private final RestTemplate restTemplate;

    @Value("${AuthService.port}")
    private String AuthServicePort;
    @Value("${AuthService.host}")
    private String AuthServiceHost;

    public AuthJwtToken(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public boolean verifyToken(String token){
        //return true;
        MultiValueMap<String,String> formData = new LinkedMultiValueMap<String,String>();
        formData.add("jwt",token);
        Boolean isAuth = restTemplate.postForObject("http://"+AuthServiceHost+":"+AuthServicePort+"/validateToken",formData,Boolean.class);
        System.out.println("[DEBUG] autenticato: "+isAuth.booleanValue()+" token: "+token);
        if(isAuth == null){
            System.out.println("Non sei loggato");
            return false;
        }
        return isAuth.booleanValue();
    }

}
