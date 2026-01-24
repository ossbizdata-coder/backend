package com.oss.controller;

import com.oss.model.Improvement;
import com.oss.model.User;
import com.oss.repository.ImprovementRepository;
import com.oss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages/improvement")
public class OSS_ImprovementController {
    @Autowired
    private ImprovementRepository improvementRepo;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> submitImprovement(@RequestBody Map<String, Object> payload) {
        try {
            String message = (String) payload.get("message");
            Integer userIdInt = (Integer) payload.get("userId");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message cannot be empty");
            }

            if (userIdInt == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            Long userId = userIdInt.longValue();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Improvement improvement = new Improvement();
            improvement.setMessage(message);
            improvement.setUser(user);
            improvement.setCreatedAt(Instant.now());

            improvementRepo.save(improvement);

            return ResponseEntity.ok().body(Map.of("message", "Improvement submitted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Improvement>> getAllImprovements() {
        return ResponseEntity.ok(improvementRepo.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImprovement(@PathVariable Long id) {
        improvementRepo.deleteById(id);
        return ResponseEntity.ok().body(Map.of("message", "Improvement deleted successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getImprovementsSummary() {
        try {
            List<Improvement> allImprovements = improvementRepo.findAll();

            // Get the latest improvement
            Improvement latestImprovement = allImprovements.stream()
                    .max(Comparator.comparing(Improvement::getCreatedAt))
                    .orElse(null);

            // Count improvements this week
            Instant weekStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .with(java.time.DayOfWeek.MONDAY)
                    .toInstant();

            long improvementsThisWeek = allImprovements.stream()
                    .filter(imp -> imp.getCreatedAt().isAfter(weekStart))
                    .count();

            // Count total improvements
            long totalImprovements = allImprovements.size();

            // Count unique users who submitted improvements
            long uniqueUsers = allImprovements.stream()
                    .map(imp -> imp.getUser().getId())
                    .distinct()
                    .count();

            Map<String, Object> summary = new HashMap<>();
            summary.put("latestImprovement", latestImprovement);
            summary.put("improvementsThisWeek", improvementsThisWeek);
            summary.put("totalImprovements", totalImprovements);
            summary.put("uniqueUsers", uniqueUsers);
            summary.put("weekStart", weekStart);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestImprovement() {
        try {
            List<Improvement> allImprovements = improvementRepo.findAll();
            Improvement latestImprovement = allImprovements.stream()
                    .max(Comparator.comparing(Improvement::getCreatedAt))
                    .orElse(null);

            if (latestImprovement == null) {
                return ResponseEntity.ok().body(Map.of("message", "No improvements found"));
            }

            return ResponseEntity.ok(latestImprovement);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/week/{userId}")
    public ResponseEntity<?> getUserImprovementsThisWeek(@PathVariable Long userId) {
        try {
            Instant weekStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .with(java.time.DayOfWeek.MONDAY)
                    .toInstant();

            List<Improvement> userImprovementsThisWeek = improvementRepo.findAll().stream()
                    .filter(imp -> imp.getUser().getId().equals(userId))
                    .filter(imp -> imp.getCreatedAt().isAfter(weekStart))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userImprovementsThisWeek);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
