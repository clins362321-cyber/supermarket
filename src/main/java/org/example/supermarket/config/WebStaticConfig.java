package org.example.supermarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:E:/supermarket-uploads}")
    private String uploadDir;

    @Value("${file.access-prefix:/files}")
    private String accessPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件（图片/视频）
        registry.addResourceHandler(accessPrefix + "/**")
                .addResourceLocations("file:" + uploadDir + "/");

        // 前端 SPA 静态资源：优先匹配 /api、/files 的请求由控制器或上面处理
        // 其余请求从 classpath:/static/ 取资源，不存在则回退到 index.html
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}

