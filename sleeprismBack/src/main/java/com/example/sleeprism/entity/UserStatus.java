// src/main/java/com/example/sleeprism/entity/UserStatus.java
package com.example.sleeprism.entity;

/**
 * 사용자 계정의 상태를 나타내는 Enum입니다.
 * ACTIVE: 활성 사용자
 * INACTIVE: 비활성 사용자 (예: 장기 미접속)
 * SUSPENDED: 정지된 사용자
 * DELETED: 삭제된 사용자 (소프트 삭제)
 */
public enum UserStatus {
  ACTIVE,
  INACTIVE,
  SUSPENDED,
  DELETED
}
