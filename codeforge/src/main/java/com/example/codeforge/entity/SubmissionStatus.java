package com.example.codeforge.entity;

public enum SubmissionStatus {
    PENDING,
    RUNNING,

    ACCEPTED,              // All testcases passed
    WRONG_ANSWER,          // Output mismatch
    TIME_LIMIT_EXCEEDED,   // Execution timeout
    RUNTIME_ERROR,         // Crash during execution
    COMPILATION_ERROR,     // Compile failed

    ERROR                 
}