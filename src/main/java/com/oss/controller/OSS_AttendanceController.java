package com.oss.controller;
import com.oss.dto.AttendanceAdjustmentRequest;
import com.oss.model.Attendance;
import com.oss.model.AttendanceHistory;
import com.oss.model.Role;
import com.oss.model.User;
import com.oss.repository.AttendanceRepository;
import com.oss.repository.UserRepository;
import com.oss.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class OSS_AttendanceController {
    private final AttendanceService service;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    @GetMapping("/today")
    public ResponseEntity<?> today() {
        Attendance attendance = service.getToday();
        if (attendance == null) {
            // Return proper JSON instead of null to avoid JSON parse errors
            return ResponseEntity.ok(Map.of(
                "status", "NOT_STARTED",
                "message", "No attendance record for today"
            ));
        }
        return ResponseEntity.ok(attendance);
    }

    /**
     * PUT /api/attendance/today
     * Update today's attendance status
     * Used by mobile app to set NOT_WORKING status when user clicks "NO"
     */
    @PutMapping("/today")
    public ResponseEntity<?> updateTodayStatus(
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Status is required"));
            }

            Attendance attendance = service.updateTodayStatus(status);
            return ResponseEntity.ok(attendance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update status: " + e.getMessage()));
        }
    }

    @PostMapping("/check-in")
    public Attendance checkIn(
            @RequestBody(required = false) Map<String, String> body) {
        Instant t = body != null && body.containsKey("checkInTime")
                    ? Instant.parse(body.get("checkInTime"))
                    : null;
        String timezone = body != null ? body.get("timezone") : null;
        return service.checkIn(t, timezone);
    }

    @PostMapping("/check-out")
    public Attendance checkOut(
            @RequestBody(required = false) Map<String, String> body) {
        Instant t = body != null && body.containsKey("checkOutTime")
                    ? Instant.parse(body.get("checkOutTime"))
                    : null;
        String timezone = body != null ? body.get("timezone") : null;
        return service.checkOut(t, timezone);
    }

    /**
     * POST /api/attendance/not-working
     * Simple endpoint to mark as NOT WORKING (NO button)
     * Accepts timezone from client
     */
    @PostMapping("/not-working")
    public ResponseEntity<?> markNotWorking(@RequestBody(required = false) Map<String, String> body) {
        try {
            String timezone = body != null ? body.get("timezone") : null;
            Attendance attendance = service.checkOut(null, timezone);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to mark as not working: " + e.getMessage()));
        }
    }

    /**
     * POST /api/attendance/working
     * Simple endpoint to mark as WORKING (YES button)
     * Accepts timezone from client
     */
    @PostMapping("/working")
    public ResponseEntity<?> markWorking(@RequestBody(required = false) Map<String, String> body) {
        try {
            String timezone = body != null ? body.get("timezone") : null;
            Attendance attendance = service.checkIn(null, timezone);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to mark as working: " + e.getMessage()));
        }
    }
    @GetMapping("/history")
    public List<AttendanceHistory> history() {
        return service.myHistory();
    }
    @GetMapping("/all")
    public List<Map<String, Object>> all() {
        return service.allAttendance();
    }
    /**
     * Update attendance overtime/deduction adjustments
     * Admin/SuperAdmin only
     */
    @PutMapping("/{id}/adjustments")
    public ResponseEntity<?> updateAttendanceAdjustments(
            @PathVariable Long id,
            @RequestBody AttendanceAdjustmentRequest request,
            Principal principal
    ) {
        try {
            // Get current user
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found"
                    ));
            // Check if ADMIN or SUPERADMIN
            if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPERADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admin privileges required."));
            }
            // Find attendance record
            Attendance attendance = attendanceRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Attendance record not found"
                    ));
            // Update adjustments
            attendance.setOvertimeHours(request.getOvertimeHours() != null ? request.getOvertimeHours() : 0.0);
            attendance.setDeductionHours(request.getDeductionHours() != null ? request.getDeductionHours() : 0.0);
            attendance.setOvertimeReason(request.getOvertimeReason());
            attendance.setDeductionReason(request.getDeductionReason());
            // Save
            Attendance updated = attendanceRepository.save(attendance);
            return ResponseEntity.ok(Map.of(
                    "message", "Attendance adjustments updated successfully",
                    "attendanceId", updated.getId(),
                    "overtimeHours", updated.getOvertimeHours(),
                    "deductionHours", updated.getDeductionHours()
            ));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error updating adjustments: " + e.getMessage()));
        }
    }
}