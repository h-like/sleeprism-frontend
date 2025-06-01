// src/main/java/com/example/sleeprism/dto/UploadFileResponse.java
package com.example.sleeprism.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 파일 업로드 응답을 위한 데이터 전송 객체 (DTO)입니다.
 * 업로드된 파일의 정보 (이름, 다운로드 URL, 타입, 크기)를 포함합니다.
 */
@Data // Lombok: @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor를 포함합니다.
@Builder // Lombok: 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
public class UploadFileResponse {
  private String fileName;        // 업로드된 파일의 이름 (UUID + 확장자)
  private String fileDownloadUri; // 업로드된 파일의 다운로드 URL
  private String fileType;        // 파일의 Content-Type
  private long size;              // 파일의 크기 (바이트)
}
