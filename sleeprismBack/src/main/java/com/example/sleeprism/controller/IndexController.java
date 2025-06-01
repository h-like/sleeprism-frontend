// src/main/java/com/example/sleeprism/controller/IndexController.java
package com.example.sleeprism.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 애플리케이션의 루트 경로 요청을 처리하는 임시 컨트롤러입니다.
 * Spring Security 설정이 올바르게 적용되는지 확인하기 위해 사용됩니다.
 */
@Controller
public class IndexController {

  /**
   * "/sleeprism/" 경로 (애플리케이션의 컨텍스트 루트)로 들어오는 GET 요청을 처리합니다.
   * 이 메서드가 호출된다면 Spring Security 설정이 해당 경로에 대해 permitAll()을 제대로 적용하고 있음을 의미합니다.
   *
   * @return 테스트 메시지 문자열
   */
  @GetMapping("/sleeprism/")
  @ResponseBody // 이 메서드의 반환 값이 HTTP 응답 본문으로 직접 전송되도록 합니다.
  public String serveRoot() {
    return "<h1>Hello from Sleeprism Backend!</h1><p>이 메시지가 보인다면 Spring Security 설정은 올바르게 작동하고 있습니다.</p>";
  }

  // 만약 index.html을 컨트롤러를 통해 직접 서빙하고 싶다면 아래와 같이 사용할 수 있습니다.
  // 하지만 현재는 정적 자원 핸들러에 맡기는 것이 일반적입니다.
  // @GetMapping("/sleeprism/")
  // public String serveIndex() {
  //     return "forward:/index.html"; // src/main/resources/static/index.html 파일을 포워딩
  // }
}
