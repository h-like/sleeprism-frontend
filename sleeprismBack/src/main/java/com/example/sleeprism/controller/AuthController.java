// src/main/java/com/example/sleeprism/controller/AuthController.java
package com.example.sleeprism.controller;

import com.example.sleeprism.dto.AuthRequestDTO;
import com.example.sleeprism.dto.AuthResponseDTO;
import com.example.sleeprism.jwt.JwtTokenProvider;
import com.example.sleeprism.entity.User;
import com.example.sleeprism.entity.UserRole;
import com.example.sleeprism.entity.UserStatus; // UserStatus 임포트 추가
import com.example.sleeprism.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 인증 및 등록을 처리하는 REST 컨트롤러입니다.
 * JWT 기반 인증을 사용합니다.
 */
@Slf4j // Lombok을 사용하여 'log' 객체를 자동으로 생성합니다.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 사용자 로그인을 처리하고 JWT 토큰을 발급합니다.
   *
   * @param authRequestDto 로그인 요청 DTO (이메일, 비밀번호 포함)
   * @return 인증 응답 DTO (액세스 토큰, 사용자 ID, 이메일, 역할 등)
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequestDto) {
    // 1. 사용자 인증 시도: 이메일과 비밀번호를 사용하여 UsernamePasswordAuthenticationToken을 생성하고 인증 매니저에 전달합니다.
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword())
    );

    // 2. 인증 성공 시 SecurityContext에 Authentication 객체 저장:
    //    이후 요청에서 사용자 인증 정보를 사용할 수 있도록 SecurityContext에 저장합니다.
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 3. UserDetails에서 사용자 정보 추출 및 JWT 토큰 생성:
    //    인증된 Principal(주체)에서 UserDetails를 가져와 실제 User 엔티티를 조회합니다.
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    User user = userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("User not found after authentication")); // 이메일로 사용자 조회, 없으면 예외 발생

    // JWT 토큰을 생성합니다.
    String token = jwtTokenProvider.generateToken(
        user.getId(), // 사용자 ID
        user.getEmail(), // 사용자 이메일
        user.getUsername(), // User 엔티티의 username 필드
        authentication.getAuthorities() // 사용자의 권한 목록
    );

    // 로그에 로그인 성공 메시지를 기록합니다.
    log.info("User {} logged in successfully. Token generated.", user.getEmail());

    // 4. 응답 DTO 생성: 생성된 토큰과 사용자 정보를 포함하는 AuthResponseDTO를 빌드합니다.
    AuthResponseDTO response = AuthResponseDTO.builder()
        .accessToken(token) // 발급된 JWT 토큰
        .userId(user.getId()) // 사용자 ID
        .email(user.getEmail()) // 사용자 이메일
        .username(user.getUsername()) // 사용자 이름
        .role(user.getRole().name()) // 사용자의 역할 (enum 이름을 문자열로 변환)
        .build();

    // 200 OK 응답과 함께 응답 DTO를 반환합니다.
    return ResponseEntity.ok(response);
  }

  /**
   * 사용자 등록을 처리합니다.
   *
   * @param authRequestDto 등록 요청 DTO (이메일, 비밀번호, 사용자 이름 포함)
   * @return 등록 성공 메시지 또는 에러 메시지
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody AuthRequestDTO authRequestDto) {
    // 1. 이메일 중복 확인: 이미 등록된 이메일인지 확인합니다.
    if (userRepository.findByEmail(authRequestDto.getEmail()).isPresent()) {
      // 중복된 이메일이면 400 Bad Request 응답을 반환합니다.
      return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
    }

    // 2. 새 사용자 엔티티 생성: AuthRequestDTO의 정보를 사용하여 User 엔티티를 빌드합니다.
    User newUser = User.builder()
        .email(authRequestDto.getEmail()) // 이메일 설정
        .password(passwordEncoder.encode(authRequestDto.getPassword())) // 비밀번호를 암호화하여 저장합니다.
        // username이 제공되면 사용하고, 없으면 이메일을 기본값으로 설정합니다.
        .username(authRequestDto.getUsername() != null ? authRequestDto.getUsername() : authRequestDto.getEmail())
        .nickname(authRequestDto.getNickname())
        .role(UserRole.USER) // 기본 권한은 UserRole.USER로 설정합니다.
        .status(UserStatus.ACTIVE) // <--- 이 라인을 추가했습니다! (UserStatus.ACTIVE로 기본값 설정)
        .build();

    // 3. 사용자 저장: 새로 생성된 사용자 엔티티를 데이터베이스에 저장합니다.
    userRepository.save(newUser);
    // 로그에 사용자 등록 성공 메시지를 기록합니다.
    log.info("User {} registered successfully with role {}", newUser.getEmail(), newUser.getRole());
    // 201 Created 응답과 함께 성공 메시지를 반환합니다.
    return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
  }
}
