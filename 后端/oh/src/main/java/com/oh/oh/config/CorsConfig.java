package com.oh.oh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 匹配所有接口
                        .allowedOrigins("*") // 允许的源（生产环境建议指定具体域名）
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的方法
                        .allowedHeaders("*") // 允许的请求头
                        .allowCredentials(false) // 是否允许发送 Cookie
                        .maxAge(3600); // 预检请求缓存时间（秒）
            }
        };
    }
}
