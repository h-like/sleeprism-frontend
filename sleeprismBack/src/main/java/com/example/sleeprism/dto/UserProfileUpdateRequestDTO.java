package com.example.sleeprism.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserProfileUpdateRequestDTO {
  @NotBlank(message = "Nickname cannot be blank.")
  @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters.")
  private String nickname;

  @NotBlank(message = "Email cannot be blank.")
  @Email(message = "Invalid email format.")
  @Size(max = 100, message = "Email cannot exceed 100 characters.")
  private String email;

  // 프로필 이미지 파일 (선택 사항)
  private MultipartFile profileImageFile; // 파일 자체를 받기 위해 MultipartFile 사용
}
