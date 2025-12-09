package org.restapi.springrestapi.finder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.exception.code.UserErrorCode;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserFinderTest {

    @InjectMocks
    UserFinder userFinder;

    @Mock
    UserRepository userRepository;

    @Test
    @DisplayName("식별자로 프록시를 조회하면 UserRepository에 위임한다")
    void findProxyById_delegatesToRepository() {
        User proxy = UserFixture.persistedUser().toBuilder().id(3L).build();
        given(userRepository.getReferenceById(3L)).willReturn(proxy);

        User result = userFinder.findProxyById(3L);

        assertThat(result).isSameAs(proxy);
        verify(userRepository).getReferenceById(3L);
    }

    @Test
    @DisplayName("findByIdOrNull은 존재하면 엔티티를 반환한다")
    void findByIdOrNull_returnsEntityWhenPresent() {
        User entity = UserFixture.persistedUser().toBuilder().id(7L).build();
        given(userRepository.findById(7L)).willReturn(Optional.of(entity));

        User result = userFinder.findByIdOrNull(7L);

        assertThat(result).isSameAs(entity);
        verify(userRepository).findById(7L);
    }

    @Test
    @DisplayName("findByIdOrNull은 존재하지 않으면 null을 반환한다")
    void findByIdOrNull_returnsNullWhenMissing() {
        given(userRepository.findById(11L)).willReturn(Optional.empty());

        User result = userFinder.findByIdOrNull(11L);

        assertThat(result).isNull();
        verify(userRepository).findById(11L);
    }

    @Test
    @DisplayName("findByIdOrThrow는 USER_NOT_FOUND 예외를 던진다")
    void findByIdOrThrow_throwsUserNotFound() {
        given(userRepository.findById(20L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userFinder.findByIdOrThrow(20L))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("findByIdOrAuthThrow는 사용자 미존재 시 UNAUTHORIZED 예외를 던진다")
    void findByIdOrAuthThrow_throwsUnauthorizedWhenMissing() {
        given(userRepository.findById(30L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userFinder.findByIdOrAuthThrow(30L))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(AuthErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("existsByIdOrThrow는 존재하지 않으면 USER_NOT_FOUND 예외를 던진다")
    void existsByIdOrThrow_throwsWhenMissing() {
        given(userRepository.existsById(55L)).willReturn(false);

        assertThatThrownBy(() -> userFinder.existsByIdOrThrow(55L))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("이메일로 조회 시 존재하면 Auth 예외 없이 반환한다")
    void findByEmailOrAuthThrow_returnsUserWhenPresent() {
        User entity = UserFixture.persistedUser().toBuilder().email("user@test.com").build();
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(entity));

        User result = userFinder.findByEmailOrAuthThrow("user@test.com");

        assertThat(result).isSameAs(entity);
        verify(userRepository).findByEmail("user@test.com");
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 INVALID_EMAIL_OR_PASSWORD 예외를 던진다")
    void findByEmailOrAuthThrow_throwsWhenEmailMissing() {
        given(userRepository.findByEmail("missing@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userFinder.findByEmailOrAuthThrow("missing@test.com"))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD));
    }
}
