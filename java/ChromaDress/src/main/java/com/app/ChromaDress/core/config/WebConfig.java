package com.app.ChromaDress.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload.dir}")
  private String uploadDir;

  public void addResourceHandlers(ResourceHandlerRegistry registry) {

    String formattedPath = uploadDir.replace("\\", "/");
    if (!formattedPath.startsWith("/")) {
      formattedPath = "/" + formattedPath;
    }
    if (!formattedPath.endsWith("/")) {
      formattedPath += "/";
    }

    registry.addResourceHandler("/uploads/clothing/**")
        .addResourceLocations("file:" + formattedPath);
  }
}
