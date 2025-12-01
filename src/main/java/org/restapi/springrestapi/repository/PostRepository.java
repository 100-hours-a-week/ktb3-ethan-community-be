package org.restapi.springrestapi.repository;

import org.restapi.springrestapi.dto.post.PostSummary;
import org.restapi.springrestapi.model.Post;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query(
        """
        select new org.restapi.springrestapi.dto.post.PostSummary (
                p.id, a.profileImageUrl, a.nickname, p.title, p.thumbnailImageUrl, p.likeCount, p.commentCount, p.viewCount, p.createdAt)
        from Post p left join p.author a
        order by p.id desc
        """
    )
    Slice<PostSummary> findSlice(Pageable pageable);

    @Query(
            """
            select new org.restapi.springrestapi.dto.post.PostSummary (
                    p.id, a.profileImageUrl, a.nickname, p.title, p.thumbnailImageUrl, p.likeCount, p.commentCount, p.viewCount, p.createdAt)
            from Post p left join p.author a
            where p.id < :cursorId
            order by p.id desc
            """
    )
    Slice<PostSummary> findSlice(@Param("cursorId") Long cursorId, Pageable pageable);

    boolean existsByIdAndAuthorId(Long id, Long authorId);

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

}
