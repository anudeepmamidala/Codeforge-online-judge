package com.example.codeforge.service;

import com.example.codeforge.dto.auth.AuthResponse;
import com.example.codeforge.dto.auth.LoginRequest;
import com.example.codeforge.dto.auth.RegisterRequest;
import com.example.codeforge.dto.auth.UserMeResponse;
import com.example.codeforge.entity.User;
import com.example.codeforge.repository.UserRepository;
import com.example.codeforge.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ✅ REGISTER
    public AuthResponse register(RegisterRequest request) {
        
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user with ROLE_USER
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")  // Default role
                .build();

        user = userRepository.save(user);  // ✅ FIX: Reassign the result
        
        log.info("User registered: {}", user.getUsername());

        // Generate token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    // ✅ LOGIN
    public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );

    log.info("User authenticated: {}", request.getUsername());

    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

    String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
    return new AuthResponse(token, user.getUsername(), user.getRole());
}

    public UserMeResponse getCurrentUser(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return new UserMeResponse(
            user.getId(),
            user.getUsername(),
            user.getRole()
    );
}

}