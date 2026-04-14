package com.example.codeforge.service;

import com.example.codeforge.dto.submission.*;
import com.example.codeforge.entity.*;
import com.example.codeforge.mapper.SubmissionMapper;
import com.example.codeforge.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubmissionService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final TestcaseRepository testcaseRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionResultRepository submissionResultRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String QUEUE_NAME = "submission_queue";

    public SubmissionResponse submit(String username, SubmitCodeRequest request) {

        // 🔹 Fetch user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔹 Fetch problem
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        // 🔥 SAFE language parsing
        Language language;
        try {
            language = Language.valueOf(request.getLanguage().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid language");
        }

        // 🔹 Create submission (PENDING)
        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .code(request.getCode())
                .language(language)
                .status(SubmissionStatus.PENDING)
                .output("Queued for execution")
                .build();

        submission = submissionRepository.save(submission);

        // 🔹 Validate testcases exist (early check)
        List<Testcase> testcases =
                testcaseRepository.findByProblemIdAndHiddenFalse(problem.getId());

        if (testcases.isEmpty()) {
            submission.setStatus(SubmissionStatus.ERROR);
            submission.setOutput("No testcases configured");
            submissionRepository.save(submission);
            return SubmissionMapper.toResponse(submission);
        }

        // 🔥 Push to Redis queue
        redisTemplate.opsForList().rightPush(
                QUEUE_NAME,
                submission.getId().toString()
        );

        log.info("Submission {} queued for execution", submission.getId());

        // 🔹 Return immediately
        return SubmissionMapper.toResponse(submission);
    }

    public List<SubmissionResponse> getSubmissionsByProblem(Long problemId) {
        return submissionRepository.findByProblem_Id(problemId)
            .stream()
            .map(SubmissionMapper::toResponse)
            .toList();
        }

    public SubmissionDetailResponse getSubmissionDetails(
            String username, Long submissionId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Submission submission =
                submissionRepository.findByIdAndUserId(submissionId, user.getId())
                        .orElseThrow(() -> new RuntimeException("Submission not found"));

        List<SubmissionResult> results =
                submissionResultRepository.findBySubmissionId(submissionId);

        return SubmissionMapper.toDetailResponse(submission, results);
    }

    public List<SubmissionResponse> getUserSubmissions(String username) {
        return submissionRepository.findByUserUsername(username)
                .stream()
                .map(SubmissionMapper::toResponse)
                .toList();
    }
}