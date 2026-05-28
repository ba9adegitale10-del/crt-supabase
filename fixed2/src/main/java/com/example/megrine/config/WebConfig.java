package com.example.megrine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
            .addPathPatterns("/volunteers/**", "/families/**",
                             "/donations/**", "/stock/**",
                             "/events/**", "/training/**", "/member/**")
            .excludePathPatterns("/css/**", "/js/**", "/images/**",
                                 "/icons/**", "/manifest.json", "/sw.js");
    }
}
