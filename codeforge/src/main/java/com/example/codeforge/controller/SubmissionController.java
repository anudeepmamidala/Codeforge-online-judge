        package com.example.codeforge.controller;

        import com.example.codeforge.dto.submission.SubmissionDetailResponse;
        import com.example.codeforge.dto.submission.SubmissionResponse;
        import com.example.codeforge.dto.submission.SubmitCodeRequest;
        import com.example.codeforge.service.SubmissionService;
        import com.example.codeforge.utils.ApiResponse;
        import lombok.RequiredArgsConstructor;
        import lombok.extern.slf4j.Slf4j;

        import java.util.List;

        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.security.access.prepost.PreAuthorize;
        import org.springframework.security.core.Authentication;
        import org.springframework.web.bind.annotation.*;
        import jakarta.validation.Valid;


        @RestController
        @RequestMapping("/api/submissions")
        @RequiredArgsConstructor
        @Slf4j
        public class SubmissionController {

        private final SubmissionService submissionService;

        // ✅ SUBMIT CODE
        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<SubmissionResponse>> submitCode(
                @Valid @RequestBody SubmitCodeRequest request,
                Authentication authentication) {
                
                log.info("User {} submitting code for problem {}", 
                        authentication.getName(), request.getProblemId());
                
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(
                                submissionService.submit(
                                        authentication.getName(),
                                        request
                                ),
                                "Code submitted successfully"
                        ));
        }

        // ✅ GET SUBMISSION DETAILS (with all testcase results)
        @GetMapping("/{submissionId}")
        @PreAuthorize("isAuthenticated()")
        public ApiResponse<SubmissionDetailResponse> getSubmissionDetails(
                @PathVariable Long submissionId,
                Authentication authentication) {
                
                log.info("User {} fetching submission details: {}", 
                        authentication.getName(), submissionId);
                
                return ApiResponse.success(
                        submissionService.getSubmissionDetails(
                                authentication.getName(),
                                submissionId
                        ),
                        "Submission details fetched"
                );
        }

        @GetMapping("/problem/{problemId}")
        @PreAuthorize("isAuthenticated()")
        public ApiResponse<List<SubmissionResponse>> getProblemSubmissions(
                @PathVariable Long problemId) {

                return ApiResponse.success(
                        submissionService.getSubmissionsByProblem(problemId),
                "Problem submissions fetched"
                );
        }
        @GetMapping("/my")
        @PreAuthorize("isAuthenticated()")
        public ApiResponse<List<SubmissionResponse>> getMySubmissions(
                Authentication authentication) {

        return ApiResponse.success(
                submissionService.getUserSubmissions(authentication.getName()),
                "User submissions fetched"
        );
        }

        }