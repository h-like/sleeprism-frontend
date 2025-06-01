package com.example.sleeprism.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "login_log")
public class LoginLog {
  // BaseTimeEntity를 상속하지 않고 loginTimestamp를 직접 관리
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "login_log_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "ip_address", length = 50)
  private String ipAddress;

  @Column(name = "login_timestamp", nullable = false)
  private LocalDateTime loginTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "login_type", length = 20)
  private LoginType loginType;

  @Column(name = "success_status", nullable = false)
  private Boolean successStatus;

  @Builder
  public LoginLog(User user, String ipAddress, LoginType loginType, Boolean successStatus) {
    this.user = user;
    this.ipAddress = ipAddress;
    this.loginTimestamp = LocalDateTime.now(); // 생성 시 현재 시간
    this.loginType = loginType;
    this.successStatus = successStatus;
  }
}




