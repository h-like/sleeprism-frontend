package com.example.sleeprism.service;

import com.example.sleeprism.dto.PostCreateRequestDTO;
import com.example.sleeprism.dto.PostResponseDTO;
import com.example.sleeprism.dto.PostUpdateRequestDTO;
import com.example.sleeprism.entity.Post;
import com.example.sleeprism.entity.PostCategory;
import com.example.sleeprism.entity.User;
import com.example.sleeprism.repository.PostRepository;
import com.example.sleeprism.repository.UserRepository;
import com.example.sleeprism.util.HtmlSanitizer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  // 게시글 생성
  @Transactional // 쓰기 작업이므로 트랜잭션 필요
  public PostResponseDTO createPost(PostCreateRequestDTO requestDto, Long userId) {
    // User 엔티티를 찾아와서 Post와 연결
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

    // HTML Sanitization 적용
    String sanitizedContent = HtmlSanitizer.sanitize(requestDto.getContent());

    Post post = Post.builder()
        .user(user)
        .title(requestDto.getTitle())
        .content(sanitizedContent)  // 정화된 HTML 내용 저장
        .category(requestDto.getCategory())
        .build();

    Post savedPost = postRepository.save(post);
    return new PostResponseDTO(savedPost); // DTO로 변환하여 반환
  }

  // 특정 게시글 조회 (조회수 증가)
  @Transactional // 조회수 증가가 쓰기 작업이므로 트랜잭션 필요
  public PostResponseDTO getPostById(Long postId) {
    Post post = postRepository.findByIdAndIsDeletedFalse(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));
    post.incrementViewCount(); // 조회수 증가
    return new PostResponseDTO(post);
  }

  // 게시글 수정
  @Transactional
  public PostResponseDTO updatePost(Long postId, PostUpdateRequestDTO requestDto, Long userId) {
    Post post = postRepository.findByIdAndIsDeletedFalse(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

    // 작성자만 수정 가능하도록 검증
    if (!post.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("You do not have permission to update this post.");
    }

    // HTML Sanitization 적용
    String sanitizedContent = HtmlSanitizer.sanitize(requestDto.getContent());

    post.update(requestDto.getTitle(), sanitizedContent, requestDto.getCategory());
    // save를 명시적으로 호출하지 않아도 @Transactional에 의해 변경 감지(Dirty Checking) 후 업데이트됨
    return new PostResponseDTO(post);
  }

  // 게시글 삭제 (소프트 삭제)
  @Transactional
  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndIsDeletedFalse(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

    // 작성자만 삭제 가능하도록 검증
    if (!post.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("You do not have permission to delete this post.");
    }

    post.delete(); // isDeleted를 true로 변경
  }

  // 모든 게시글 조회 (삭제되지 않은 게시글만)
  public List<PostResponseDTO> getAllPosts() {
    return postRepository.findByIsDeletedFalseOrderByCreatedAtDesc() // 이 메서드는 PostRepository에 추가해야 함
        .stream()
        .map(PostResponseDTO::new)
        .collect(Collectors.toList());
  }

  // 카테고리별 게시글 조회
  public List<PostResponseDTO> getPostsByCategory(PostCategory category) {
    return postRepository.findByCategoryAndIsDeletedFalseOrderByCreatedAtDesc(category)
        .stream()
        .map(PostResponseDTO::new)
        .collect(Collectors.toList());
  }

  // TODO: 검색, 인기 게시글 등의 추가 메서드 구현

}
