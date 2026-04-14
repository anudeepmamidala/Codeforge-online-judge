package com.example.codeforge.dto.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitCodeRequest {


    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Code cannot be empty")
    private String code;

    @NotBlank(message = "Langiage is required")
    private String language; // "python", "java", "cpp"
}