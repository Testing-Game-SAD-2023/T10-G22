package com.example.uigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UiGateway {
    @Value("${GreetService.port}")
    private String GreetServicePort;
    @Value("${GreetService.host}")
    private String GreetServiceHost;
    @Value("${GreetService.serviceName}")
    private String GreetServiceName;

    @Value("${ErrorService.port}")
    private String ErrorServicePort;
    @Value("${ErrorService.host}")
    private String ErrorServiceHost;
    @Value("${ErrorService.serviceName}")
    private String ErrorServiceName;

    @Value("${login.port}")
    private String LoginServicePort;
    @Value("${login.host}")
    private String LoginServiceHost;
    @Value("${login.serviceName}")
    private String LoginServiceName;

    @Value("${loginAdmin.port}")
    private String LoginAdminServicePort;
    @Value("${loginAdmin.host}")
    private String LoginAdminServiceHost;
    @Value("${loginAdmin.serviceName}")
    private String LoginAdminServiceName;

    @Value("${editor.port}")
    private String EditorServicePort;
    @Value("${editor.host}")
    private String EditorServiceHost;
    @Value("${editor.serviceName}")
    private String EditorServiceName;

    @Value("${api.port}")
    private String ApiServicePort;
    @Value("${api.host}")
    private String ApiServiceHost;
    @Value("${api.serviceName}")
    private String ApiServiceName;

    @Value("${selpage.port}")
    private String SelPagePort;
    @Value("${selpage.host}")
    private String SelPageHost;
    @Value("${selpage.serviceName}")
    private String SelPageName;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path(GreetServiceName,"/"+GreetServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+GreetServiceHost+":"+GreetServicePort+"/main")
                )
                .route(p -> p
                        .path(ErrorServiceName,"/"+ErrorServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+ErrorServiceHost+":"+ErrorServicePort+"/reportPage")
                )
                .route(p -> p
                        .path(LoginAdminServiceName,"/admin/**")
                        .filters(f -> f
                                .stripPrefix(1))
                        .uri("http://"+LoginAdminServiceHost+":"+LoginAdminServicePort+"/loginAdmin")
                )
                .route(p -> p
                        .path(EditorServiceName,"/"+EditorServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+EditorServiceHost+":"+EditorServicePort+"/")
                )
                .route(p -> p
                        .path(SelPageName,"/"+SelPageName+"/**")
                        .filters(f -> f
                                .stripPrefix(1))
                        .uri("http://"+SelPageHost+":"+SelPagePort+"/")
                )
                .route(p -> p
                        .path(ApiServiceName,"/"+"api"+"/**")
                        .filters(f -> f
                                .stripPrefix(0))
                        .uri("http://"+ApiServiceHost+":"+ApiServicePort+"/api/")
                )
                .route(p -> p
                        .path(LoginServiceName,"/**")
                        .filters(f -> f
                                .stripPrefix(0))
                        .uri("http://"+LoginServiceHost+":"+LoginServicePort+"/login")
                )
                .build();
    }
}
