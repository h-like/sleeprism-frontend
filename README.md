# 🌙 Sleeprism

> 꿈을 공유하고 해석하는 AI 기반 커뮤니티 서비스  

[배포 링크](https://h-like.github.io/sleeprism-frontend/) | [시연 영상](https://youtu.be/zpVfVakX74U) | [포트폴리오](https://example.com)

---

## 🧠 주요 기능

- ✍️ 꿈 입력 → AI 해몽
- 🃏 타로 카드 선택 기능
- 🗣 사용자 간 댓글 및 채팅
- 🔔 실시간 알림 기능

---

## 🛠 기술 스택
- Frontend: React, TypeScript
- Backend: Spring Boot, SpringSecurity, WebSocket
- Database: MySQL

---

## 꿈 해몽 flowchart

```mermaid
graph TD
    subgraph "사용자 (공통)"
        Action_A["1. 꿈 내용으로 '게시글' 작성"] --> Server_A["DB에 게시글 저장 (MySQL)"];
        Action_B["2. '꿈 해몽하기' 버튼 클릭"] --> Server_B["해몽 서비스 호출"];
        Server_F["6. 해몽 옵션과 타로카드 표시"] --> Action_C["7. 마음에 드는 해몽 최종 선택"];
        Action_C --> Server_G["DB에 선택된 해몽 정보 저장"];
    end

    subgraph "웹/앱 서버 (Sleeprism Backend)"
        Server_B --> Server_C{"기존 해몽 기록 존재?"};
        Server_C -- "Yes" --> Server_F;
        Server_C -- "No" --> Server_D["외부 Gemini API 호출"];
        Server_D --> Server_E["AI 응답과 랜덤 타로카드 매칭 및 DB 저장"];
        Server_E --> Server_F;
    end

    subgraph "외부 서비스 (Google AI)"
        Server_D -- "꿈 내용 전달" --> External_A["Gemini API (해몽 생성)"];
        External_A -- "해몽 결과(JSON) 반환" --> Server_E;
    end

    %% -- 거래 로직 구분선 --

    subgraph "사용자 (구매자)"
        Action_D["8. 다른 사람의 게시글 '구매 요청'"] --> Sale_A["판매 요청 서비스 호출"];
        Sale_E["12. 판매 수락 알림 수신"] --> Action_E["13. '내 소유 꿈' 목록에서 확인"];
    end

    subgraph "사용자 (판매자)"
        Sale_C["10. 판매 요청 알림 수신"] --> Action_F["11. 판매 요청 '수락'"];
        Action_F --> Sale_D["판매 수락 서비스 호출"];
    end

    subgraph "웹/앱 서버 (Sleeprism Backend)"
        Sale_A --> Sale_B{"요청 유효성 검증 (판매 여부, 중복 등)"};
        Sale_B -- "성공" --> Payment_A["PG사 에스크로 연동 (구현 예정)"];
        Payment_A --> Sale_C_Action["SaleRequest 생성(PENDING) 및 DB 저장"];
        Sale_C_Action --> Sale_C;

        Sale_D --> Sale_D_Action["SaleRequest 상태 변경 (ACCEPTED)"];
        Sale_D_Action --> Sale_D_Post["게시글 소유권 이전 및 판매 완료 처리"];
        Sale_D_Post --> Sale_D_Transaction["Transaction 기록 생성 (COMPLETED)"];
        Sale_D_Transaction --> Sale_E;
    end
```

# 웹소켓을 이용한 채팅
```mermaid
graph TD
    subgraph "사용자 A (발신자)"
        Action_Chat_A["/1. 채팅방 목록에서 채팅방 선택"] --> Server_Chat_A["채팅방 상세 정보 및 이전 대화 내역 조회"];
        Action_Chat_B["/3. 메시지 입력 후 '전송'"] --> Socket_A["메시지 전송 (STOMP over WebSocket)"];
        Socket_C["/7. 전송한 메시지 화면에 표시"]
    end

    subgraph "웹소켓 서버 / API (Sleeprism Backend)"
        Server_Chat_A --> Action_Chat_A;
        Socket_A --> Server_Chat_B["메시지 수신 및 처리 (ChatMessageService)"];
        Server_Chat_B --> Server_Chat_C["/4. 채팅 메시지 DB 저장 (MySQL)"];
        Server_Chat_C --> Server_Chat_D["/5. 상대방(사용자 B)에게 '알림' 생성"];
        Server_Chat_D --> Socket_B["/6. 채팅방 구독자에게 메시지 브로드캐스팅"];
        Server_Chat_D --> Push_A["(선택) 푸시 알림 발송 (FCM 등)"];
    end

    subgraph "사용자 B (수신자)"
        Socket_B --> Socket_D["/8. 새 메시지 실시간 수신 및 화면 표시"];
        Push_A --> Push_B["(앱 미실행 시) 새 메시지 푸시 알림 수신"];
    end
```
