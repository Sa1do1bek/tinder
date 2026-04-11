package com.example.tinder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://semispheric-paloma-factiously.ngrok-free.dev",
                        "https://footrest-faculty-palpitate.ngrok-free.dev/",
                        "http://192.168.7.223/:8081", // here
                        "http://192.168.7.100:3000", // here
                        "http://16.170.202.6:8080",   // serverdagi frontend
                        "http://localhost:8081",       // lokalda ishlayotgan frontend
                        "http://localhost:63342",// boshqa lokal port (masalan: React/Vite)
                        "http://localhost:3000",
                        "https://maxlegal.uz"
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
