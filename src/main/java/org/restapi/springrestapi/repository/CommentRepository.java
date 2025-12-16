package org.restapi.springrestapi.repository;

import org.restapi.springrestapi.dto.comment.CommentSummaryProjection;
import org.restapi.springrestapi.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("""
            SELECT c
            FROM Comment c
            JOIN FETCH c.user
            WHERE c.post.id = :postId 
            ORDER BY c.id DESC
            """)
	Slice<Comment> findSlice(@Param("postId") Long postId, Pageable pageable);

	@Query("""
            SELECT c
            FROM Comment c
            JOIN FETCH c.user
            WHERE c.post.id = :postId 
            AND c.id < :cursorId
            ORDER BY c.id DESC
            """)
	Slice<Comment> findSlice(@Param("postId") Long postId, @Param("cursorId") Long cursorId, Pageable pageable);

	// N + 1이 발생하는 지연 로딩
	@Query("""
            SELECT c
            FROM Comment c
            WHERE c.post.id = :postId
            ORDER BY c.id DESC
            """)
	Slice<Comment> findSliceWithoutLazy(@Param("postId") Long postId, Pageable pageable);

	// Fetch Join으로 연관 엔티티(user) 로딩
	@Query("""
            SELECT c
            FROM Comment c
            JOIN FETCH c.user
            WHERE c.post.id = :postId
            ORDER BY c.id DESC
            """)
	List<Comment> findSliceWithFetchJoin(@Param("postId") Long postId, Pageable pageable);

	// EntityGraph로 연관 엔티티(user) 로딩
	@EntityGraph(attributePaths = "user")
	@Query("""
            SELECT c
            FROM Comment c
            WHERE c.post.id = :postId
            ORDER BY c.id DESC
            """)
	Slice<Comment> findSliceWithEntityGraph(@Param("postId") Long postId, Pageable pageable);

	// DTO Projection으로 comment, author의 필요한 컬럼만 join 조회.
	@Query("""
            SELECT new org.restapi.springrestapi.dto.comment.CommentSummaryProjection(
                c.id,
                c.content,
                c.createdAt,
                c.updatedAt,
                u.id,
                u.nickname,
                u.profileImageUrl
            )
            FROM Comment c
            JOIN c.user u
            WHERE c.post.id = :postId
            ORDER BY c.id DESC
            """)
	Slice<CommentSummaryProjection> findSliceWithProjection(@Param("postId") Long postId, Pageable pageable);



}
