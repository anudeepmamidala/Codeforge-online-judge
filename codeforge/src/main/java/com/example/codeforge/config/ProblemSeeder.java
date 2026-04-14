package com.example.codeforge.config;

import com.example.codeforge.entity.Problem;
import com.example.codeforge.entity.Testcase;
import com.example.codeforge.entity.ValidationType;
import com.example.codeforge.repository.ProblemRepository;
import com.example.codeforge.repository.TestcaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ProblemSeeder {

    private final ProblemRepository problemRepository;
    private final TestcaseRepository testcaseRepository;

    @Bean
    public ApplicationRunner seedProblems() {
        return args -> {
            String title = "Two Sum";

            if (problemRepository.findByTitleAndIsActiveTrue(title).isEmpty()) {
                Problem p = Problem.builder()
                        .title(title)
                        .description("Given an array of integers and a target, return indices of the two numbers such that they add up to target. Input format: first line contains n and target, second line contains n integers separated by spaces.")
                        .examples("Input:\n4 9\n2 7 11 15\nOutput:\n0 1")
                        .constraints("1 <= n <= 10^5; -10^9 <= nums[i] <= 10^9")
                        .difficulty("EASY")
                        .tags("arrays,hashmap,two-pointers")
                        .validationType(ValidationType.EXACT_MATCH)
                        .isActive(true)
                        .build();

                Problem saved = problemRepository.save(p);
                log.info("Seeded problem '{}' with id={}", saved.getTitle(), saved.getId());

                // Public testcase 1
                Testcase t1 = Testcase.builder()
                        .problem(saved)
                        .input("4 9\n2 7 11 15")
                        .expectedOutput("0 1")
                        .hidden(false)
                        .build();

                // Public testcase 2
                Testcase t2 = Testcase.builder()
                        .problem(saved)
                        .input("3 6\n3 2 4")
                        .expectedOutput("1 2")
                        .hidden(false)
                        .build();

                testcaseRepository.save(t1);
                testcaseRepository.save(t2);

                log.info("Seeded 2 public testcases for problem '{}'", saved.getTitle());
            } else {
                log.info("Problem '{}' already exists, skipping seed", title);
            }
        };
    }
}
