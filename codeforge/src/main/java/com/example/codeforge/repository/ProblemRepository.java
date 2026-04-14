package com.example.codeforge.repository;

import com.example.codeforge.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    
    List<Problem> findByDifficultyAndIsActiveTrue(String difficulty);
    
    List<Problem> findByIsActiveTrueOrderByCreatedAtDesc();
    
    List<Problem> findByTagsContainingAndIsActiveTrue(String tag);
    
    Optional<Problem> findByIdAndIsActiveTrue(Long id);
    
    Optional<Problem> findByTitleAndIsActiveTrue(String title);
}