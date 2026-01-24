package com.oss.repository;

import com.oss.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // ======================
    // BASIC QUERIES
    // ======================

    Optional<Attendance> findByUserAndWorkDate(
            User user,
            Instant workDate
                                              );

    List<Attendance> findByUserOrderByWorkDateDesc(User user);

    List<Attendance> findByUserAndWorkDateBetween(
            User user,
            Instant start,
            Instant end
                                                 );

    List<Attendance> findByUserAndWorkDateBetweenOrderByWorkDateDesc(
            User user,
            Instant start,
            Instant end
                                                                    );

    // ======================
    // MY ATTENDANCE HISTORY
    // ======================

    @Query("""
        SELECT new com.oss.model.AttendanceHistory(
            a.workDate,
            a.checkInTime,
            a.checkOutTime,
            a.totalMinutes,
            a.manualCheckout,
            a.status
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
            SUM(CASE WHEN a.checkInTime IS NOT NULL THEN 1 ELSE 0 END),
            SUM(CASE WHEN a.checkOutTime IS NOT NULL THEN 1 ELSE 0 END)
        )
        FROM Attendance a
        JOIN a.user u
        GROUP BY u.id, u.name, u.email
    """)
    List<StaffAttendanceReport> getAttendanceReportGroupedByUser();

    // ======================
    // STAFF SALARY REPORT (SAFE VERSION)
    // ======================
    // ❌ REMOVED TIMESTAMPDIFF (DB specific)
    // ✔ Uses pre-calculated totalMinutes

    @Query("""
        SELECT new com.oss.model.StaffSalaryReport(
            u.id,
            u.name,
            SUM(a.totalMinutes),
            SUM(a.totalMinutes) / 60.0,
            u.hourlyRate,
            (SUM(a.totalMinutes) / 60.0) * u.hourlyRate
        )
        FROM Attendance a
        JOIN a.user u
        WHERE a.workDate BETWEEN :from AND :to
          AND a.status = 'COMPLETED'
          AND a.totalMinutes IS NOT NULL
        GROUP BY u.id, u.name, u.hourlyRate
    """)
    List<StaffSalaryReport> getMonthlyStaffSalary(
            @Param("from") Instant from,
            @Param("to") Instant to
                                                 );
}
