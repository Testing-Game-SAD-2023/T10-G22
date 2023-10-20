package com.example.uigateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthJwtToken {
    private final RestTemplate restTemplate;
    @Value("${login.port}")
    private String AuthServicePort;
    @Value("${login.host}")
    private String AuthServiceHost;

    public AuthJwtToken(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
    public boolean verifyToken(String token){
        System.out.println("[DEBUG]: Verifico Token UI-gateway");
        MultiValueMap<String,String> formData = new LinkedMultiValueMap<String,String>();
        formData.add("jwt",token);
        Boolean isAuth = restTemplate.postForObject("http://"+AuthServiceHost+":"+AuthServicePort+"/validateToken",formData,Boolean.class);
        if(isAuth == null) return false;
        return isAuth.booleanValue();
    }
}
