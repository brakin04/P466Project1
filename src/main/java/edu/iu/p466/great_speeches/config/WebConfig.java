package edu.iu.p466.great_speeches.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Use "file:" to point to a local directory. 
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();
        // Ensure the path is absolute or relative to project root.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath); 
    }
}
