// src/main/java/com/example/sleeprism/dto/UserResponseDTO.java
package com.example.sleeprism.dto;

import com.example.sleeprism.entity.User;
import com.example.sleeprism.entity.UserRole;
import com.example.sleeprism.entity.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 응답을 위한 데이터 전송 객체 (DTO)입니다.
 * 사용자 프로필 정보를 포함합니다.
 */
@Getter // Lombok을 사용하여 모든 필드에 대한 getter 메서드를 자동으로 생성합니다.
@Setter // Lombok을 사용하여 모든 필드에 대한 setter 메서드를 자동으로 생성합니다.
@NoArgsConstructor // Lombok을 사용하여 인자 없는 생성자를 자동으로 생성합니다.
public class UserResponseDTO {
  private Long id;
  private String username;        // 사용자 이름 (고유 식별자)
  private String nickname;        // 사용자 닉네임 (표시 이름)
  private String email;
  private String profileImageUrl;
  private UserRole role;
  private UserStatus status;
  private String socialProvider;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * User 엔티티를 기반으로 UserResponseDTO 객체를 생성하는 생성자입니다.
   *
   * @param user 변환할 User 엔티티 객체
   */
  public UserResponseDTO(User user) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.nickname = user.getNickname();        // User 엔티티에서 닉네임 가져오기
    this.email = user.getEmail();
    this.profileImageUrl = user.getProfileImageUrl(); // User 엔티티에서 프로필 이미지 URL 가져오기
    this.role = user.getRole();
    this.status = user.getStatus();            // User 엔티티에서 상태 가져오기
    this.socialProvider = user.getSocialProvider(); // User 엔티티에서 소셜 제공자 가져오기
    this.createdAt = user.getCreatedAt();
    this.updatedAt = user.getUpdatedAt();
  }
}
