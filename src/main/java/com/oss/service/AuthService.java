package com.oss.service;
import com.oss.dto.Common_LoginRequest;
import com.oss.dto.Common_RegisterRequest;
import com.oss.model.Role;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    public User register(Common_RegisterRequest req) {
        if (repo.findByEmail(req.getEmail()).isPresent())
            return null;
        // Determine role: use provided role or default to STAFF
        Role userRole = Role.STAFF; // default
        if (req.getRole() != null && !req.getRole().isEmpty()) {
            try {
                userRole = Role.valueOf(req.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                // If invalid role provided, use default STAFF
                userRole = Role.STAFF;
            }
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .role(userRole)
                .build();
        return repo.save(user);
    }
    public User login(Common_LoginRequest req) {
        return repo.findByEmail(req.getEmail())
                .filter(u -> encoder.matches(req.getPassword(), u.getPassword()))
                .orElse(null);
    }
}