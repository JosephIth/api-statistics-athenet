package com.athenet.statistics.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
    Orígenes CORS configurables para los endpoints REST de lectura del marcador.
    En perfil dev (application-dev.yml) se permite "*".
*/
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/marcador/**")
                .allowedOriginPatterns(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("Accept", "Content-Type", "Origin")
                .maxAge(3600L);
    }
}
