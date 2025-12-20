package org.restapi.springrestapi.benchmark;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.comment.CommentSummaryProjection;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.CommentRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class CommentFetchStrategyBenchmarkTest extends AbstractFetchStrategyBenchmarkTest {

    @Autowired CommentRepository commentRepository;
    @Autowired PostRepository postRepository;
    @Autowired UserRepository userRepository;

    private Long targetPostId;

    @BeforeEach
    void setUpData() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        seedComments();
    }

    @Test
    @DisplayName("1명의 사용자가 10개의 댓글을 작성할 때")
    void compareStrategies_comments() {
        runStrategies(provideStrategies()).forEach(this::printResult);
        /*
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | Lazy Loading         |       223 ms |       400 ea |      4.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | Fetch Join           |        87 ms |       100 ea |      1.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | EntityGraph          |       135 ms |       100 ea |      1.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | DTO Projection       |        40 ms |       100 ea |      1.00 ea |
        =====================================================================
         */
    }

    private void seedComments() {
        User postAuthor = userRepository.save(UserFixture.uniqueUser("comment-author"));
        Post post = Post.builder()
                .title("벤치마크 댓글 게시글")
                .content("본문")
                .thumbnailImageUrl("http://thumb/comment")
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .build();
        post.changeAuthor(postAuthor);
        Post targetPost = postRepository.save(post);
        targetPostId = targetPost.getId();

        List<User> commenters = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            commenters.add(userRepository.save(UserFixture.uniqueUser("commenter-" + i)));
        }

        for (int c = 0; c < 10; ++c) {
            for (int userIdx = 0; userIdx < commenters.size(); ++userIdx) {
                Comment comment = Comment.builder()
                        .content("벤치마크 댓글 " + userIdx * c)
                        .build();
                comment.changePost(targetPost);
                comment.changeUser(commenters.get(userIdx));
                commentRepository.save(comment);
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    private List<BenchmarkStrategy> provideStrategies() {
        return List.of(
            new BenchmarkStrategy("Lazy Loading", () -> commentRepository.findSliceWithoutLazy(targetPostId, PageRequest.of(0, PAGE_SIZE))
                .getContent()
                .forEach(comment -> comment.getUser().getNickname())),
            new BenchmarkStrategy("Fetch Join", () -> {
                List<Comment> comments = commentRepository.findSliceWithFetchJoin(targetPostId, PageRequest.of(0, PAGE_SIZE));
                comments.forEach(comment -> comment.getUser().getNickname());
            }),
            new BenchmarkStrategy("EntityGraph", () -> commentRepository.findSliceWithEntityGraph(targetPostId, PageRequest.of(0, PAGE_SIZE))
                .getContent()
                .forEach(comment -> comment.getUser().getNickname())),
            new BenchmarkStrategy("DTO Projection", () -> commentRepository.findSliceWithProjection(targetPostId, PageRequest.of(0, PAGE_SIZE))
                .getContent()
                .forEach(CommentSummaryProjection::userNickname))
        );
    }

}
