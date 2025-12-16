package org.restapi.springrestapi.repository;

import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.PostSummaryProjection;
import org.restapi.springrestapi.model.Post;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query("""
        SELECT p 
        FROM Post p 
        JOIN FETCH p.author
        ORDER BY p.id DESC
    """)
	Slice<PostResult> findSlice(Pageable pageable);

	@Query("""
        SELECT p 
        FROM Post p 
        JOIN FETCH p.author 
        WHERE p.id < :cursorId 
        ORDER BY p.id DESC
    """)
	Slice<PostResult> findSlice(@Param("cursorId") Long cursorId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.commentCount = p.commentCount + 1 where p.id = :id")
    void increaseCommentCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.commentCount = p.commentCount - 1 where p.id = :id")
    void decreaseCommentCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.likeCount = p.likeCount + 1 where p.id = :id")
    void increaseLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.likeCount = p.likeCount - 1 where p.id = :id")
    void decreaseLikeCount(@Param("id") Long id);

    @Query("select p.likeCount from Post p where p.id = :id")
    Optional<Integer> findLikeCountById(@Param("id") Long id);

    /*
        PostFetchStrategyBenchmarkTest 에서 N + 1 문제 해결 방안 비교 및 분석을 위해 작성됨
    */

    // N + 1이 발생하는 지연 로딩
    @Query("""
        SELECT p
        FROM Post p
        ORDER BY p.id DESC
    """)
    Slice<Post> findSliceWithoutLazy(Pageable pageable);

    // Fetch Join으로 연관 엔티티(author) 로딩
    @Query("""
        SELECT p
        FROM Post p
        JOIN FETCH p.author
        ORDER BY p.id DESC
    """)
    List<Post> findSliceWithFetchJoin(Pageable pageable);

    // EntityGraph로 연관 엔티티(author) 로딩
    @EntityGraph(attributePaths = "author")
    @Query("""
        SELECT p
        FROM Post p
        ORDER BY p.id DESC
    """)
    Slice<Post> findSliceWithEntityGraph(Pageable pageable);

    // DTO Projection으로 post, author의 필요한 컬럼만 join 조회.
    @Query("""
        SELECT new org.restapi.springrestapi.dto.post.PostSummaryProjection(
            p.id,
            p.title,
            p.content,
            p.thumbnailImageUrl,
            p.likeCount,
            p.commentCount,
            p.viewCount,
            p.createdAt,
            a.id,
            a.nickname,
            a.profileImageUrl
        )
        FROM Post p
        JOIN p.author a
        ORDER BY p.id DESC
    """)
    Slice<PostSummaryProjection> findSliceWithProjection(Pageable pageable);
}
