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
@Table(name = "comments")
public class Comment extends BaseTimeEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id") // 대댓글을 위한 부모 댓글 ID
  private Comment parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> children = new ArrayList<>(); // 자식 댓글들 (대댓글)

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  // 연관관계 편의 메소드
  public void setPost(Post post) {
    this.post = post;
    if (!post.getComments().contains(this)) {
      post.getComments().add(this);
    }
  }

  public void setUser(User user) {
    this.user = user;
    if (!user.getComments().contains(this)) {
      user.getComments().add(this);
    }
  }

  public void setParent(Comment parent) {
    this.parent = parent;
    if (parent != null && !parent.getChildren().contains(this)) {
      parent.getChildren().add(this);
    }
  }

  @Builder
  public Comment(Post post, User user, String content, Comment parent) {
    this.post = post;
    this.user = user;
    this.content = content;
    this.parent = parent;
    this.isDeleted = false;
  }

  public void delete() {
    this.isDeleted = true;
  }

  public void update(String content) {
    this.content = content;
  }
}



