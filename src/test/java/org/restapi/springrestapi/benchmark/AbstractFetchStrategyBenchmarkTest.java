package org.restapi.springrestapi.benchmark;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractFetchStrategyBenchmarkTest {

    protected static final int PAGE_SIZE = 30; // 한 번에 불러올 게시글 또는 댓글의 수
    protected static final int REPEAT = 100;   // 조회 반복 횟수

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    protected List<BenchmarkResult> runStrategies(List<BenchmarkStrategy> strategies) {
        List<BenchmarkResult> results = strategies.stream()
            .map(this::runBenchmark)
            .toList();
        results.forEach(this::printResult);
        return results;
    }

    private BenchmarkResult runBenchmark(BenchmarkStrategy strategy) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long totalElapsedNs = 0L;
        for (int i = 0; i < REPEAT; i++) {
            entityManager.clear();
            long start = System.nanoTime();
            strategy.action().run();
            totalElapsedNs += System.nanoTime() - start;
        }

        long totalElapsedMs = TimeUnit.NANOSECONDS.toMillis(totalElapsedNs);
        long totalSqlCount = statistics.getPrepareStatementCount();

        return new BenchmarkResult(
            strategy.label(),
            totalElapsedMs,
            totalSqlCount,
            totalSqlCount / (double) REPEAT
        );
    }

    private void printResult(BenchmarkResult result) {
        System.out.println("=====================================================================");
        System.out.printf("| %-20s | %12s | %12s | %12s |\n",
            "Strategy Name",
            "Total Time",
            "Total SQL",
            "Avg SQL");
        System.out.println("---------------------------------------------------------------------");

        System.out.printf("| %-20s | %9d ms | %9d ea | %9.2f ea |\n",
            result.label(),
            result.totalElapsedMs(),
            result.totalSqlCount(),
            result.averageSqlCount()
        );

        System.out.println("=====================================================================");
    }

    protected record BenchmarkResult(
        String label,
        long totalElapsedMs,
        long totalSqlCount,
        double averageSqlCount
    ) { }

    protected record BenchmarkStrategy(String label, Runnable action) { }

    protected BenchmarkResult findResultByLabel(List<BenchmarkResult> results, String label) {
        return results.stream()
            .filter(result -> result.label().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Benchmark result not found for: " + label));
    }
}
