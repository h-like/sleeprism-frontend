package com.example.sleeprism.security;

import com.example.sleeprism.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 데이터베이스에서 사용자 이름(username)으로 사용자 정보를 조회
    // 여기서는 이메일을 username으로 사용한다고 가정합니다.
    return userRepository.findByEmail(username) // findByUsername 또는 findByEmail (이메일을 로그인 ID로 사용할 경우)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
  }
}
