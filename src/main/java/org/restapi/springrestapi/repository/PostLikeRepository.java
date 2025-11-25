package org.restapi.springrestapi.repository;

import org.restapi.springrestapi.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    PostLike findByUserIdAndPostId(Long userId, Long postId);
}
