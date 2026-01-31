package com.daghan.catalog.interfaces.web.rest;

import com.daghan.catalog.application.dto.LoginRequest;
import com.daghan.catalog.application.dto.LoginResponse;
import com.daghan.catalog.infrastructure.persistence.repository.SpringDataUserRepository;
import com.daghan.catalog.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for JWT token generation")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final SpringDataUserRepository userRepository;
    private final JwtService jwtService;

    public AuthRestController(
            AuthenticationManager authenticationManager,
            SpringDataUserRepository userRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()));

        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generate(user.getUsername(), user.getRole());

        return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getRole()));
    }
}
