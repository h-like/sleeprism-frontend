package com.example.sleeprism.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

  @Value("${file.upload-dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // /files/** 경로로 요청이 오면 C:\\upload 디렉토리에서 파일을 찾도록 설정
    registry.addResourceHandler("/files/**") // 웹에서 접근할 경로
        .addResourceLocations("file:" + uploadDir + "/"); // 실제 파일 시스템 경로
  }
}
