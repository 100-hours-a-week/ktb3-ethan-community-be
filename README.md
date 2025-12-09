
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

1. **애플리케이션 실행**
   - IDE: `SpringRestApiApplication`
   - Gradle: `./gradlew bootRun`
   - JWT 시크릿은 운영 환경에서 외부 설정으로 교체하고, `ddl-auto: update`는 개발 전용으로 사용하세요.
2. **API 문서**
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
3. **파일 업로드 저장소**
   - 기본 경로: 프로젝트 루트 `upload/`
   - 공개 URL: `http://localhost:8080/upload`

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
- CSRF: `/auth/refresh`에만 보호 적용 (Double Submit Cookie 방식)
    - JWT + RTR(Refresh Token Rotation) 전략을 사용하므로, 엑세스 토큰 재발급 요청은 CSRF 공격에 취약할 수 있습니다.
    - 서버는 `CookieCsrfTokenRepository`로 `XSRF-TOKEN` 쿠키를 발급하고, 클라이언트는 같은 값을 `X-XSRF-TOKEN` 헤더로 전송해 Double Submit을 통과해야 `/auth/refresh` 요청이 성공합니다.
- JWT
  - Access Token: Authorization 헤더(`Bearer <token>`)
  - Refresh Token: HttpOnly + Secure 쿠키로 발급되며, RTR 전략으로 매번 갱신

## 테스트 철학 및 성능 노트

최근에는 “의미 있는 비즈니스 로직 검증”을 기준으로 테스트를 설계하며 커버리지 **93**%를 달성했습니다.  
숫자 자체보다도 리팩토링·유지보수에 대비해 신뢰할 수 있는 실행 기반을 마련하는 것이 더 중요하다는 점을 깨달았고, 앞으로도 개발 시간 이상으로 테스트에 투자할 계획입니다.

성능 측면에서는 다음 최적화를 적용 했습니다.

1. **MockMvc 직렬화에 Gson 사용 + 공통 지원 클래스 도입**  
   컨트롤러/통합 테스트가 매번 `ObjectMapper`를 띄우던 구조에서, `ControllerTestSupport`를 만들어 JWT 필터 목킹과 직렬화를 공통화하고 `Gson`을 정적 상수로 재사용하도록 바꿨습니다. Jackson 부팅과 필터 세팅 비용을 절약할 수 있었습니다.

2. **Validator 인스턴스 전역 상수화**  
   `ValidNicknameTest`, `ValidPasswordTest`, `ValidPostTitleTest` 등 Bean Validation을 사용하는 테스트는 모두 `static final Validator`를 공유하도록 변경했습니다. `Validation.buildDefaultValidatorFactory()` 호출을 매번 반복하지 않으므로 파라미터화된 테스트 실행 시간이 줄었습니다.

이와 같은 최적화를 통해 전체 테스트 시간이 평균 **4.308s → 3.469s**로 감소했으며, FIRST 원칙에 근접한 테스트 환경을 갖추는데 기여했습니다.