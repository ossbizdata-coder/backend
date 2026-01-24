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
    @PostMapping("/check-in")
    public Attendance checkIn(
            @RequestBody(required = false) Map<String, String> body) {
        Instant t = body != null && body.containsKey("checkInTime")
                    ? Instant.parse(body.get("checkInTime"))
                    : null;
        return service.checkIn(t);
    }
    @PostMapping("/check-out")
    public Attendance checkOut(
            @RequestBody(required = false) Map<String, String> body) {
        Instant t = body != null && body.containsKey("checkOutTime")
                    ? Instant.parse(body.get("checkOutTime"))
                    : null;
        return service.checkOut(t);
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