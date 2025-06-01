// src/main/java/com/example/sleeprism/controller/FileController.java
package com.example.sleeprism.controller;

import com.example.sleeprism.dto.UploadFileResponse;
import com.example.sleeprism.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 파일 업로드 및 다운로드를 처리하는 REST 컨트롤러입니다.
 */
@Slf4j // Lombok을 사용하여 'log' 객체를 자동으로 생성합니다.
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class FileController {

  private final FileStorageService fileStorageService;

  /**
   * 단일 파일을 업로드합니다.
   *
   * @param file 업로드할 MultipartFile 객체
   * @return 업로드된 파일 정보가 담긴 UploadFileResponse DTO
   */
  @PostMapping("/uploadFile")
  public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
    // 파일을 "general-uploads" 디렉토리에 업로드하고 상대 경로를 반환받습니다.
    String relativePath;
    try {
      relativePath = fileStorageService.uploadFile(file, "general-uploads");
    } catch (IOException e) {
      log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
      // 실제 애플리케이션에서는 더 구체적인 에러 처리 (예: Custom Exception, ResponseEntity.status) 필요
      throw new RuntimeException("File upload failed: " + e.getMessage());
    }

    // 파일 다운로드 URI를 생성합니다.
    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/api/files/downloadFile/")
        .path(relativePath) // 상대 경로를 사용하여 다운로드 URI를 완성합니다.
        .toUriString();

    // UploadFileResponse DTO를 빌더 패턴으로 생성하여 반환합니다.
    return UploadFileResponse.builder()
        .fileName(relativePath.substring(relativePath.lastIndexOf('/') + 1)) // 파일명만 추출
        .fileDownloadUri(fileDownloadUri)
        .fileType(file.getContentType())
        .size(file.getSize())
        .build();
  }

  /**
   * 다중 파일을 업로드합니다.
   *
   * @param files 업로드할 MultipartFile 배열
   * @return 업로드된 각 파일 정보가 담긴 UploadFileResponse DTO 리스트
   */
  @PostMapping("/uploadMultipleFiles")
  public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
    // 각 파일을 uploadFile 메서드를 통해 처리하고 결과를 리스트로 수집합니다.
    return Arrays.asList(files)
        .stream()
        .map(this::uploadFile)
        .collect(Collectors.toList());
  }

  /**
   * 파일을 다운로드합니다.
   *
   * @param fileName 다운로드할 파일의 상대 경로 (예: general-uploads/uuid.jpg)
   * @param request HttpServletRequest (Content-Type 결정을 위해 사용)
   * @return 파일 Resource와 함께 ResponseEntity (다운로드 형태로)
   */
  @GetMapping("/downloadFile/{fileName:.+}") // 파일명에 . (점)이 포함될 수 있도록 패턴 설정 (예: general-uploads/image.jpg)
  public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
    // 파일 로드
    Resource resource;
    try {
      resource = fileStorageService.loadAsResource(fileName); // 파일의 상대 경로를 전달하여 Resource를 로드합니다.
    } catch (IOException e) {
      log.error("Failed to load file: {}", fileName, e);
      // 실제 애플리케이션에서는 404 Not Found 또는 다른 적절한 에러 응답 처리
      return ResponseEntity.notFound().build();
    }

    // 파일의 Content-Type 결정
    String contentType = null;
    try {
      // 리소스의 파일 경로를 사용하여 ServletContext에서 MIME 타입을 가져옵니다.
      contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
    } catch (IOException ex) {
      log.info("Could not determine file type for {}. Defaulting to application/octet-stream.", fileName);
    }

    // Content-Type이 결정되지 않았으면 기본값 설정
    if (contentType == null) {
      contentType = "application/octet-stream"; // 일반적인 이진 파일
    }

    // ResponseEntity를 구성하여 파일 다운로드 응답을 반환합니다.
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"") // 다운로드 형태로
        .body(resource);
  }
}
