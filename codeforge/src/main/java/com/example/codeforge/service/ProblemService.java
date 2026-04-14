package com.example.codeforge.service;

import com.example.codeforge.dto.problem.ProblemRequest;
import com.example.codeforge.dto.problem.ProblemResponse;
import com.example.codeforge.entity.Problem;
import com.example.codeforge.mapper.ProblemMapper;
import com.example.codeforge.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;

    // ✅ GET ALL PROBLEMS
    public List<ProblemResponse> getAllProblems() {
        log.info("Fetching all problems");
        return problemRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(ProblemMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ GET PROBLEM BY ID
    public ProblemResponse getProblemById(Long id) {
        log.info("Fetching problem {}", id);
        Problem problem = problemRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        return ProblemMapper.toResponse(problem);
    }

    // ✅ CREATE PROBLEM (ADMIN ONLY)
    public ProblemResponse createProblem(ProblemRequest request) {
        log.info("Creating problem: {}", request.getTitle());
        
        // Idempotency: if an active problem with same title exists, return it instead of creating duplicate
        java.util.Optional<Problem> existingOpt = problemRepository.findByTitleAndIsActiveTrue(request.getTitle());
        if (existingOpt.isPresent()) {
            Problem existing = existingOpt.get();
            log.info("Problem with title '{}' already exists (id={}), skipping create", request.getTitle(), existing.getId());
            return ProblemMapper.toResponse(existing);
        }

        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .examples(request.getExamples())
                .constraints(request.getConstraints())
                .difficulty(request.getDifficulty())
                .tags(request.getTags())
                .isActive(true)
                .build();

        Problem saved = problemRepository.save(problem);
        log.info("Problem created with ID: {}", saved.getId());
        return ProblemMapper.toResponse(saved);
    }

    // ✅ UPDATE PROBLEM (ADMIN ONLY)
    public ProblemResponse updateProblem(Long id, ProblemRequest request) {
        log.info("Updating problem {}", id);
        
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setExamples(request.getExamples());
        problem.setConstraints(request.getConstraints());
        problem.setDifficulty(request.getDifficulty());
        problem.setTags(request.getTags());
        problem.setUpdatedAt(LocalDateTime.now());
        
        Problem updated = problemRepository.save(problem);
        return ProblemMapper.toResponse(updated);
    }

    // ✅ DELETE PROBLEM (ADMIN ONLY) - Soft delete
    public void deleteProblem(Long id) {
        log.info("Deleting problem {}", id);
        
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        problem.setIsActive(false);
        problem.setUpdatedAt(LocalDateTime.now());
        problemRepository.save(problem);
    }
}