package com.example.worker.repository;

import com.example.worker.entity.Submission;
import com.example.worker.entity.SubmissionStatus;

import org.springframework.data.repository.query.Param; // ✅ CORRECT

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Lists
    List<Submission> findByUserId(Long userId);
    List<Submission> findByUserUsername(String username);

    Optional<Submission> findByIdAndUserId(Long submissionId, Long userId);

    // Counts (USE LONG ONLY)
    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, SubmissionStatus status);


    @Query("SELECT s FROM Submission s JOIN FETCH s.problem WHERE s.id = :id")
    Optional<Submission> findWithProblemById(@Param("id") Long id);
}
