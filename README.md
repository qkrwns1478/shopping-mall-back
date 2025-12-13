# MUNSIKSA Shopping Mall Service

## Introduction
**MUNSIKSA**는 안정적인 이커머스 비즈니스를 위한 Spring Boot 기반의 백엔드 서비스입니다.
단순한 쇼핑몰 기능을 넘어, 결제 무결성 검증, 관리자 보안 강화, 그리고 LLM 기반의 운영 자동화 시스템(MCP)을 도입하여 기술적 차별성을 확보했습니다.

## Key Problem Solving

<details>
<summary>접기/펼치기</summary>

### **쇼핑몰 운영 효율화를 위한 Spring Boot 기반 MCP AI 에이전트 서버 구축**

**1. 문제**

- **반복적인 관리 업무의 비효율성:** 회원 적립금 관리, 재고 파악, 메인 진열 변경 등 쇼핑몰 운영에 필수적인 단순 반복 업무가 관리자 페이지(UI)에 의존적으로 처리되어 운영 효율이 저하되었습니다.
- **레거시 시스템과 AI 연동의 복잡성:** 기존 구축된 Spring Boot 백엔드 로직을 수정하지 않고, 최신 LLM(Gemini, Claude 등)이 데이터베이스 실시간 현황을 조회하거나 제어하게 만드는 아키텍처 설계가 어려웠습니다.
- **보안 및 인증 처리의 난해함:** AI 에이전트가 관리자 권한으로 API를 호출해야 하지만, 민감한 인증 정보(세션, 쿠키 등)를 안전하게 관리하면서 외부 AI 클라이언트와 통신하는 표준 프로토콜이 부재했습니다.

**2. 해결**

- **Sidecar 패턴의 Node.js MCP 서버 도입:** 기존 Spring Boot 서버를 전혀 수정하지 않고, 옆에서 독립적으로 동작하며 AI 요청을 REST API 호출로 변환해주는 'MCP(Model Context Protocol) 어댑터 서버'를 구현했습니다.
- **핵심 운영 기능의 Tool화:** 회원 관리, 상품 관리, 카테고리 및 메인 진열 관리 등 핵심 비즈니스 로직을 AI가 이해할 수 있는 함수형 도구(Tool)로 정의하고 Zod 스키마로 표준화했습니다.
- **자동화된 관리자 인증 체계 구축:** 환경 변수(`.env`)를 통해 관리자 계정 정보를 안전하게 주입하고, MCP 서버 구동 시 자동으로 Spring Boot 백엔드에 로그인하여 세션 쿠키(JSESSIONID)를 획득 및 유지하는 로직을 구현했습니다.

**3. 결과**

- **자연어 기반 운영 자동화 실현:** "재고가 부족한 상품을 찾아 메인 진열에서 내려줘"와 같은 복합적인 명령을 AI가 스스로 판단하여 순차적 API 호출(조회 → 판단 → 삭제)로 수행하는 지능형 운영 환경을 구축했습니다.
- **시스템 확장성 및 유지보수성 확보:** 백엔드(Spring Boot)와 AI 인터페이스(MCP Server)를 분리함으로써, 향후 AI 모델이 변경되거나 백엔드 로직이 바뀌어도 상호 영향 없이 유연한 확장이 가능해졌습니다.
- **개발 생산성 향상:** Gemini CLI 등 표준 MCP 클라이언트를 통해 데이터베이스 상태를 즉시 점검하고 더미 데이터를 생성하는 등, 개발 및 테스트 단계에서의 생산성을 비약적으로 높였습니다.

### **결제 프로세스 무결성 확보 및 비로그인 주문 흐름 최적화**

**1. 문제**

- **결제 위변조 위험 및 0원 결제 오류:** 클라이언트 데이터 신뢰 시 금액 변조 위험이 있었으며, 전액 포인트 사용으로 최종 금액이 0원이 될 경우 PG사 모듈에서 승인 거부 오류가 발생했습니다.
- **비로그인 주문 시 데이터 불일치:** 비로그인 상태의 임시 장바구니 ID와 로그인 후 DB ID의 불일치로 인해, '바로 구매' 시도시 401(Unauthorized) 및 잘못된 접근 오류가 발생했습니다.
- **API Null Safety 미흡:** `Map.of` 메서드 사용 시 Value가 `null`인 경우(예: 주소 미입력) 500 내부 서버 에러가 발생하여 서비스 안정성이 저하되었습니다.

**2. 해결**

- **서버 측 교차 검증 및 예외 처리:** 포트원 V2 API를 통해 실제 결제 내역과 DB 주문 금액을 대조하여 무결성을 검증하고, 0원 결제 시 PG 로직을 생략하는 조건부 분기 처리를 구현했습니다.
- **인증 기반 주문 로직 재설계:** '바로 구매' 클릭 시 로그인을 선행시키고, `addCart` 수행 후 생성된 유효한 DB 장바구니 ID(`cartItemId`)를 프론트엔드에 반환하여 주문 페이지로 리다이렉트했습니다.
- **응답 객체 안정화:** 응답 생성 로직을 `HashMap`으로 변경하고 삼항 연산자를 적용하여, `null` 데이터가 존재하더라도 빈 문자열로 치환해 안정적인 JSON 응답을 보장했습니다.

**3. 결과**

- **결제 보안 강화:** 결제 금액 위변조 시도를 원천 차단하고, 복합 결제(포인트+카드) 및 전액 포인트 결제 시나리오를 에러 없이 완벽하게 지원하게 되었습니다.
- **UX 및 전환율 개선:** 비로그인 사용자도 끊김 없이 로그인 후 즉시 주문 단계로 이어지는 매끄러운 경험을 제공하여 이탈률 감소 및 주문 성공률을 향상시켰습니다.
- **시스템 견고성 확보:** 필수 정보가 누락된 회원 데이터 조회 시에도 서버가 중단되지 않는 방어 코드를 구축하여 프론트엔드 렌더링 오류를 방지했습니다.

### **관리자 보안 기능 강화 및 서버 장애 대응 로직 구현**

**1. 문제**

- **권한 관리의 취약점:** 관리자 대시보드에서 회원을 조회하거나 강제 탈퇴시키는 기능이 부재했고, 특히 일반 회원을 관리자로 승격시키는 민감한 작업에 대한 보안 장치가 없었습니다.
- **미들웨어 설정 오류:** Next.js 미들웨어 설정 미흡으로 인해 권한 없는 사용자가 관리자 페이지에 접근하는 보안 취약점이 발견되었습니다.
- **장애 상황 시 Fail-Open 문제:** 백엔드 서버 다운 시 프론트엔드에서 이를 인지하지 못해 무한 로딩이 발생하거나, 예외 처리 미비로 인해 보안 페이지 접근이 허용되는 심각한 문제가 있었습니다.

**2. 해결**

- **백엔드 2차 인증 구현:** 관리자 전용 API를 신설하고, 권한 변경 요청 시 관리자 이메일로 인증 코드를 발송하고 검증하는 2차 인증 로직을 `MemberService`와 `EmailService`에 구현했습니다.
- **Fail-Closed 정책 적용:** 백엔드 헬스 체크(`/members/info`)를 통해 서버 상태를 감지하고, 연결 실패 시에는 즉시 메인 페이지로 리다이렉트하여 비인가 접근을 원천 차단했습니다.
- **프론트엔드 보안 강화:** `middleware.ts`를 표준 규격에 맞춰 재설정하여 모든 관리자 경로에 대한 접근 제어를 강화했습니다.

**3. 결과**

- **보안 체계 구축:** 관리자 승격 시 이메일 인증을 강제함으로써, 실수나 악의적인 권한 탈취를 방지하는 강력한 보안 체계를 구축했습니다.
- **시스템 신뢰성 확보:** 서버 점검이나 장애 상황에서도 사용자에게 명확한 피드백을 제공하고, 보안 사고를 예방하여 시스템 안전성을 확보했습니다.
- **관리자 기능 완성:** 회원 목록 조회, 삭제, 권한 변경 등 관리자 핵심 기능을 완성하여 쇼핑몰 운영을 위한 기반 시스템을 성공적으로 런칭했습니다.

</details>

## Tech Stacks

### Application
![Java](https://img.shields.io/badge/Java-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=spring-boot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)
![Hibernate](https://img.shields.io/badge/JPA%20Hibernate-59666C?logo=hibernate&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=spring&logoColor=white)

### MCP Server
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-339933?logo=node.js&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?logo=axios&logoColor=white)

## Environment Vairables `.env`
```
DB_USERNAME=
DB_PASSWORD=

EMAIL_USERNAME=
EMAIL_APP_PASSWORD=

JWT_SECRET=

ITEM_IMG_PATH=
UPLOAD_PATH=

ADMIN_DEFAULT_EMAIL=
ADMIN_DEFAULT_PASSWORD=

PORTONE_API_KEY=
```

## Features
- **Member**: 회원가입(이메일 인증), 로그인/로그아웃, 마이페이지, 권한 관리
- **Item**: 상품 CRUD, 카테고리 관리, 다중 이미지 업로드, 재고 관리
- **Order & Pay**: 장바구니, 결제(PortOne), 주문 내역 조회, 주문 취소
- **Coupon**: 쿠폰 생성, 사용자 발급, 관리자 일괄 지급(Bulk Issue)
- **AI Operations**: Gemini 기반 자연어 운영 관리 (MCP Server 연동)

## API Documentation
Swagger UI를 통해 API 명세를 확인할 수 있습니다.
- URL: `http://localhost:8080/swagger-ui/index.html`