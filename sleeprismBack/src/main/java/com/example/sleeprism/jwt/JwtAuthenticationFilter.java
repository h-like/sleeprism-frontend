// src/main/java/com/example/sleeprism/jwt/JwtAuthenticationFilter.java
package com.example.sleeprism.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter; // 요청당 한 번만 필터링되도록 보장

import java.io.IOException;

/**
 * JWT 토큰을 이용한 인증을 처리하는 필터입니다.
 * 모든 HTTP 요청에 대해 한 번씩 실행됩니다.
 */
@Slf4j // Lombok을 사용하여 'log' 객체를 자동으로 생성합니다.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  /**
   * JwtTokenProvider를 주입받는 생성자입니다.
   * SecurityConfig에서 이 필터를 Bean으로 등록할 때 사용됩니다.
   *
   * @param jwtTokenProvider JWT 토큰 생성 및 유효성 검사를 담당하는 서비스
   */
  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  /**
   * 실제 필터링 로직을 수행하는 메서드입니다.
   * HTTP 요청에서 JWT 토큰을 추출하고, 유효성을 검사하여 인증 정보를 설정합니다.
   *
   * @param request HttpServletRequest 객체
   * @param response HttpServletResponse 객체
   * @param filterChain FilterChain 객체
   * @throws ServletException 서블릿 오류 발생 시
   * @throws IOException I/O 오류 발생 시
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request); // HTTP 요청에서 JWT 토큰 추출

      // JWT 토큰이 존재하고 유효한 경우
      if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
        // 토큰으로부터 인증 정보를 획득합니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
        // Spring Security Context에 인증 정보를 저장합니다.
        // 이 정보는 이후 SecurityContextHolder.getContext().getAuthentication()으로 접근할 수 있습니다.
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception ex) {
      // JWT 토큰 처리 중 예외 발생 시 로그를 기록합니다.
      log.error("Could not set user authentication in security context", ex);
      // 필요에 따라 여기에 401 Unauthorized 응답을 직접 설정할 수 있습니다.
      // 예: response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
      // return; // 응답을 보냈으므로 다음 필터로 진행하지 않습니다.
    }

    // 다음 필터로 요청을 전달합니다. (필터 체인 계속 진행)
    filterChain.doFilter(request, response);
  }

  /**
   * HTTP 요청 헤더에서 "Bearer " 접두사를 가진 JWT 토큰을 추출합니다.
   *
   * @param request HttpServletRequest 객체
   * @return 추출된 JWT 토큰 문자열 (없으면 null)
   */
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    // "Authorization" 헤더가 "Bearer "로 시작하는지 확인합니다.
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // "Bearer " 접두사 (7글자)를 제거하고 실제 토큰만 반환합니다.
    }
    return null;
  }
}
