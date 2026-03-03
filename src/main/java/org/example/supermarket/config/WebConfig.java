package org.example.supermarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 配置，方便前端 Vue3 开发环境联调
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 开发阶段放开前端来源，避免端口/域名不一致导致 CORS 问题
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                // 不在前后端之间使用 Cookie，简单返回通配 CORS 头即可
                .maxAge(3600);
    }
}

