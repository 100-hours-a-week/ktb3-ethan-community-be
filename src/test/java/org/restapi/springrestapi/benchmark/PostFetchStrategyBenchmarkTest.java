package org.restapi.springrestapi.benchmark;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class PostFetchStrategyBenchmarkTest {

    private static final int PAGE_SIZE = 100;

    @Autowired PostRepository postRepository;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void clear() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("게시글과 사용자가 1:1인 경우")
    class OneToOneAuthor {
        @BeforeEach
        void setUpData() {
            seedPosts(authorCount -> authorCount);
        }

        @Test
        @DisplayName("전략별 SQL 수 비교")
        void compareStrategies_oneToOne() {
            compareStrategies();
        }
        /*
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | 지연 로딩(N+1)           |    1135 ms |   10001 ea |
        ==============================================================
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | Fetch Join           |      82 ms |       1 ea |
        ==============================================================
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | EntityGraph          |      73 ms |       1 ea |
        ==============================================================
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | DTO Projection       |      65 ms |       1 ea |
        ==============================================================
         */
    }

    @Nested
    @DisplayName("한 사용자가 여러 게시글을 가진 경우")
    class SharedAuthor {
        @BeforeEach
        void setUpData() {
            seedPosts(authorCount -> authorCount / 5);
        }

        @Test
        @DisplayName("전략별 SQL 수 비교(작성자 재사용)")
        void compareStrategies_sharedAuthor() {
            compareStrategies();
        }
        /*
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | 지연 로딩(N+1)           |     505 ms |    2001 ea |
        ==============================================================
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | Fetch Join           |     388 ms |       1 ea |
        ==============================================================
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | EntityGraph          |     169 ms |       1 ea |
        ==============================================================
        ==============================================================
        | Strategy Name        |  Time (ms) | Query Count |
        --------------------------------------------------------------
        | DTO Projection       |     133 ms |       1 ea |
        ==============================================================
         */
    }

    private void seedPosts(IntUnaryOperator authorCountSupplier) {
        List<User> authors = new ArrayList<>();
        int totalAuthors = authorCountSupplier.applyAsInt(PAGE_SIZE);
        for (int i = 0; i < Math.max(totalAuthors, 1); i++) {
            authors.add(userRepository.save(UserFixture.uniqueUser("bench-" + i)));
        }
        for (int i = 0; i < PAGE_SIZE * 5; i++) {
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

    private void compareStrategies() {
        BenchmarkResult lazy = runBenchmark("지연 로딩(N+1)", () -> postRepository.findSliceWithoutFetch(PageRequest.of(0, PAGE_SIZE))
            .getContent()
            .forEach(post -> post.getAuthor().getNickname()));

        BenchmarkResult fetchJoin = runBenchmark("Fetch Join", () -> {
            List<Post> posts = postRepository.findSliceWithFetchJoin(PageRequest.of(0, PAGE_SIZE));
            posts.forEach(post -> post.getAuthor().getNickname());
        });

        BenchmarkResult entityGraph = runBenchmark("EntityGraph", () -> postRepository.findSliceWithEntityGraph(PageRequest.of(0, PAGE_SIZE))
            .getContent()
            .forEach(post -> post.getAuthor().getNickname()));

        BenchmarkResult projection = runBenchmark("DTO Projection", () -> postRepository.findSliceWithProjection(PageRequest.of(0, PAGE_SIZE))
            .getContent()
            .forEach(PostSummaryProjection::postId));

        assertThat(lazy.sqlCount()).isGreaterThan(fetchJoin.sqlCount());
        assertThat(lazy.sqlCount()).isGreaterThan(entityGraph.sqlCount());
        assertThat(lazy.sqlCount()).isGreaterThan(projection.sqlCount());

        printResult(lazy);
        printResult(fetchJoin);
        printResult(entityGraph);
        printResult(projection);
    }

    private BenchmarkResult runBenchmark(String label, Runnable action) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long start = System.nanoTime();
        action.run();
        long elapsedNs = System.nanoTime() - start;

        return new BenchmarkResult(
            label,
            TimeUnit.NANOSECONDS.toMillis(elapsedNs),
            statistics.getPrepareStatementCount()
        );
    }

    private void printResult(BenchmarkResult result) {
        System.out.println("==============================================================");
        System.out.printf("| %-20s | %10s | %10s |\n", "Strategy Name", "Time (ms)", "Query Count");
        System.out.println("--------------------------------------------------------------");

        System.out.printf("| %-20s | %7d ms | %7d ea |\n",
                result.label(),
                result.elapsedMs(),
                result.sqlCount()
        );

        System.out.println("==============================================================");
    }

    private record BenchmarkResult(String label, long elapsedMs, long sqlCount) {}
}
