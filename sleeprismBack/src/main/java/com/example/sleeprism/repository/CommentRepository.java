package com.example.sleeprism.repository;

import com.example.sleeprism.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findByPost_IdOrderByCreatedAtAsc(Long postId); // 특정 포스트의 댓글
  List<Comment> findByUser_Id(Long userId); // 특정 유저의 댓글
}
