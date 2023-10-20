package com.example.uigateway;

import com.example.uigateway.service.AuthJwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static final List<String> exclusionList = Collections.unmodifiableList(Arrays.asList("login","static","favicon","admin","errorRepo","controls","style","loginAdmin","register","uploadFile","","api"));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        if(!checkIfExclusionList(request.getPath()) && !checkIfApiRequest(request.getPath().subPath(1))){
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
    public static Boolean checkIfApiRequest(PathContainer subpathReq){
        String field = subpathReq.toString().split("\\/")[0];
        return field.equals("api");
    }
    public static Boolean checkIfExclusionList(RequestPath url){
        String field = url.subPath(1).toString();
        String[] fields = field.split("\\/");
        if(fields.length > 1){
            field = fields[0];
            System.out.println("[DEBUG]: più di un campo " + field);
        }else{
            field = field.split("\\.")[0];
            System.out.println("[DEBUG]: un campo " + field);
        }
        return exclusionList.contains(field);
    }
    public static String getJwtCookieValue( org.springframework.http.server.reactive.ServerHttpRequest request){
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies.isEmpty()) {
            return null;
        }
        List<HttpCookie> jwtCookies = cookies.get("jwt");
        if(jwtCookies == null || jwtCookies.isEmpty()){
            return null;
        }
        return jwtCookies.get(0).getValue();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
