package com.example.sleeprism.service;

import com.example.sleeprism.dto.UserProfileUpdateRequestDTO;
import com.example.sleeprism.dto.UserResponseDTO;
import com.example.sleeprism.dto.UserSignInRequestDTO;
import com.example.sleeprism.dto.UserSignUpRequestDTO;

import com.example.sleeprism.entity.LoginLog;
import com.example.sleeprism.entity.LoginType;
import com.example.sleeprism.entity.User;
import com.example.sleeprism.entity.UserRole;
import com.example.sleeprism.repository.LoginLogRepository;
import com.example.sleeprism.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // 비밀번호 암호화를 위해 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional; // Optional 임포트 추가

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final LoginLogRepository loginLogRepository;
  private final PasswordEncoder passwordEncoder; // SecurityConfig에서 빈으로 등록해야 함
  private final FileStorageService fileStorageService; // 파일 업로드 서비스 (별도 구현 필요)

  // 회원가입
  @Transactional
  public UserResponseDTO signUp(UserSignUpRequestDTO requestDto) {
    // 사용자 이름, 닉네임, 이메일 중복 확인
    if (userRepository.existsByUsername(requestDto.getUsername())) {
      throw new EntityExistsException("Username already exists.");
    }
    if (userRepository.existsByNickname(requestDto.getNickname())) {
      throw new EntityExistsException("Nickname already exists.");
    }
    if (userRepository.existsByEmail(requestDto.getEmail())) {
      throw new EntityExistsException("Email already exists.");
    }

    User newUser = User.builder()
        .username(requestDto.getUsername())
        .password(passwordEncoder.encode(requestDto.getPassword())) // 비밀번호 암호화
        .nickname(requestDto.getNickname())
        .email(requestDto.getEmail())
        .profileImageUrl(null) // 초기에는 프로필 이미지 없음
        .role(UserRole.USER) // 기본 역할 USER
        .status(com.example.sleeprism.entity.UserStatus.ACTIVE) // 기본 상태 ACTIVE
        .build();

    User savedUser = userRepository.save(newUser);
    return new UserResponseDTO(savedUser);
  }

  // 일반 로그인 (Spring Security의 인증 과정과 연동됨)
  // 이 메서드는 주로 로그인 시도 성공/실패 로그를 남기는 용도로 사용될 수 있습니다.
  // 실제 인증은 Spring Security 필터 체인에서 처리합니다.
  @Transactional
  public UserResponseDTO signIn(UserSignInRequestDTO requestDto, String ipAddress) {
    Optional<User> userOptional = userRepository.findByUsernameOrEmail(requestDto.getUsernameOrEmail(), requestDto.getUsernameOrEmail());

    boolean successStatus = false;
    User user = null;
    try {
      user = userOptional.orElseThrow(() -> new EntityNotFoundException("User not found or invalid credentials."));

      if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
        throw new IllegalArgumentException("Invalid password.");
      }
      successStatus = true;
      // 로그인 성공 시 추가 로직 (예: JWT 토큰 생성 및 반환)
      // 실제 구현에서는 이 메서드가 로그인 성공 후 UserDetails 객체를 반환하거나,
      // Controller에서 토큰을 생성하여 반환하는 흐름이 됩니다.
      return new UserResponseDTO(user);
    } finally {
      // 로그인 시도 로그 기록 (성공/실패 여부와 관계없이)
      LoginLog loginLog = LoginLog.builder()
          .user(userOptional.orElse(null)) // 유저를 찾지 못했으면 null
          .ipAddress(ipAddress)
          .loginType(LoginType.NORMAL)
          .successStatus(successStatus)
          .build();
      loginLogRepository.save(loginLog);
    }
  }

  // 사용자 정보 조회
  public UserResponseDTO getUserProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    return new UserResponseDTO(user);
  }

  // 사용자 프로필 업데이트
  @Transactional
  public UserResponseDTO updateProfile(Long userId, UserProfileUpdateRequestDTO requestDto, MultipartFile profileImageFile) throws IOException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

    // 닉네임 중복 확인 (본인 제외)
    if (!user.getNickname().equals(requestDto.getNickname()) && userRepository.existsByNickname(requestDto.getNickname())) {
      throw new EntityExistsException("Nickname already exists.");
    }
    // 이메일 중복 확인 (본인 제외)
    if (!user.getEmail().equals(requestDto.getEmail()) && userRepository.existsByEmail(requestDto.getEmail())) {
      throw new EntityExistsException("Email already exists.");
    }

    user.updateNicknameAndEmail(requestDto.getNickname(), requestDto.getEmail()); // User 엔티티에 update 메서드 추가 필요

    // 프로필 이미지 파일이 있다면 업로드 및 URL 업데이트
    if (profileImageFile != null && !profileImageFile.isEmpty()) {
      String imageUrl = fileStorageService.uploadFile(profileImageFile, "profile-images"); // "profile-images"는 저장할 디렉토리/버킷 경로 예시
      user.updateProfileImageUrl(imageUrl); // User 엔티티에 updateProfileImageUrl 메서드 추가 필요
    } else if (profileImageFile != null && profileImageFile.isEmpty() && user.getProfileImageUrl() != null) {
      // 파일이 비어있는 채로 전달된 경우 (기존 이미지 삭제 요청)
      fileStorageService.deleteFile(user.getProfileImageUrl()); // 기존 이미지 삭제
      user.updateProfileImageUrl(null); // URL 초기화
    }

    // save를 명시적으로 호출하지 않아도 @Transactional에 의해 변경 감지(Dirty Checking) 후 업데이트됨
    return new UserResponseDTO(user);
  }

  // 사용자 삭제 (탈퇴)
  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    userRepository.delete(user); // 실제 삭제 (Soft Delete를 원하면 User 엔티티에 isDeleted 필드 추가)
  }
}