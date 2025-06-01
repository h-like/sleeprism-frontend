package com.example.sleeprism.dto;

import com.example.sleeprism.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostCreateRequestDTO {
  @NotBlank(message = "Title cannot be blank")
  @Size(max = 255, message = "Title cannot exceed 255 characters")
  private String title;

  @NotBlank(message = "Content cannot be blank")
  private String content;

  @NotNull(message = "Category cannot be null")
  private PostCategory category;
}
