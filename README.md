
# '하루조각 커뮤니티' Backend

- 조각처럼 작은 하루의 기억들을 공유하는 커뮤니티 서비스의 백엔드 API 서버입니다.
- Spring Boot 기반의 인증/인가(JWT), 게시글/댓글, 파일 업로드를 제공합니다.

---

## 주요 기능
- 인증/회원
    - 회원가입, 로그인, 로그아웃, 액세스 토큰 재발급 (JWT + Refresh Token Cookie)
    - 사용자 프로필 조회/수정, 비밀번호 변경, 회원 탈퇴
- 게시글
    - 등록/수정/삭제, 상세/목록 조회(커서 기반 페이지네이션), 좋아요 토글
- 댓글
    - 등록/수정/삭제, 목록 조회(커서 기반)
- 업로드
    - 프로필 이미지, 게시글 대표 이미지 업로드 (로컬 스토리지 저장)
- 공통
    - Swagger UI(v3) 문서화, 예외 포맷 통일(APIResponse), 헬스체크(`/hc`) 및 csrf 토큰 발급(`/csrf`)

---

## Stack
- Language: Java 17
- Framework: Spring Boot 3.5.6 (Web, Security, Data JPA)
- DB: MySQL 8.x (JPA/Hibernate)
- Security: Spring Security, JWT (io.jsonwebtoken:jjwt 0.12.x)
- Docs: springdoc-openapi-starter-webmvc-ui 2.8.x
- Build: Gradle

---

### 프로젝트 실행 및 주요 경로

1) 애플리케이션 실행
- IDE: `SpringRestApiApplication` 실행
- Gradle: `./gradlew bootRun`


- 주의: JWT 시크릿은 실제 배포 시 환경변수/외부 설정으로 교체하세요.
- `ddl-auto: update`는 개발 편의용입니다(운영 비권장).

<br>


2) API 문서 
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

<br>

3) 파일 업로드 저장소
- 기본 경로: 프로젝트 루트의 `upload/`
- 공개 URL 기본값: `http://localhost:8080/upload`

---

### 폴더 구조
- `src/main/java/org/restapi/springrestapi`
    - `controller`: Auth/User/Post/Comment/Upload REST 컨트롤러
    - `service`: 도메인 서비스 구현(비즈니스 로직)
    - `repository`: Spring Data JPA 저장소
    - `model`: JPA 엔티티(User, Post, Comment, PostLike)
    - `dto`: 요청/응답 DTO 및 결과 모델
    - `security`: SecurityConfig, JWT 필터/프로바이더, 핸들러
    - `config`: Swagger, Web/CORS, 업로드 리소스 매핑
    - `common`: APIResponse, 파일 저장 유틸 등
    - `validator`: 요청 유효성 커스텀 어노테이션 및 구현
- `src/main/resources/application.yml`: 환경 설정
- `erd.png`: ERD 이미지

---

### 인증/보안 개요
- 세션리스(stateless) 구성: `SessionCreationPolicy.STATELESS`
- CSRF: 활성화 + `CookieCsrfTokenRepository` (HttpOnly=false, Secure=true)
    - 예외 경로: `/auth/login`, `/auth/signup`
    - 프론트엔드에서 CSRF 토큰을 헤더로 전송해야 함
    - 필요시 `/csrf`로 CSRF 토큰 요청 가능
- CORS: `CorsConfig`를 통해 허용 도메인/메서드/헤더 설정
- JWT
  - 토큰이 존재할 경우 유효해야하는 사용자로 판별, 인증 수행
  - Access Token: Authorization 헤더(`Bearer <token>`)로 전송
  - Refresh Token: 서버가 `HttpOnly, Secure` 쿠키로 발급/갱신

---

## 트러블슈팅 
