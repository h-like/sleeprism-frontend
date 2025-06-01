// src/main/java/com/example/sleeprism/oauth2/OAuth2AuthenticationSuccessHandler.java
package com.example.sleeprism.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 인증 성공 시 처리 핸들러입니다.
 * JWT 토큰을 생성하여 프론트엔드 URL로 리다이렉트합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  private final com.example.sleeprism.jwt.JwtTokenProvider jwtTokenProvider;

  // application.properties에서 프론트엔드 리다이렉트 URL 값을 주입받습니다.
  @Value("${oauth2.redirect.front-url}")
  private String frontendRedirectUrl;

  /**
   * OAuth2 인증 성공 시 호출되는 메서드입니다.
   * JWT 토큰을 생성하고, 토큰을 포함한 URL로 프론트엔드를 리다이렉트합니다.
   *
   * @param request HttpServletRequest 객체
   * @param response HttpServletResponse 객체
   * @param authentication 인증된 Authentication 객체 (CustomOAuth2User 포함)
   * @throws IOException I/O 오류 발생 시
   * @throws ServletException 서블릿 오류 발생 시
   */
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    // 인증된 Principal에서 CustomOAuth2User 객체를 가져옵니다.
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    // JWT 토큰을 생성합니다.
    // generateToken 메서드의 시그니처: Long userId, String email, String username, Collection<? extends GrantedAuthority> authorities
    String jwtToken = jwtTokenProvider.generateToken(
        oAuth2User.getId(),              // userId
        oAuth2User.getEmail(),           // email (CustomOAuth2User에 getEmail()이 있다고 가정)
        oAuth2User.getUsername(),        // username (Spring Security의 UserDetails.getUsername() 역할)
        oAuth2User.getAuthorities()      // authorities
    );

    // 프론트엔드 리다이렉트 URL에 JWT 토큰을 쿼리 파라미터로 추가하여 최종 URL을 생성합니다.
    String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
        .queryParam("token", jwtToken)
        .build().toUriString();

    // 생성된 URL로 클라이언트를 리다이렉트합니다.
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}
