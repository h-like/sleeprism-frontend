// src/main/java/com/example/sleeprism/oauth2/CustomOAuth2UserService.java
package com.example.sleeprism.oauth2;

import com.example.sleeprism.entity.User;
import com.example.sleeprism.entity.UserRole;
import com.example.sleeprism.entity.UserStatus; // UserStatus 임포트 추가
import com.example.sleeprism.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth2 로그인 시 사용자 정보를 로드하고 처리하는 서비스입니다.
 * DefaultOAuth2UserService를 확장하여 소셜 로그인 사용자 정보를 우리 서비스의 User 엔티티와 동기화합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;

  /**
   * OAuth2 사용자 정보를 로드합니다.
   *
   * @param userRequest OAuth2 사용자 요청
   * @return 로드된 OAuth2User 객체 (CustomOAuth2User)
   * @throws OAuth2AuthenticationException 인증 과정 중 오류 발생 시
   */
  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, naver, kakao
    String socialId = getSocialId(registrationId, oAuth2User.getAttributes()); // 각 소셜에서 제공하는 고유 ID

    String email = null;
    String nickname = null;
    String profileImageUrl = null;

    // 소셜 서비스별로 사용자 정보 추출 로직
    if ("google".equals(registrationId)) {
      email = (String) oAuth2User.getAttributes().get("email");
      nickname = (String) oAuth2User.getAttributes().get("name");
      profileImageUrl = (String) oAuth2User.getAttributes().get("picture");
    } else if ("naver".equals(registrationId)) {
      Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
      if (response != null) {
        email = (String) response.get("email");
        nickname = (String) response.get("nickname");
        profileImageUrl = (String) response.get("profile_image");
      }
    } else if ("kakao".equals(registrationId)) {
      Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
      if (kakaoAccount != null) {
        email = (String) kakaoAccount.get("email");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile != null) {
          nickname = (String) profile.get("nickname");
          profileImageUrl = (String) profile.get("profile_image_url");
        }
      }
    }

    Optional<User> existingUser = userRepository.findBySocialProviderAndSocialId(registrationId, socialId);

    User user;
    if (existingUser.isPresent()) {
      // 이미 존재하는 소셜 로그인 사용자: 정보 업데이트 (예: 프로필 이미지 변경)
      user = existingUser.get();
      // TODO: user.update(nickname, profileImageUrl, email 등); // 필요한 경우 업데이트 메서드 호출
      // 현재 User 엔티티에 updateNicknameAndEmail, updateProfileImageUrl 메서드가 있으므로 활용 가능
      if (nickname != null && !nickname.isEmpty()) {
        user.updateNicknameAndEmail(nickname, user.getEmail()); // 닉네임만 업데이트
      }
      if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
        user.updateProfileImageUrl(profileImageUrl);
      }
      // 이메일은 보통 소셜 로그인에서 고정되므로 업데이트하지 않거나, 변경 로직 필요
    } else {
      // 새로운 소셜 로그인 사용자: 회원가입 처리
      user = User.builder()
          .username(registrationId + "_" + socialId) // 예: google_12345 (고유한 사용자 이름)
          .password(UUID.randomUUID().toString()) // 소셜 로그인 사용자 비밀번호는 의미 없지만, 필드가 non-nullable이면 임의의 값 할당
          .nickname(nickname != null ? nickname : registrationId + "_" + UUID.randomUUID().toString().substring(0, 8)) // 닉네임 없으면 임의 생성
          .email(email) // 추출한 이메일 설정
          .profileImageUrl(profileImageUrl)
          .socialProvider(registrationId)
          .socialId(socialId)
          .role(UserRole.USER)
          .status(UserStatus.ACTIVE) // UserStatus 임포트 및 사용
          .build();
      userRepository.save(user);
    }
    // CustomUserDetails 객체에 OAuth2User 정보와 User 엔티티를 함께 저장
    // JWT 발급을 위해서는 사용자 정보를 좀 더 구체적으로 담는 CustomUserDetails를 만드는 것이 일반적
    // CustomOAuth2User 생성자에 email 인자를 추가하여 전달합니다.
    return new CustomOAuth2User(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "id");
  }

  // 각 소셜 서비스별 고유 ID 추출 (필요에 따라 다름)
  private String getSocialId(String registrationId, Map<String, Object> attributes) {
    if ("google".equals(registrationId)) {
      return (String) attributes.get("sub");
    } else if ("naver".equals(registrationId)) {
      Map<String, Object> response = (Map<String, Object>) attributes.get("response");
      return (String) response.get("id");
    } else if ("kakao".equals(registrationId)) {
      return String.valueOf(attributes.get("id")); // 카카오는 id가 Long 타입으로 올 수 있음
    }
    return null;
  }
}
