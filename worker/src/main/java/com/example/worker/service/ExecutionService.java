package com.example.worker.service;

import com.example.worker.entity.*;
import com.example.worker.repository.SubmissionRepository;
import com.example.worker.repository.SubmissionResultRepository;
import com.example.worker.utils.CodeExecutionUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExecutionService {


    @Value("${HOST_SHARED_PATH}")
    private String hostPath;

    @Value("${CONTAINER_SHARED_PATH:/shared}")
    private String containerPath;

    private final SubmissionResultRepository submissionResultRepository;
    private final SubmissionRepository submissionRepository;

    public void executeAndSaveResults(
            Submission submission, List<Testcase> testcases) {

                log.info("HOST PATH: {}", hostPath);

        boolean hasError = false;
        boolean hasTimeout = false;
        boolean allPassed = true;
        int passedCount = 0;

        log.info("Starting execution for submission {} with {} testcases",
                submission.getId(), testcases.size());

        for (Testcase tc : testcases) {

            long start = System.currentTimeMillis();

            log.debug("Executing testcase {} for submission {}",
                    tc.getId(), submission.getId());

            // 🔥 MULTI-LANGUAGE EXECUTION
            String language = submission.getLanguage() != null
                    ? submission.getLanguage().name()
                    : "PYTHON";

        CodeExecutionUtil.ExecutionResult result =
            CodeExecutionUtil.execute(
                submission.getCode(),
                tc.getInput(),
                tc.getExpectedOutput(),
                language,           
                submission.getProblem().getValidationType() != null
            ? submission.getProblem().getValidationType().name()
            : "EXACT_MATCH"
            );

                    log.error("STDERR: {}", result.stderr());
log.info("STDOUT: {}", result.stdout());

            long execTime = System.currentTimeMillis() - start;

            log.debug("Testcase {} result: verdict={}, passed={}, time={}ms",
                    tc.getId(), result.verdict(), result.passed(), execTime);

            if ("ERROR".equals(result.verdict())) {
                hasError = true;
                allPassed = false;
            } else if ("TIMEOUT".equals(result.verdict())) {
                hasTimeout = true;
                allPassed = false;
            } else if (!result.passed()) {
                allPassed = false;
            } else {
                passedCount++;
            }

            SubmissionResult submissionResult = SubmissionResult.builder()
                    .submission(submission)
                    .testcase(tc)
                    .passed(result.passed())
                    .output(result.stdout())
                    .error(result.stderr())
                    .executionTime((int) execTime)
                    .build();

            submissionResultRepository.save(submissionResult);
        }

        if (hasError) {
            submission.setStatus(SubmissionStatus.ERROR);
            submission.setOutput("Runtime Error");
        } else if (hasTimeout) {
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setOutput("Time Limit Exceeded");
        } else if (allPassed) {
            submission.setStatus(SubmissionStatus.PASSED);
            submission.setOutput("Accepted");
        } else {
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setOutput(
                    passedCount + "/" + testcases.size() + " testcases passed"
            );
        }

        submissionRepository.save(submission);

        log.info("Finished submission {} → status: {}",
                submission.getId(), submission.getStatus());
    }
}