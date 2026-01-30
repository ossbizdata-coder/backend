package com.oss.repository;
import com.oss.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // ======================
    // BASIC QUERIES
    // ======================
    Optional<Attendance> findByUserAndWorkDate(
            User user,
            LocalDate workDate
                                              );

    // ✅ Get the LATEST attendance record for a user on a specific date
    // Ordered by ID DESC to get the most recent if duplicates exist
    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.workDate = :workDate ORDER BY a.id DESC")
    Optional<Attendance> findLatestByUserAndWorkDate(
            @Param("user") User user,
            @Param("workDate") LocalDate workDate
                                                    );

    List<Attendance> findByUserOrderByWorkDateDesc(User user);

    List<Attendance> findByUserAndWorkDateBetween(
            User user,
            LocalDate start,
            LocalDate end
                                                 );
    List<Attendance> findByUserAndWorkDateBetweenOrderByWorkDateDesc(
            User user,
            LocalDate start,
            LocalDate end
                                                                    );

    // Find all attendance records for a specific work date (for daily summary)
    List<Attendance> findByWorkDate(LocalDate workDate);

    // ======================
    // MY ATTENDANCE HISTORY
    // ======================
    @Query("""
        SELECT new com.oss.model.AttendanceHistory(
            a.id,
            a.workDate,
            a.status,
            a.isWorking,
            a.overtimeHours,
            a.deductionHours,
            a.overtimeReason,
            a.deductionReason
        )
        FROM Attendance a
        WHERE a.user = :user
        ORDER BY a.workDate DESC
    """)
    List<AttendanceHistory> getMyHistory(
            @Param("user") User user
                                        );
    // ======================
    // STAFF ATTENDANCE REPORT
    // ======================
    @Query("""
        SELECT new com.oss.model.StaffAttendanceReport(
            u.id,
            u.name,
            u.email,
            COUNT(a),
            SUM(CASE WHEN a.isWorking = true THEN 1 ELSE 0 END),
            SUM(CASE WHEN a.isWorking = false THEN 1 ELSE 0 END)
        )
        FROM Attendance a
        JOIN a.user u
        GROUP BY u.id, u.name, u.email
    """)
    List<StaffAttendanceReport> getAttendanceReportGroupedByUser();
    // ======================
    // STAFF SALARY REPORT (NEW SIMPLIFIED VERSION)
    // ======================
    // ✅ Uses daily salary + overtime/deduction adjustments
    @Query("""
        SELECT new com.oss.model.StaffSalaryReport(
            u.id,
            u.name,
            COUNT(CASE WHEN a.isWorking = true THEN 1 END),
            SUM(COALESCE(a.overtimeHours, 0)),
            SUM(COALESCE(a.deductionHours, 0)),
            u.dailySalary,
            (COUNT(CASE WHEN a.isWorking = true THEN 1 END) * u.dailySalary)
                + (SUM(COALESCE(a.overtimeHours, 0)) * (u.dailySalary / 8.0))
                - (SUM(COALESCE(a.deductionHours, 0)) * (u.dailySalary / 8.0))
        )
        FROM Attendance a
        JOIN a.user u
        WHERE a.workDate BETWEEN :from AND :to
        GROUP BY u.id, u.name, u.dailySalary
    """)
    List<StaffSalaryReport> getMonthlyStaffSalary(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
                                                 );
}