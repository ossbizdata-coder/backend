-- Sample Attendance Data for User ID 1 - January 2026
-- Generated for daily salary system testing
-- Includes overtime and deduction scenarios

-- ============================================
-- PART 1: Update User ID 1 with Salary Rates
-- ============================================

-- Set daily salary and deduction rate for User ID 1
UPDATE users
SET daily_salary = 1500,
    deduction_rate_per_hour = 125
WHERE id = 1;

-- Verify user settings
SELECT id, name, email, daily_salary, deduction_rate_per_hour FROM users WHERE id = 1;

-- ============================================
-- PART 2: Sample Attendance Data for January 2026
-- ============================================

-- Week 1: January 1-5, 2026 (Wed-Sun)
-- Day 1: Wednesday, Jan 1 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-01 00:00:00', '2026-01-01 09:00:00', '2026-01-01 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 2: Thursday, Jan 2 - Overtime day (8 hrs + 2 hrs OT)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-02 00:00:00', '2026-01-02 09:00:00', '2026-01-02 19:00:00', 'COMPLETED', 600, 0, 2.0, 0, 'Client meeting extended to finalize project requirements', NULL);

-- Day 3: Friday, Jan 3 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-03 00:00:00', '2026-01-03 09:00:00', '2026-01-03 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 4: Saturday, Jan 4 - Weekend overtime (6 hrs + 3 hrs OT)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-04 00:00:00', '2026-01-04 10:00:00', '2026-01-04 19:00:00', 'COMPLETED', 540, 0, 3.0, 0, 'Emergency server maintenance and deployment', NULL);

-- Day 5: Sunday, Jan 5 - Day off
-- No entry

-- Week 2: January 6-12, 2026
-- Day 6: Monday, Jan 6 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-06 00:00:00', '2026-01-06 09:00:00', '2026-01-06 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 7: Tuesday, Jan 7 - Left early (6.5 hours, 1.5 hrs deduction)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-07 00:00:00', '2026-01-07 09:00:00', '2026-01-07 15:30:00', 'COMPLETED', 390, 0, 0, 1.5, NULL, 'Medical appointment - approved leave');

-- Day 8: Wednesday, Jan 8 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-08 00:00:00', '2026-01-08 09:00:00', '2026-01-08 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 9: Thursday, Jan 9 - Late start (7 hours, 1 hr deduction)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-09 00:00:00', '2026-01-09 10:00:00', '2026-01-09 17:00:00', 'COMPLETED', 420, 0, 0, 1.0, NULL, 'Traffic delay - road accident');

-- Day 10: Friday, Jan 10 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-10 00:00:00', '2026-01-10 09:00:00', '2026-01-10 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 11-12: Weekend - Days off
-- No entries

-- Week 3: January 13-19, 2026
-- Day 13: Monday, Jan 13 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-13 00:00:00', '2026-01-13 09:00:00', '2026-01-13 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 14: Tuesday, Jan 14 - Overtime (8 hrs + 2.5 hrs OT)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-14 00:00:00', '2026-01-14 09:00:00', '2026-01-14 19:30:00', 'COMPLETED', 630, 0, 2.5, 0, 'Year-end report preparation and review', NULL);

-- Day 15: Wednesday, Jan 15 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-15 00:00:00', '2026-01-15 09:00:00', '2026-01-15 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 16: Thursday, Jan 16 - Short day (5 hours - below minimum, no salary)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-16 00:00:00', '2026-01-16 09:00:00', '2026-01-16 14:00:00', 'COMPLETED', 300, 0, 0, 0, NULL, 'Half day - personal matter');

-- Day 17: Friday, Jan 17 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-17 00:00:00', '2026-01-17 09:00:00', '2026-01-17 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 18: Saturday, Jan 18 - Today (partial, still working)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-18 00:00:00', '2026-01-18 09:00:00', NULL, 'IN_PROGRESS', NULL, 0, 0, 0, NULL, NULL);

-- Week 4: January 20-26, 2026 (Future dates - placeholder)
-- Day 20: Monday, Jan 20 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-20 00:00:00', '2026-01-20 09:00:00', '2026-01-20 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 21: Tuesday, Jan 21 - Out of office (3 hours deduction)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-21 00:00:00', '2026-01-21 09:00:00', '2026-01-21 14:00:00', 'COMPLETED', 300, 0, 0, 3.0, NULL, 'Family emergency - approved absence');

-- Day 22: Wednesday, Jan 22 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-22 00:00:00', '2026-01-22 09:00:00', '2026-01-22 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 23: Thursday, Jan 23 - Overtime (8 hrs + 3 hrs OT)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-23 00:00:00', '2026-01-23 09:00:00', '2026-01-23 20:00:00', 'COMPLETED', 660, 0, 3.0, 0, 'Sprint deadline - feature completion and testing', NULL);

-- Day 24: Friday, Jan 24 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-24 00:00:00', '2026-01-24 09:00:00', '2026-01-24 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Week 5: January 27-31, 2026
-- Day 27: Monday, Jan 27 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-27 00:00:00', '2026-01-27 09:00:00', '2026-01-27 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 28: Tuesday, Jan 28 - Late arrival (6.5 hours, 1.5 hrs deduction)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-28 00:00:00', '2026-01-28 10:30:00', '2026-01-28 17:00:00', 'COMPLETED', 390, 0, 0, 1.5, NULL, 'Vehicle breakdown - towing service delay');

-- Day 29: Wednesday, Jan 29 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-29 00:00:00', '2026-01-29 09:00:00', '2026-01-29 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 30: Thursday, Jan 30 - Overtime (8 hrs + 2 hrs OT)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-30 00:00:00', '2026-01-30 09:00:00', '2026-01-30 19:00:00', 'COMPLETED', 600, 0, 2.0, 0, 'Monthly closing tasks and reconciliation', NULL);

-- Day 31: Friday, Jan 31 - Normal day (8 hours)
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, '2026-01-31 00:00:00', '2026-01-31 09:00:00', '2026-01-31 17:00:00', 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- ============================================
-- PART 3: Verification Queries
-- ============================================

-- View all attendance for User ID 1 in January 2026
SELECT
    id,
    work_date,
    status,
    total_minutes / 60.0 as hours_worked,
    overtime_hours,
    deduction_hours,
    overtime_reason,
    deduction_reason
FROM attendance
WHERE user_id = 1
  AND work_date >= '2026-01-01'
  AND work_date <= '2026-01-31'
ORDER BY work_date;

-- Calculate expected salary for January 2026
SELECT
    u.name,
    u.daily_salary,
    u.deduction_rate_per_hour,
    COUNT(CASE WHEN (a.total_minutes / 60.0) >= 6 AND a.status = 'COMPLETED' THEN 1 END) as qualified_days,
    SUM(CASE
        WHEN (a.total_minutes / 60.0) >= 6 AND a.status = 'COMPLETED'
        THEN u.daily_salary + (a.overtime_hours * u.deduction_rate_per_hour) - (a.deduction_hours * u.deduction_rate_per_hour)
        ELSE 0
    END) as total_salary_january,
    SUM(a.overtime_hours) as total_overtime_hours,
    SUM(a.deduction_hours) as total_deduction_hours
FROM users u
LEFT JOIN attendance a ON u.id = a.user_id
WHERE u.id = 1
  AND a.work_date >= '2026-01-01'
  AND a.work_date <= '2026-01-31'
  AND a.status = 'COMPLETED'
GROUP BY u.id, u.name, u.daily_salary, u.deduction_rate_per_hour;

-- Daily breakdown
SELECT
    work_date,
    total_minutes / 60.0 as hours,
    CASE
        WHEN (total_minutes / 60.0) >= 6
        THEN 2000 + (overtime_hours * 150) - (deduction_hours * 150)
        ELSE 0
    END as daily_salary,
    overtime_hours,
    deduction_hours,
    CASE
        WHEN overtime_reason IS NOT NULL THEN overtime_reason
        WHEN deduction_reason IS NOT NULL THEN deduction_reason
        ELSE 'Regular work day'
    END as notes
FROM attendance
WHERE user_id = 1
  AND work_date >= '2026-01-01'
  AND work_date <= '2026-01-31'
  AND status = 'COMPLETED'
ORDER BY work_date;

-- ============================================
-- SUMMARY STATISTICS
-- ============================================

SELECT
    'Total Days Present' as metric,
    COUNT(*) as value
FROM attendance
WHERE user_id = 1
  AND work_date >= '2026-01-01'
  AND work_date <= '2026-01-31'
  AND status = 'COMPLETED'
UNION ALL
SELECT
    'Days >= 6 Hours (Qualified)',
    COUNT(*)
FROM attendance
WHERE user_id = 1
  AND work_date >= '2026-01-01'
  AND work_date <= '2026-01-31'
  AND status = 'COMPLETED'
  AND (total_minutes / 60.0) >= 6
UNION ALL
SELECT
    'Days with Overtime',
    COUNT(*)
FROM attendance
WHERE user_id = 1
  AND work_date >= '2026-01-01'
  AND work_date <= '2026-01-31'
  AND overtime_hours > 0
UNION ALL
SELECT
    'Days with Deductions',
    COUNT(*)
FROM attendance
WHERE user_id = 1
  AND work_date >= '2026-01-01'
  AND work_date <= '2026-01-31'
  AND deduction_hours > 0;

