package com.example.worker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "testcases")
@Getter  // ✅ Make sure this is here
@Setter  // ✅ Make sure this is here
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Testcase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private boolean hidden;  // ✅ Ensure it's primitive boolean

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}