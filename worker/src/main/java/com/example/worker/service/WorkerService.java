package com.example.worker.service;

import com.example.worker.entity.*;
import com.example.worker.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerService {

    private final StringRedisTemplate redisTemplate;
    private final SubmissionRepository submissionRepository;
    private final TestcaseRepository testcaseRepository;
    private final ExecutionService executionService;

    private static final String QUEUE_NAME = "submission_queue";
    private static final int WORKER_COUNT = 2;
    private static final int MAX_RETRIES = 3;  // ✅ NEW

    private final ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);
    private volatile boolean running = true;

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @PostConstruct
    public void debugRedis() {
        System.out.println("🔥 REDIS HOST = " + redisHost);
        System.out.println("🔥 REDIS PORT = " + redisPort);
    }

    // ================= START =================
    @EventListener(ApplicationReadyEvent.class)
    public void startWorkers() {
        for (int i = 0; i < WORKER_COUNT; i++) {
            executor.submit(this::processJobs);
        }
        log.info("{} workers started", WORKER_COUNT);
    }

    // ================= SHUTDOWN =================
    @PreDestroy
    public void stopWorkers() {
        log.info("Shutting down workers...");
        running = false;

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Forcing worker shutdown...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Shutdown interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ================= WORKER LOOP =================
    public void processJobs() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                log.debug("Worker polling...");

                String job = redisTemplate.opsForList()
                        .leftPop(QUEUE_NAME, 5, TimeUnit.SECONDS);

                if (job != null) {
                    log.info("Processing job {}", job);
                    processSubmission(job);
                }

            } catch (Exception e) {
                log.error("Worker loop error (retrying)", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("Worker thread exiting...");
    }

    // ================= CORE LOGIC =================
    private void processSubmission(String job) {
        Long submissionId = null;

        try {
            submissionId = parseSubmissionId(job);
            if (submissionId == null) return;

            String retryKey = "submission:retries:" + submissionId;
            String retryVal = redisTemplate.opsForValue().get(retryKey);
            int retries = retryVal != null ? Integer.parseInt(retryVal) : 0;

            if (retries >= MAX_RETRIES) {
                log.error("Submission {} exceeded max retries, dropping job", submissionId);
                markSubmissionError(submissionId,
                        new RuntimeException("Max retries exceeded"));
                redisTemplate.delete(retryKey);
                return;
            }

            Submission submission = null;

            for (int i = 0; i < 3; i++) {
                submission = submissionRepository
                        .findWithProblemById(submissionId)
                        .orElse(null);

                if (submission != null) break;

                log.warn("Submission {} not found, retrying...", submissionId);
                Thread.sleep(500);
            }

            if (submission == null) {
                throw new RuntimeException("Submission not found after retries: " + submissionId);
            }

            submission.setStatus(SubmissionStatus.RUNNING);
            submissionRepository.save(submission);

            Long problemId = submission.getProblem().getId();

            List<Testcase> testcases =
                    testcaseRepository.findByProblemIdAndHiddenFalse(problemId);

            if (testcases.isEmpty()) {
                submission.setStatus(SubmissionStatus.ERROR);
                submission.setOutput("No testcases found");
                submissionRepository.save(submission);
                return;
            }

            executionService.executeAndSaveResults(submission, testcases);

            redisTemplate.delete(retryKey);

            log.info("Completed submission {}", submissionId);

        } catch (Exception e) {
            log.error("Error processing job {} → requeueing", job, e);

            if (submissionId != null) {
                String retryKey = "submission:retries:" + submissionId;
                redisTemplate.opsForValue().increment(retryKey);
                redisTemplate.expire(retryKey, 1, TimeUnit.HOURS);
            }

            markSubmissionError(submissionId, e);

            redisTemplate.opsForList().rightPush(QUEUE_NAME, job);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Long parseSubmissionId(String job) {
        try {
            return Long.parseLong(job.trim());
        } catch (Exception e) {
            log.error("Malformed job payload: {}", job);
            return null;
        }
    }

    private void markSubmissionError(Long submissionId, Exception cause) {
        if (submissionId == null) return;

        try {
            submissionRepository.findById(submissionId).ifPresent(s -> {
                if (s.getStatus() == SubmissionStatus.RUNNING
                        || s.getStatus() == SubmissionStatus.PENDING) {
                    s.setStatus(SubmissionStatus.ERROR);
                    s.setOutput("Internal error: " + cause.getMessage());
                    submissionRepository.save(s);
                    log.warn("Marked submission {} as ERROR", submissionId);
                }
            });
        } catch (Exception e) {
            log.error("Failed to mark submission {} as ERROR", submissionId, e);
        }
    }
}