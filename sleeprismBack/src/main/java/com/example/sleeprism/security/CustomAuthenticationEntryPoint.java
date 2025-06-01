// src/main/java/com/example/sleeprism/security/CustomAuthenticationEntryPoint.java
package com.example.sleeprism.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 호출되는 진입점입니다.
 * 401 Unauthorized 응답을 반환합니다.
 */
@Slf4j // Lombok을 사용하여 'log' 객체를 자동으로 생성합니다.
@Component // 스프링 빈으로 등록합니다.
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /**
   * 인증되지 않은 접근이 발생했을 때 호출되는 메서드입니다.
   *
   * @param request HttpServletRequest 객체
   * @param response HttpServletResponse 객체
   * @param authException 발생한 인증 예외
   * @throws IOException I/O 오류 발생 시
   * @throws ServletException 서블릿 오류 발생 시
   */
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    // 인증되지 않은 접근에 대한 오류를 로그로 기록합니다.
    log.error("Unauthorized error: {}", authException.getMessage());
    // 클라이언트에 401 Unauthorized 상태 코드와 함께 오류 메시지를 보냅니다.
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
  }
}
