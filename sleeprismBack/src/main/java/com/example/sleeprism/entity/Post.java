package com.example.sleeprism.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "post")
public class Post extends BaseTimeEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) // N:1 관계, 지연 로딩이 기본
  @JoinColumn(name = "user_id", nullable = false) // FK 컬럼 지정
  private User user;

  @Column(nullable = false, length = 255)
  private String title;

  @Lob // 대용량 텍스트를 위한 어노테이션
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private PostCategory category;

  @Column(name = "view_count", nullable = false)
  private int viewCount = 0;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostLike> likes = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Bookmark> bookmarks = new ArrayList<>();


  // 연관관계 편의 메소드
  public void setUser(User user) {
    this.user = user;
    if (!user.getPosts().contains(this)) { // 무한루프 방지
      user.getPosts().add(this);
    }
  }

  @Builder
  public Post(User user, String title, String content, PostCategory category) {
    this.user = user;
    this.title = title;
    this.content = content;
    this.category = category;
    this.viewCount = 0;
    this.isDeleted = false;
  }

  // 조회수 증가 메소드
  public void incrementViewCount() {
    this.viewCount++;
  }

  // 삭제 처리 (Soft Delete)
  public void delete() {
    this.isDeleted = true;
  }

  // 수정 메소드
  public void update(String title, String content, PostCategory category) {
    this.title = title;
    this.content = content;
    this.category = category;
  }

  // Attchment 와 연결. 첨부 파일들 연결
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attachment> attachments = new ArrayList<>();

  // 연관관계 편의 메소드
  public void addAttachment(Attachment attachment) {
    this.attachments.add(attachment);
    if (attachment.getPost() != this) {
      attachment.setPost(this);
    }
  }
}




