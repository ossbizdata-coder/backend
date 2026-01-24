package com.oss.controller;
import com.oss.model.IdeaOfTheWeek;
import com.oss.model.User;
import com.oss.repository.IdeaOfTheWeekRepository;
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
@RequestMapping("/api/messages")
public class OSS_IdeaController {
    @Autowired
    private IdeaOfTheWeekRepository ideaRepository;
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/idea")
    public ResponseEntity<?> submitIdea(@RequestBody Map<String, Object> payload) {
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
            IdeaOfTheWeek idea = new IdeaOfTheWeek();
            idea.setMessage(message);
            idea.setUser(user);
            idea.setCreatedAt(Instant.now());
            ideaRepository.save(idea);
            return ResponseEntity.ok().body(Map.of("message", "Idea submitted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/idea")
    public ResponseEntity<?> getAllIdeas() {
        return ResponseEntity.ok(ideaRepository.findAll());
    }
    @GetMapping("/ideas")
    public ResponseEntity<?> getAllIdeasAlias() {
        return ResponseEntity.ok(ideaRepository.findAll());
    }
    @DeleteMapping("/idea/{id}")
    public ResponseEntity<?> deleteIdea(@PathVariable Long id) {
        ideaRepository.deleteById(id);
        return ResponseEntity.ok().body(Map.of("message", "Idea deleted successfully"));
    }
    @GetMapping("/ideas/summary")
    public ResponseEntity<?> getIdeasSummary() {
        try {
            List<IdeaOfTheWeek> allIdeas = ideaRepository.findAll();
            // Get the latest idea
            IdeaOfTheWeek latestIdea = allIdeas.stream()
                    .max(Comparator.comparing(IdeaOfTheWeek::getCreatedAt))
                    .orElse(null);
            // Count ideas this week
            Instant weekStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .with(java.time.DayOfWeek.MONDAY)
                    .toInstant();
            long ideasThisWeek = allIdeas.stream()
                    .filter(idea -> idea.getCreatedAt().isAfter(weekStart))
                    .count();
            // Count total ideas
            long totalIdeas = allIdeas.size();
            // Count unique users who submitted ideas
            long uniqueUsers = allIdeas.stream()
                    .map(idea -> idea.getUser().getId())
                    .distinct()
                    .count();
            Map<String, Object> summary = new HashMap<>();
            summary.put("latestIdea", latestIdea);
            summary.put("ideasThisWeek", ideasThisWeek);
            summary.put("totalIdeas", totalIdeas);
            summary.put("uniqueUsers", uniqueUsers);
            summary.put("weekStart", weekStart);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/ideas/latest")
    public ResponseEntity<?> getLatestIdea() {
        try {
            List<IdeaOfTheWeek> allIdeas = ideaRepository.findAll();
            IdeaOfTheWeek latestIdea = allIdeas.stream()
                    .max(Comparator.comparing(IdeaOfTheWeek::getCreatedAt))
                    .orElse(null);
            if (latestIdea == null) {
                return ResponseEntity.ok().body(Map.of("message", "No ideas found"));
            }
            return ResponseEntity.ok(latestIdea);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/ideas/week/{userId}")
    public ResponseEntity<?> getUserIdeasThisWeek(@PathVariable Long userId) {
        try {
            Instant weekStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .with(java.time.DayOfWeek.MONDAY)
                    .toInstant();
            List<IdeaOfTheWeek> userIdeasThisWeek = ideaRepository.findAll().stream()
                    .filter(idea -> idea.getUser().getId().equals(userId))
                    .filter(idea -> idea.getCreatedAt().isAfter(weekStart))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userIdeasThisWeek);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}