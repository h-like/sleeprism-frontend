package com.example.sleeprism.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequestDTO {
  private String email;
  private String username;
  private String nickname;
  private String password;
}
