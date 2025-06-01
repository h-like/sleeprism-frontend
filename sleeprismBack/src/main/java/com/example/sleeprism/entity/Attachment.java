package com.example.sleeprism.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "attachment")
public class Attachment extends BaseTimeEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attachment_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post; // 어떤 게시글에 속하는지

  @Column(nullable = false, length = 255)
  private String originalFileName; // 원본 파일명

  @Column(nullable = false, length = 255)
  private String storedFileName; // 서버에 저장된 파일명 (UUID 등)

  @Column(nullable = false, length = 255)
  private String fileUrl; // 파일에 접근할 수 있는 URL (S3, CDN 등)

  @Column(length = 50)
  private String fileType; // 파일 종류 (image/jpeg, video/mp4, application/pdf 등 MIME 타입)

  private Long fileSize; // 파일 크기 (바이트)

  // 파일 유형을 나타내는 Enum을 추가할 수도 있습니다. (예: IMAGE, VIDEO, DOCUMENT)
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private AttachmentType attachmentType; // 이미지, 동영상, 일반 파일 구분

  @Builder
  public Attachment(Post post, String originalFileName, String storedFileName, String fileUrl, String fileType, Long fileSize, AttachmentType attachmentType) {
    this.post = post;
    this.originalFileName = originalFileName;
    this.storedFileName = storedFileName;
    this.fileUrl = fileUrl;
    this.fileType = fileType;
    this.fileSize = fileSize;
    this.attachmentType = attachmentType;
  }

  // 연관관계 편의 메서드 (양방향 관계 시 필요)
  public void setPost(Post post) {
    this.post = post;
    if (!post.getAttachments().contains(this)) {
      post.getAttachments().add(this);
    }
  }
}
