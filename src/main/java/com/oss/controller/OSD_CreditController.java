package com.oss.controller;
import com.oss.dto.OSD_CreditDTO;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import com.oss.service.CreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/credits")
public class OSD_CreditController {
    private static final Logger log = LoggerFactory.getLogger(OSD_CreditController.class);
    @Autowired
    private CreditService creditService;
    @Autowired
    private UserRepository userRepository;
    @GetMapping
    public List<OSD_CreditDTO> getAllCredits() {
        return creditService.getAllCredits();
    }
    /**
     * GET /api/credits/unpaid
     * Get all unpaid credits (convenience endpoint for mobile app)
     * ✅ FIXED: Added explicit endpoint so frontend doesn't need to use /filter?isPaid=false
     */
    @GetMapping("/unpaid")
    public ResponseEntity<List<OSD_CreditDTO>> getUnpaidCredits() {
        List<OSD_CreditDTO> unpaidCredits = creditService.getCreditsByPaidStatus(false);
        return ResponseEntity.ok(unpaidCredits);
    }
    @GetMapping("/filter")
    public List<OSD_CreditDTO> filterCredits(@RequestParam(required = false) Boolean isPaid) {
        return creditService.getCreditsByPaidStatus(isPaid);
    }
    @PostMapping
    public ResponseEntity<?> addCredit(@RequestBody Map<String, Object> body) {
        creditService.saveCredit(body);
        return ResponseEntity.ok("Credit added successfully");
    }
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("isPaid")) {
                return ResponseEntity.badRequest().body("Missing isPaid field");
            }
            Boolean isPaid = (Boolean) body.get("isPaid");
            if (isPaid == null) {
                return ResponseEntity.badRequest().body("isPaid cannot be null");
            }
            creditService.updatePaidStatus(id, isPaid);
            return ResponseEntity.ok("Status updated");
        } catch (RuntimeException e) {
            log.error("Runtime exception updating credit {}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Exception updating credit {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
    @GetMapping("/summary")
    public List<Map<String, Object>> getSummary() {
        return creditService.getUnpaidSummary();
    }
    @GetMapping("/outstanding-total")
    public ResponseEntity<?> getOutstandingTotal() {
        Map<String, Object> response = creditService.getOutstandingTotal();
        return ResponseEntity.ok(response);
    }
    // ==================== STAFF SALARY CALCULATION ENDPOINTS ====================
    /**
     * DELETE /api/credits/{id}
     * Delete a credit record (SUPERADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deleteCredit(@PathVariable Long id, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            creditService.deleteCredit(id, user);
            return ResponseEntity.ok("Credit deleted successfully");
        } catch (RuntimeException e) {
            log.error("Error deleting credit {}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    /**
     * PUT /api/credits/{id}
     * Edit a credit record (SUPERADMIN only)
     */
    @PutMapping("/{id}/edit")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> editCredit(@PathVariable Long id, @RequestBody Map<String, Object> body, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            creditService.editCredit(id, body, user);
            return ResponseEntity.ok("Credit updated successfully");
        } catch (RuntimeException e) {
            log.error("Error updating credit {}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}