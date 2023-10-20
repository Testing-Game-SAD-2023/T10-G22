package com.example.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGateway {

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

    @Value("${JacocoService.port}")
    private String JacocoServicePort;
    @Value("${JacocoService.host}")
    private String JacocoServiceHost;
    @Value("${JacocoService.serviceName}")
    private String JacocoServiceName;

    @Value("${CUTService.port}")
    private String CUTServicePort;
    @Value("${CUTService.host}")
    private String CUTServiceHost;
    @Value("${CUTService.serviceName}")
    private String CUTServiceName;

    @Value("${InfoService.port}")
    private String InfoServicePort;
    @Value("${InfoService.host}")
    private String InfoServiceHost;
    @Value("${InfoService.serviceName}")
    private String InfoServiceName;

    @Value("${SaveDataService.port}")
    private String SaveDataServicePort;
    @Value("${SaveDataService.host}")
    private String SaveDataServiceHost;
    @Value("${SaveDataService.serviceName}")
    private String SaveDataServiceName;

    @Value("${DownloadService.port}")
    private String DownloadServicePort;
    @Value("${DownloadService.host}")
    private String DownloadServiceHost;
    @Value("${DownloadService.serviceName}")
    private String DownloadServiceName;

    @Value("${RunService.port}")
    private String RunServicePort;
    @Value("${RunService.host}")
    private String RunServiceHost;
    @Value("${RunService.serviceName}")
    private String RunServiceName;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path(GreetServiceName,"/api/"+GreetServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+GreetServiceHost+":"+GreetServicePort+"/")
                )
                .route(p -> p
                        .path(ErrorServicePort,"/api/"+ErrorServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+ErrorServiceHost+":"+ErrorServicePort+"/")
                )
                .route(p -> p
                        .path(JacocoServiceName,"/api/"+JacocoServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+JacocoServiceHost+":"+JacocoServicePort+"/")
                )
                .route(p -> p
                        .path(CUTServiceName,"/api/"+CUTServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+CUTServiceHost+":"+CUTServicePort+"/")
                )
                .route(p -> p
                        .path(InfoServiceName,"/api/"+InfoServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+InfoServiceHost+":"+InfoServicePort+"/")
                )
                .route(p -> p
                        .path(SaveDataServiceName,"/api/"+SaveDataServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+SaveDataServiceHost+":"+SaveDataServicePort+"/")
                )
                .route(p -> p
                        .path(DownloadServiceName,"/api/"+DownloadServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+DownloadServiceHost+":"+DownloadServicePort+"/")
                )
                .route(p -> p
                        .path(RunServiceName,"/api/"+RunServiceName+"/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("http://"+RunServiceHost+":"+RunServicePort+"/")
                )
                .build();
    }
}
