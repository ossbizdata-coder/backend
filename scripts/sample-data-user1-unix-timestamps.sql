-- Sample Attendance Data for User ID 1 - January 2026
-- Using Unix timestamps (milliseconds) to match existing format
-- Includes overtime and deduction scenarios

-- ============================================
-- PART 0: Cleanup - Remove User ID 1's old sample data only
-- ============================================
-- This ONLY deletes User ID 1's records, keeping all other users' data safe

DELETE FROM attendance WHERE user_id = 1;

SELECT 'Deleted User ID 1 old records. Remaining total:' as info;
SELECT COUNT(*) as remaining_records FROM attendance;

-- ============================================
-- PART 1: Update User ID 1 with Salary Rates
-- ============================================

UPDATE users
SET daily_salary = 1500,
    deduction_rate_per_hour = 125
WHERE id = 1;

SELECT id, name, email, daily_salary, deduction_rate_per_hour FROM users WHERE id = 1;

-- ============================================
-- PART 2: Sample Attendance - Using Unix Timestamps
-- ============================================
-- Note: Unix timestamps in milliseconds
-- Jan 1, 2026 00:00:00 = 1767225600000
-- Each day = 86400000 milliseconds

-- Day 1: Jan 1 - Normal day (8 hours) - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767225600000, 1767258000000, 1767286800000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 2: Jan 2 - Overtime (8 hrs + 2 hrs OT) - 09:00 to 19:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767312000000, 1767344400000, 1767380400000, 'COMPLETED', 600, 0, 2.0, 0, 'Client meeting extended', NULL);

-- Day 3: Jan 3 - Normal day - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767398400000, 1767430800000, 1767459600000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 4: Jan 4 - Weekend overtime (9 hrs + 3 hrs OT) - 10:00 to 19:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767484800000, 1767520800000, 1767553200000, 'COMPLETED', 540, 0, 3.0, 0, 'Emergency server maintenance', NULL);

-- Day 6: Jan 6 - Normal day - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767657600000, 1767690000000, 1767718800000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 7: Jan 7 - Left early (6.5 hrs, 1.5 hrs deduction) - 09:00 to 15:30
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767744000000, 1767776400000, 1767799800000, 'COMPLETED', 390, 0, 0, 1.5, NULL, 'Medical appointment');

-- Day 8: Jan 8 - Normal day - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767830400000, 1767862800000, 1767891600000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 9: Jan 9 - Late start (7 hrs, 1 hr deduction) - 10:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1767916800000, 1767952800000, 1767978000000, 'COMPLETED', 420, 0, 0, 1.0, NULL, 'Traffic delay');

-- Day 10: Jan 10 - Normal day - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1768003200000, 1768035600000, 1768064400000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 13: Jan 13 - Normal day - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1768262400000, 1768294800000, 1768323600000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 14: Jan 14 - Overtime (8 hrs + 2.5 hrs OT) - 09:00 to 19:30
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1768348800000, 1768381200000, 1768419000000, 'COMPLETED', 630, 0, 2.5, 0, 'Year-end report preparation', NULL);

-- Day 15: Jan 15 - Normal day - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1768435200000, 1768467600000, 1768496400000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- Day 16: Jan 16 - Short day (5 hrs - below minimum) - 09:00 to 14:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1768521600000, 1768554000000, 1768572000000, 'COMPLETED', 300, 0, 0, 0, NULL, 'Half day - personal matter');

-- Day 17: Jan 17 - Normal day - 09:00 to 17:00
INSERT INTO attendance (user_id, work_date, check_in_time, check_out_time, status, total_minutes, manual_checkout, overtime_hours, deduction_hours, overtime_reason, deduction_reason)
VALUES (1, 1768608000000, 1768640400000, 1768669200000, 'COMPLETED', 480, 0, 0, 0, NULL, NULL);

-- ============================================
-- VERIFICATION
-- ============================================

SELECT 'Attendance Records Inserted:' as info;
SELECT COUNT(*) as total FROM attendance WHERE user_id = 1;

SELECT 'Sample Daily Breakdown:' as info;
SELECT
    work_date,
    total_minutes / 60.0 as hours,
    overtime_hours,
    deduction_hours,
    CASE
        WHEN (total_minutes / 60.0) >= 6
        THEN 1500 + (overtime_hours * 125) - (deduction_hours * 125)
        ELSE 0
    END as daily_salary
FROM attendance
WHERE user_id = 1
ORDER BY work_date;

SELECT 'Monthly Summary:' as info;
SELECT
    COUNT(CASE WHEN (total_minutes / 60.0) >= 6 THEN 1 END) as qualified_days,
    SUM(overtime_hours) as total_overtime,
    SUM(deduction_hours) as total_deductions,
    SUM(CASE
        WHEN (total_minutes / 60.0) >= 6
        THEN 1500 + (overtime_hours * 125) - (deduction_hours * 125)
        ELSE 0
    END) as total_salary
FROM attendance
WHERE user_id = 1;

