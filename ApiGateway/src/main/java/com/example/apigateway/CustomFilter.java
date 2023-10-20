package com.example.apigateway;

import com.example.apigateway.service.AuthJwtToken;
import io.netty.handler.codec.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;


@Component
public class CustomFilter implements GlobalFilter, Ordered {

    @Autowired
    private AuthJwtToken authTokenService;

    @Value("ErrorService.host")
    private String errorRepoHost;

    @Value("ErrorService.port")
    private String errorRepoPort;

    final private RestTemplate restTemplate;

    @Autowired
    public CustomFilter(RestTemplate rest){
        this.restTemplate = rest;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        if(!request.getPath().subPath(1).toString().contains("/errorRepo/") && !request.getPath().subPath(1).toString().contains("/classManager/")){
            System.out.println("[DEBUG]: Controllo...");
            if(authTokenService.verifyToken(getJwtCookieValue(request))){
                System.out.println("Autorizzato");
                return chain.filter(exchange).then(Mono.fromRunnable(()->{
                    var response = exchange.getResponse();
                    if(response.getStatusCode() != HttpStatus.OK) {
                        makePostRequest(request,response);
                    }
                }));
            }else{
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    /*System.out.println("Parametro richiesta: "+ (request.getPath().subPath(1).toString().contains("/errorRepo/") || authTokenService.verifyToken(getJwtCookieValue(request))));
        if(request.getPath().subPath(1).toString().contains("/errorRepo/")){

    }

        if(authTokenService.verifyToken(getJwtCookieValue(request))){
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            var response = exchange.getResponse();
            if(response.getStatusCode() != HttpStatus.OK) {
                makePostRequest(request,response);
            }
        }));
    }else{

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }*/

    public void makePostRequest(org.springframework.http.server.reactive.ServerHttpRequest request, org.springframework.http.server.reactive.ServerHttpResponse response){
        System.out.println("Malfunzionamento " + response.getStatusCode() + " Richiesta: " + request.getPath() + " " + response.getHeaders());
        System.out.println("Status code: "+response.getStatusCode().value());
        String url = "http://errorRepo:8086/insertError?description="+request.getBody().toString()+"&statusCode="+response.getStatusCode().value()+"&fromReq="+request.getPath().toString();
        try{
            WebClient client = WebClient.create();
            //String requestBody = "{\"description\":"+ response.getHeaders() +",\"statusCode\":"+response.getStatusCode()+ ", \"fromReq\":"+request.getPath()+"}";
            client
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(null)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(responseBody -> {
                        System.out.println(responseBody.toString());
                    });

        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    public static String getJwtCookieValue( org.springframework.http.server.reactive.ServerHttpRequest request){
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        System.out.println("URI: "+request.getURI()+"PATH: "+request.getPath());
        if (cookies.isEmpty()) {
            System.out.println("[DEBUG]: I cookie non ci sono");
            return null;
        }
        List<HttpCookie> jwtCookies = cookies.get("jwt");
        if(jwtCookies == null || jwtCookies.isEmpty()){
            System.out.println("[DEBUG]: I cookie non ci sono 2");
            return null;
        }
        System.out.println("[DEBUG]: I cookie ci sono: "+ jwtCookies.get(0).getValue());
        return jwtCookies.get(0).getValue();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
