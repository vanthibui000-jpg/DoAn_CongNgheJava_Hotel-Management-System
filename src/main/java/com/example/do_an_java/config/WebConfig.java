package com.example.do_an_java.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;
    private final String uploadImagesRoot;

    public WebConfig(LoginInterceptor loginInterceptor,
                     @Value("${app.upload.images-root:uploads/Images}") String uploadImagesRoot) {
        this.loginInterceptor = loginInterceptor;
        this.uploadImagesRoot = uploadImagesRoot;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/admin/**",
                        "/dashboard",
                        "/doi-mat-khau",
                        "/khach-hang",
                        "/khach-hang/**"
                )
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/Images/**",
                        "/error"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadImagesPath = Paths.get(uploadImagesRoot)
                .toAbsolutePath()
                .normalize();
        Path bundledImagesPath = Paths.get("src", "main", "resources", "static", "Images")
                .toAbsolutePath()
                .normalize();
        registry.addResourceHandler("/Images/**")
                .addResourceLocations(
                        uploadImagesPath.toUri().toString(),
                        bundledImagesPath.toUri().toString(),
                        "classpath:/static/Images/");
    }
}
