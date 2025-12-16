
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

## 인증/보안 개요
- 세션리스(stateless) 구성: `SessionCreationPolicy.STATELESS`
- JWT
  - Access Token(AT): Authorization 헤더(`Bearer <token>`)
  - Refresh Token(RT): HttpOnly + Secure 쿠키로 발급되며, RTR 전략으로 매번 갱신
- CSRF: `/auth/refresh`에만 보호 적용 (Double Submit Cookie 방식)
  - JWT + RTR(Refresh Token Rotation) 전략을 사용하므로, AT 재발급 요청은 CSRF 공격에 취약할 수 있습니다.
  - - 공격자의 악의적 요청으로 사용자의 RT 만료에 의한 강제 로그아웃, 지속적인 리프레쉬 요청에 대한 서비스 정책으로 인한 계정 잠금 등의 피해가 발생할 수 있습니다.
  - 서버는 `CookieCsrfTokenRepository`로 `XSRF-TOKEN` 쿠키를 발급하고, 클라이언트는 같은 값을 `X-XSRF-TOKEN` 헤더로 전송해 Double Submit을 통과해야 `/auth/refresh` 요청이 성공하도록 설계 해야합니다.


![](/public/csrf-flow.png)

---

## 테스트 철학 및 성능 노트

최근에는 “의미 있는 비즈니스 로직 검증”을 기준으로 테스트를 설계하며 커버리지 **93**%를 달성했습니다.  
숫자 자체보다도 리팩토링·유지보수에 대비해 신뢰할 수 있는 실행 기반을 마련하는 것이 더 중요하다는 점을 깨달았고, 앞으로도 개발 시간 이상으로 테스트에 투자할 계획입니다.

성능 측면에서는 다음 최적화를 적용 했습니다.

1. **MockMvc 직렬화에 Gson 사용 + 공통 지원 클래스 도입**  
   컨트롤러/통합 테스트가 매번 `ObjectMapper`를 띄우던 구조에서, `ControllerTestSupport`를 만들어 JWT 필터 목킹과 직렬화를 공통화하고 `Gson`을 정적 상수로 재사용하도록 바꿨습니다. Jackson 부팅과 필터 세팅 비용을 절약할 수 있었습니다.

2. **Validator 인스턴스 전역 상수화**  
   `ValidNicknameTest`, `ValidPasswordTest`, `ValidPostTitleTest` 등 Bean Validation을 사용하는 테스트는 모두 `static final Validator`를 공유하도록 변경했습니다. `Validation.buildDefaultValidatorFactory()` 호출을 매번 반복하지 않으므로 파라미터화된 테스트 실행 시간이 줄었습니다.

| before               | after                |
|----------------------|----------------------|
| ![](/public/ori.png) | ![](/public/opt.png) |

최적화를 통해 전체 테스트 시간이 평균 **4.308s → 3.469s**로 감소했으며, FIRST 원칙에 근접한 테스트 환경을 갖추는데 기여했습니다.

---
## N + 1 문제 해결 방안 비교 및 분석

### 게시글 벤치마크(PostFetchStrategyBenchmarkTest)
본 벤치마크는 `PageRequest.of(0, PAGE_SIZE)`를 사용해 게시글 30개를 조회한 뒤, 작성자 연관 엔티티 접근 시 Hibernate Statistics를 기록합니다.
비교 대상은 Lazy Loading(기본 설정), Fetch Join, EntityGraph, DTO Projection입니다.

단일 실행에 따른 편차를 줄이기 위해, 각 조회 전략을 100회 반복 실행(REPEAT = 100)하고 그에 따른 총 실행 시간을 측정했습니다.

본 실험은 게시글 30개 조회 시 작성자 분포에 따라 N + 1 문제의 양상이 어떻게 달라지는지, 그리고 영속성 컨텍스트의 1차 캐시가 Lazy Loading에 어떤 영향을 미치는지를 확인하기 위한 목적을 가집니다.

1명의 작성자가 1개의 게시글을 작성한 경우

1명의 작성자가 5개의 게시글을 작성한 경우

|                | 작성자 1명 * 게시글 1개 | 작성자 1명 * 게시글 5개 | 평균 SQL | 비고                                                                                 |
|----------------|-----------------------|-----------------|----------|------------------------------------------------------------------------------------|
| Lazy Loading   | 393ms| 58ms| 31 → 6   | 게시글의 작성자가 동일한 경우, 영속성 컨텍스트의 1차 캐시에 이미 로드된 엔티티가 존재하여 추가 쿼리가 감소합니다. |
| Fetch Join     | 66ms| 34ms| 1        | 한 번의 JOIN으로 모든 관계 조회                                                               |
| EntityGraph    | 108ms| 71ms| 1        | JPA 그래프의 반복 호출 효과                                                                  |
| DTO Projection | 27ms| 13ms| 1        | DTO만 로드, 영속화 없이 빠름                                                                 |

Lazy Loading은 여전히 게시글 수에 비례해 추가 쿼리가 발생하는 N + 1 문제가 존재하지만, 작성자가 동일한 경우에는 영속성 컨텍스트의 1차 캐시에 의해 작성자 조회 쿼리가 재사용되어 쿼리 수가 감소하는 것을 확인할 수 있습니다.

Fetch Join과 EntityGraph는 작성자 분포와 무관하게 단일 쿼리를 유지하며, 조회 시간 감소는 작성자 수 감소로 인해 JOIN 결과 row 수와 조회 데이터 양이 줄어든 영향으로 해석됩니다.

DTO Projection은 영속성 컨텍스트와 무관하지만, 동일 작성자로 인해 DB에서 반환되는 데이터량이 감소하면서 결과적으로 조회 시간이 더 줄어드는 것으로 해석됩니다.

구체적으로 데이터량 감소는 EXPLAIN 또는 EXPLAIN ANALYZE를 자동 실행 로직을 구성해서 검증할 예정입니다.


### 댓글 벤치마크(CommentFetchStrategyBenchmarkTest)

본 벤치마크는 `PageRequest.of(0, PAGE_SIZE)`를 사용해 댓글 30개를 조회한 뒤, 작성자 연관 엔티티 접근 시 Hibernate Statistics를 기록합니다.
비교 대상은 Lazy Loading(기본 설정), Fetch Join, EntityGraph, DTO Projection입니다.

단일 실행에 따른 편차를 줄이기 위해, 각 조회 전략을 100회 반복 실행(REPEAT = 100)하고 그에 따른 총 실행 시간을 측정했습니다.

본 실험은 게시글과 댓글 조회 간 작성자 분포에 따라 N + 1 문제의 양상이 어떻게 달라지는지, 그리고 영속성 컨텍스트의 1차 캐시가 Lazy Loading에 어떤 영향을 미치는지를 확인하기 위한 목적을 가집니다.

1명의 작성자가 10개의 댓글을 작성한 경우에 대한 측정 결과입니다.

| 전략| 총 실행 시간 | 평균 SQL | 비고|
|----------------|-------|----------|---|
| Lazy Loading   | 223ms | 4.00 | 3명의 댓글러가 반복|
| Fetch Join     | 87ms | 1.00 ||
| EntityGraph    | 135ms | 1.00 ||
| DTO Projection | 40ms | 1.00 ||
