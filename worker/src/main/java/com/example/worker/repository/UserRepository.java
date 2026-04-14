package com.example.worker.repository;

import com.example.worker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);  // ✅ ADD THIS
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);       // ✅ ADD THIS
    boolean existsByEmail(String email);
}