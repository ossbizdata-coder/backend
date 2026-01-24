package com.oss.controller;
import com.oss.dto.Common_LoginRequest;
import com.oss.dto.Common_RegisterRequest;
import com.oss.model.Role;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import com.oss.security.CustomUserDetailsService;
import com.oss.security.JwtUtil;
import com.oss.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class Common_AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token user");
        }
        if (user.getRole() != Role.SUPERADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
        }
        return ResponseEntity.ok(userRepository.findAll());
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Common_RegisterRequest req) {
        // Validate name (always required)
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name is required");
        }
        // For CUSTOMER role, auto-generate email and password if not provided
        boolean isCustomer = req.getRole() != null && req.getRole().equalsIgnoreCase("CUSTOMER");
        if (!isCustomer) {
            // For non-customer roles, email and password are required
            if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }
        } else {
            // Auto-generate email and password for customers if not provided
            if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
                String generatedEmail = req.getName().toLowerCase().replaceAll("\\s+", "_")
                    + "_" + System.currentTimeMillis() + "@oss-customer.com";
                req.setEmail(generatedEmail);
            }
            if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
                req.setPassword("customer@" + System.currentTimeMillis());
            }
        }
        User saved = authService.register(req);
        if (saved == null) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        return ResponseEntity.status(201).body(saved);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Common_LoginRequest req) {
        User user = authService.login(req);
        if (user == null) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name(), user.getId());
        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "role", user.getRole().name(),
                        "userId", user.getId()
                )
        );
    }
}