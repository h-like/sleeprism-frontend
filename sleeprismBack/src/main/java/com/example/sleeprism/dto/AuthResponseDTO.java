// src/main/java/com/example/sleeprism/dto/AuthResponseDTO.java
package com.example.sleeprism.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 인증 응답을 위한 데이터 전송 객체 (DTO)입니다.
 * JWT 액세스 토큰 및 사용자 관련 정보를 포함합니다.
 */
@Getter // Lombok을 사용하여 모든 필드에 대한 getter 메서드를 자동으로 생성합니다.
@Setter // Lombok을 사용하여 모든 필드에 대한 setter 메서드를 자동으로 생성합니다.
@Builder // Lombok을 사용하여 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
public class AuthResponseDTO {
  private String accessToken;

  // @Builder.Default를 사용하여, 빌더를 통해 tokenType이 명시적으로 설정되지 않았을 때
  // "Bearer" 값을 기본값으로 사용하도록 합니다.
  @Builder.Default
  private String tokenType = "Bearer";

  private Long userId;
  private String email;
  private String username;
  private String role;

}
