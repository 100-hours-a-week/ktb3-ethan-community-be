package org.restapi.springrestapi.repository;

import org.restapi.springrestapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByIdAndDeletedAtIsNull(Long id);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

}
