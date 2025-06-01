// src/main/java/com/example/sleeprism/oauth2/CustomOAuth2User.java
package com.example.sleeprism.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 로그인 후 사용자 정보를 담는 Custom OAuth2User 구현체입니다.
 * Spring Security의 DefaultOAuth2User를 확장하여 추가적인 사용자 정보를 제공합니다.
 */
@Getter // Lombok을 사용하여 모든 필드에 대한 getter 메서드를 자동으로 생성합니다.
public class CustomOAuth2User extends DefaultOAuth2User {

  private Long id;        // 우리 서비스의 User 엔티티 ID
  private String username; // 우리 서비스의 User 엔티티 username (여기서는 이메일 역할)
  private String email;   // 사용자 이메일 (OAuth2 제공자로부터 받은 실제 이메일)
  private String role;    // 사용자 역할 (예: "ROLE_USER")

  /**
   * CustomOAuth2User 생성자입니다.
   *
   * @param id 우리 서비스의 User 엔티티 ID
   * @param username 우리 서비스의 User 엔티티 username (이메일 역할)
   * @param email 사용자 이메일 (OAuth2 제공자로부터 받은 실제 이메일)
   * @param role 사용자 역할 (예: "ROLE_USER")
   * @param authorities 사용자 권한 목록
   * @param attributes OAuth2 제공자로부터 받은 사용자 속성 맵
   * @param nameAttributeKey 속성 맵에서 사용자 이름을 식별하는 키
   */
  public CustomOAuth2User(Long id, String username, String email, String role,
                          Collection<? extends GrantedAuthority> authorities,
                          Map<String, Object> attributes, String nameAttributeKey) {
    super(authorities, attributes, nameAttributeKey);
    this.id = id;
    this.username = username;
    this.email = email; // 이메일 필드 추가 및 초기화
    this.role = role;
  }

  // 기존 생성자를 오버로드하거나, 위 생성자로 통합하는 것을 고려할 수 있습니다.
  // 기존 코드에서 사용하던 생성자에 맞춰 수정합니다.
  // 이전에 `CustomOAuth2User(user.getId(), user.getUsername(), user.getRole().name(), oAuth2User.getAttributes())`
  // 이렇게 호출했으므로, 이 생성자에 맞춰 email을 추가합니다.
  public CustomOAuth2User(Long id, String username, String role, Map<String, Object> attributes) {
    super(null, attributes, "id"); // authorities는 null로, nameAttributeKey는 "id"로 임시 설정 (실제 구현에 따라 다름)
    this.id = id;
    this.username = username;
    // attributes에서 email을 추출하여 설정합니다.
    this.email = (String) attributes.get("email"); // Google, Kakao 등에서 'email' 속성을 직접 제공하는 경우
    // 네이버의 경우 'response' 객체 안에 email이 있으므로, CustomOAuth2UserService에서 email을 추출하여 생성자에 전달하는 것이 더 안전합니다.
    this.role = role;
  }

  // getEmail() 메서드를 추가합니다.
  @Override
  public String getName() {
    return this.username; // Spring Security의 getName()은 UserDetails의 getUsername()과 유사하게 사용될 수 있습니다.
  }
}
