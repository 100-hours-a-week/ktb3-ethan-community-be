# ERD

![ERD](https://github.com/100-hours-a-week/ktb3-ethan-full/blob/main/spring-rest-api/erd.png)


# SOLID 원칙 기반 리팩토링(SRP + DIP)

이 섹션은 SOLID 원칙 기반 리팩토링의 방향과 성과를 정리합니다.

- 대상: `UserServiceImpl`, `PostServiceImpl`, `CommentServiceImpl`
- 선정 이유:
  - 핵심 유스케이스(가입/프로필 수정/비밀번호 변경/게시글·댓글 CRUD 등)와 검증(존재/중복)이 서비스 내부에 혼재되어 있어 SRP 위배 소지가 있었음.
  - 서비스가 세부 구현(Repository)의 검증 메서드에 직접 의존하는 경우가 있어 DIP 관점에서 결합도가 높았음.

- 문제점:
  - 변경 이유의 증가: 검증 정책이 바뀌면 서비스 클래스를 수정해야 함.
  - 재사용성 저하: 동일 검증을 다른 서비스에서 재사용하기 어려움.

- 판단 근거:
  - 검증 로직은 재사용성이 높고, 변경 가능성이 존재하므로 별도 구성 가치가 큼.
  - SRP 준수(책임 분리), DIP 준수(추상 의존)로 구조 개선 필요.

- 리팩토링 방향(통합):
  1) 검증 인터페이스 도입: `UserValidator`, `PostValidator`, `CommentValidator`
     - UserValidator: `validateDuplicateEmail`, `validateDuplicateNickname`, `validateOnRegister`, `validatePasswordChange`
     - PostValidator: `validateAuthorId`, `validatePostExists`, `validateAuthorPermission`, `validateAuthorInfo`
     - CommentValidator: `validateUser`, `validatePost`, `validateComment`, `validateOrigin`
  2) 구현체 분리(`*ValidatorImpl`):
     - `UserValidatorImpl`: `UserFinder.existsByEmail/existsByNickName`으로 중복 검증, 비밀번호 일치 검증 수행
     - `PostValidatorImpl`: 존재 검증은 `UserFinder.existsById`, `PostFinder.existsById` 사용, 권한 검증은 `PostFinder.existsByIdAndUserId` 사용
     - `CommentValidatorImpl`: 존재 검증은 `UserFinder/PostFinder/CommentFinder`의 `existsById` 사용
  3) 서비스는 검증 추상에 의존하고, 기존 private 검증 메서드 제거:
     - `UserServiceImpl`, `PostServiceImpl`, `CommentServiceImpl`에 각 Validator 주입 후 호출 위임
  4) 존재 검증 원칙:
     - 엔티티가 바로 필요하지 않으면 `exists*`를 사용하고, Validator에서 적절한 도메인 예외를 던진다.
     - 엔티티가 즉시 필요하면 `find*`를 사용하고, Finder가 “미존재 예외”를 던지도록 위임한다.
     - 적용 상태: 현재 `UserValidatorImpl`, `PostValidatorImpl`, `CommentValidatorImpl` 모두 존재 검증에 Finder의 `exists*`를 사용하도록 통일 완료. 권한 검증은 Repository 특화 exists(`postRepository.existsByIdAndUserId`)만 예외적으로 사용.

- 변경 파일(주요):
  - User
    - `src/main/java/org/restapi/springrestapi/service/user/UserValidator.java`
    - `src/main/java/org/restapi/springrestapi/service/user/UserValidatorImpl.java`
    - `src/main/java/org/restapi/springrestapi/service/user/UserServiceImpl.java`
    - `src/main/java/org/restapi/springrestapi/finder/UserFinder.java`
    - `src/main/java/org/restapi/springrestapi/finder/UserFinderImpl.java`
  - Post
    - `src/main/java/org/restapi/springrestapi/service/post/PostValidator.java`
    - `src/main/java/org/restapi/springrestapi/service/post/PostValidatorImpl.java`
    - `src/main/java/org/restapi/springrestapi/service/post/PostServiceImpl.java`
    - `src/main/java/org/restapi/springrestapi/finder/PostFinder.java`
    - `src/main/java/org/restapi/springrestapi/finder/PostFinderImpl.java`
  - Comment
    - `src/main/java/org/restapi/springrestapi/service/comment/CommentValidator.java`
    - `src/main/java/org/restapi/springrestapi/service/comment/CommentValidatorImpl.java`
    - `src/main/java/org/restapi/springrestapi/service/comment/CommentServiceImpl.java`
    - `src/main/java/org/restapi/springrestapi/finder/CommentFinder.java`
    - `src/main/java/org/restapi/springrestapi/finder/CommentFinderImpl.java`

- 성과:
  - 책임 분리로 변경 이유가 분리됨: 검증 정책 변경 시 Validator 구현만 수정하면 됨.
  - 테스트 용이성 증대: Validator 단위 테스트로 검증 정책을 독립 확인 가능.
  - private 검증 메서드 제거: User 3개 + Post/Comment 6개 ≈ 총 9개 제거 → 클래스 복잡도/라인 수 감소
  - 의존성 방향 개선: 서비스 → 추상(Validator) 의존으로 결합도 저하
  - 재사용성 향상: 동일 검증이 필요한 다른 유스케이스에서 Validator 재사용 가능.
  - 분기/검증이 Validator로 이동하며 서비스 메서드 복잡도 완화
