package com.example.sleeprism.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring Security의 User 클래스 임포트
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long validityInSeconds;

  public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKeyString,
                          @Value("${jwt.token-validity-in-seconds}") long validityInSeconds) {
    this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    this.validityInSeconds = validityInSeconds;
    log.info("JWT Secret Key initialized.");
  }

  // JWT 토큰 생성
  public String generateToken(Long userId, String email, String username, Collection<? extends GrantedAuthority> authorities) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + (validityInSeconds * 1000));

    String roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    return Jwts.builder()
        .subject(String.valueOf(userId)) // 토큰의 주체 (여기서는 userId)
        .claim("email", email) // 사용자 이메일 (클레임으로 추가)
        .claim("username", username) // 사용자 이름 (클레임으로 추가)
        .claim("roles", roles) // 사용자 권한 (클레임으로 추가)
        .issuedAt(now) // 발행 시간
        .expiration(expiryDate) // 만료 시간
        .signWith(secretKey, SignatureAlgorithm.HS256) // 서명 (비밀키와 알고리즘)
        .compact(); // 토큰 생성
  }

  // JWT 토큰에서 인증 정보 조회 (Spring Security의 Authentication 객체 반환)
  public Authentication getAuthentication(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("roles").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    // Spring Security의 User 객체 생성 (UserDetails 구현체)
    // claims.getSubject()는 generateToken에서 subject로 설정한 userId (String 형태)
    // claims.get("email", String.class)는 generateToken에서 클레임으로 추가한 email
    User principal = new User(claims.get("email", String.class), "", authorities);

    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  // JWT 토큰 유효성 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token);
      return true;
    } catch (SignatureException ex) {
      log.error("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      log.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty.");
    }
    return false;
  }

  // 토큰에서 사용자 ID 추출
  public Long getUserIdFromToken(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
    return Long.parseLong(claims.getSubject()); // subject는 userId
  }

  // 토큰에서 사용자 이메일 추출
  public String getUserEmailFromToken(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
    return claims.get("email", String.class);
  }
}