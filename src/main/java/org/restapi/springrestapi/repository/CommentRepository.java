package org.restapi.springrestapi.repository;

import org.restapi.springrestapi.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	@Query("""
            SELECT c
            FROM Comment c
            JOIN FETCH c.user
            WHERE c.post.id = :postId
            """)
	Slice<Comment> findSlice(@Param("postId") Long postId, Pageable pageable);

	// 2. 두 번째 페이지부터 조회용 (커서 있음)
	// 보통 커서 기반 페이징은 '이전 ID보다 작은/큰' 데이터를 찾으므로 부등호 방향에 주의하세요.
	// (예: 최신순 정렬이면 c.id < :cursorId)
	@Query("""
            SELECT c
            FROM Comment c
            JOIN FETCH c.user
            WHERE c.post.id = :postId 
            AND c.id < :cursorId
            """)
	Slice<Comment> findSlice(@Param("postId") Long postId, @Param("cursorId") Long cursorId, Pageable pageable);
}
