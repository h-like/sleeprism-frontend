package com.example.sleeprism.repository;

import com.example.sleeprism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  // JpaRepository<엔티티 타입, 엔티티의 ID 타입>

  // username으로 사용자 조회 (일반 로그인)
  Optional<User> findByUsername(String username);

  // email로 사용자 조회
  Optional<User> findByEmail(String email);

  // nickname으로 사용자 조회
  Optional<User> findByNickname(String nickname);

  // socialProvider와 socialId로 사용자 조회 (소셜 로그인)
  Optional<User> findBySocialProviderAndSocialId(String socialProvider, String socialId);

  // username 또는 email로 사용자 조회 (로그인 시 유연하게 사용 가능)
  Optional<User> findByUsernameOrEmail(String username, String email);

  // 특정 필드의 존재 여부 확인 (중복 체크 등에 활용)
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
}
