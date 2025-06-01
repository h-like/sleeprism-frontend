package com.example.sleeprism.controller;

import com.example.sleeprism.dto.PostCreateRequestDTO;
import com.example.sleeprism.dto.PostResponseDTO;
import com.example.sleeprism.dto.PostUpdateRequestDTO;
import com.example.sleeprism.entity.PostCategory;
import com.example.sleeprism.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // RESTful API 컨트롤러
@RequestMapping("/api/posts") // 기본 URL 경로 설정
@RequiredArgsConstructor
public class PostController {
  private final PostService postService;

  // 게시글 생성 (인증된 사용자만 가능하다고 가정)
  @PostMapping
  public ResponseEntity<PostResponseDTO> createPost(
      @Valid @RequestBody PostCreateRequestDTO requestDto,
      @AuthenticationPrincipal UserDetails userDetails // Spring Security를 통해 현재 로그인한 사용자 정보 주입
  ) {
    // 실제 애플리케이션에서는 userDetails에서 userId를 추출하여 사용
    // 예: User user = ((CustomUserDetails) userDetails).getUser();
    // 일단은 userId를 임의로 1L로 가정하거나, Service에서 UserDetails를 받아서 처리하도록 수정
    Long userId = 1L; // TODO: 실제 로그인 사용자 ID로 대체

    PostResponseDTO responseDto = postService.createPost(requestDto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  // 특정 게시글 조회
  @GetMapping("/{postId}")
  public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long postId) {
    PostResponseDTO responseDto = postService.getPostById(postId);
    return ResponseEntity.ok(responseDto);
  }

  // 게시글 수정 (작성자만 가능하다고 가정)
  @PutMapping("/{postId}")
  public ResponseEntity<PostResponseDTO> updatePost(
      @PathVariable Long postId,
      @Valid @RequestBody PostUpdateRequestDTO requestDto,
      @AuthenticationPrincipal UserDetails userDetails // Spring Security를 통해 현재 로그인한 사용자 정보 주입
  ) {
    Long userId = 1L; // TODO: 실제 로그인 사용자 ID로 대체
    PostResponseDTO responseDto = postService.updatePost(postId, requestDto, userId);
    return ResponseEntity.ok(responseDto);
  }

  // 게시글 삭제 (작성자만 가능하다고 가정)
  @DeleteMapping("/{postId}")
  public ResponseEntity<Void> deletePost(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails // Spring Security를 통해 현재 로그인한 사용자 정보 주입
  ) {
    Long userId = 1L; // TODO: 실제 로그인 사용자 ID로 대체
    postService.deletePost(postId, userId);
    return ResponseEntity.noContent().build(); // 204 No Content
  }

  // 모든 게시글 조회
  @GetMapping
  public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
    List<PostResponseDTO> posts = postService.getAllPosts();
    return ResponseEntity.ok(posts);
  }

  // 카테고리별 게시글 조회
  @GetMapping("/category/{category}")
  public ResponseEntity<List<PostResponseDTO>> getPostsByCategory(@PathVariable PostCategory category) {
    List<PostResponseDTO> posts = postService.getPostsByCategory(category);
    return ResponseEntity.ok(posts);
  }

  // TODO: 검색, 인기 게시글 등에 대한 엔드포인트 추가
}
