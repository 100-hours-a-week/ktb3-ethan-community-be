package org.restapi.springrestapi.benchmark;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.post.PostSummaryProjection;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
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
class PostFetchStrategyBenchmarkTest extends AbstractFetchStrategyBenchmarkTest {

    @Autowired PostRepository postRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void clearDatabase() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("게시글 작성자와 게시글이 1:1인 경우")
    void compareStrategies_oneToOne() {
        seedPosts(authorCount -> authorCount);
        runStrategies(provideStrategies()).forEach(this::printResult);
        /*
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | Lazy Loading         |       393 ms |      3100 ea |     31.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | Fetch Join           |        66 ms |       100 ea |      1.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | EntityGraph          |       108 ms |       100 ea |      1.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | DTO Projection       |        27 ms |       100 ea |      1.00 ea |
        =====================================================================
         */
    }

    @Test
    @DisplayName("1명의 사용자가 5개의 게시글을 작성할 때")
    void compareStrategies_sharedAuthor() {
        seedPosts(authorCount -> authorCount / 6);
        runStrategies(provideStrategies()).forEach(this::printResult);
        /*
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | Lazy Loading         |        58 ms |       600 ea |      6.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | Fetch Join           |        34 ms |       100 ea |      1.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | EntityGraph          |        71 ms |       100 ea |      1.00 ea |
        =====================================================================
        =====================================================================
        | Strategy Name        |   Total Time |    Total SQL |      Avg SQL |
        ---------------------------------------------------------------------
        | DTO Projection       |        13 ms |       100 ea |      1.00 ea |
        =====================================================================
         */
    }

    private void seedPosts(IntUnaryOperator authorCountSupplier) {
        List<User> authors = new ArrayList<>();
        final int totalAuthors = authorCountSupplier.applyAsInt(PAGE_SIZE);
        for (int i = 0; i < Math.max(totalAuthors, 1); i++) {
            authors.add(userRepository.save(UserFixture.uniqueUser("bench-" + i)));
        }
        for (int i = 0; i < PAGE_SIZE; ++i) {
            User author = authors.get(i % authors.size());
            Post post = Post.builder()
                .title("벤치마크 제목 " + i)
                .content("본문 " + i)
                .thumbnailImageUrl("http://thumb/" + i)
                .likeCount(i)
                .commentCount(i / 2)
                .viewCount(i * 3)
                .createdAt(LocalDateTime.now().minusDays(i))
                .updatedAt(LocalDateTime.now().minusDays(i))
                .build();
            post.changeAuthor(author);
            postRepository.save(post);
        }
        entityManager.flush();
        entityManager.clear();
    }

    private List<BenchmarkStrategy> provideStrategies() {
        return List.of(
            new BenchmarkStrategy("Lazy Loading", () -> postRepository.findSliceWithoutLazy(PageRequest.of(0, PAGE_SIZE))
                .getContent()
                .forEach(post -> post.getAuthor().getNickname())),
            new BenchmarkStrategy("Fetch Join", () -> {
                List<Post> posts = postRepository.findSliceWithFetchJoin(PageRequest.of(0, PAGE_SIZE));
                posts.forEach(post -> post.getAuthor().getNickname());
            }),
            new BenchmarkStrategy("EntityGraph", () -> postRepository.findSliceWithEntityGraph(PageRequest.of(0, PAGE_SIZE))
                .getContent()
                .forEach(post -> post.getAuthor().getNickname())),
            new BenchmarkStrategy("DTO Projection", () -> postRepository.findSliceWithProjection(PageRequest.of(0, PAGE_SIZE))
                .getContent()
                .forEach(PostSummaryProjection::postId))
        );
    }
}
