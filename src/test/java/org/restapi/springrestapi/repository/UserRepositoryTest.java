package org.restapi.springrestapi.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.model.UserTest;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("User 저장 시 joinAt이 자동으로 세팅된다")
    void save_should_set_joinAt_via_prePersist() {
        // given
        User user = UserFixture.persistedUser();

        // when
        User saved = userRepository.save(user);

        // then
        assertThat(saved.getJoinAt()).isNotNull();
    }


    @Test
    @DisplayName("delete() 호출 시 DB row는 남아 있고 deleted_at만 설정된다 (soft delete)")
    void soft_delete_veri() {
        // given
        User saved = userRepository.save(UserFixture.persistedUser());
        Long userId = saved.getId();

        // when
        LocalDateTime beforeDeleteUser = LocalDateTime.now();
        userRepository.delete(saved);
        userRepository.flush();
        entityManager.clear();

        // then
        assertThat(userRepository.findById(userId)).isEmpty();

        // 운영 서비스에는 관련 기능이 없으므로 테스트에서 직접 쿼리 생성
        User deletedUser = (User) entityManager
                .createNativeQuery("select * from users where id = :id", User.class)
                .setParameter("id", userId)
                .getSingleResult();

        assertThat(deletedUser).isNotNull();
        assertThat(deletedUser.getDeletedAt()).isAfter(beforeDeleteUser);
    }
}
