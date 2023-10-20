package com.example.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    public static final List<String> origins = Arrays.asList("*");

    @Bean
    public CorsWebFilter corsConf(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(origins);
        corsConfig.setMaxAge(3600L);
        source.registerCorsConfiguration("/**",corsConfig);
        return new CorsWebFilter(source);
    }
}
