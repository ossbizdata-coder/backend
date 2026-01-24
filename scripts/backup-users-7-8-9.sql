-- ============================================
-- BACKUP SCRIPT - Attendance Data for Users 7, 8, 9
-- Date: January 18, 2026
-- Purpose: Backup before cleaning up User ID 1 sample data
-- ============================================

-- Create backup table
CREATE TABLE IF NOT EXISTS attendance_backup (
    id INTEGER,
    user_id INTEGER,
    work_date TEXT,
    check_in_time TEXT,
    check_out_time TEXT,
    latitude REAL,
    longitude REAL,
    status TEXT,
    total_minutes INTEGER,
    manual_checkout INTEGER,
    overtime_hours REAL,
    deduction_hours REAL,
    overtime_reason TEXT,
    deduction_reason TEXT,
    backup_date TEXT
);

-- Backup User ID 7 (Piumi)
INSERT INTO attendance_backup
SELECT
    id,
    user_id,
    work_date,
    check_in_time,
    check_out_time,
    latitude,
    longitude,
    status,
    total_minutes,
    manual_checkout,
    overtime_hours,
    deduction_hours,
    overtime_reason,
    deduction_reason,
    datetime('now') as backup_date
FROM attendance
WHERE user_id = 7;

-- Backup User ID 8 (Dammi)
INSERT INTO attendance_backup
SELECT
    id,
    user_id,
    work_date,
    check_in_time,
    check_out_time,
    latitude,
    longitude,
    status,
    total_minutes,
    manual_checkout,
    overtime_hours,
    deduction_hours,
    overtime_reason,
    deduction_reason,
    datetime('now') as backup_date
FROM attendance
WHERE user_id = 8;

-- Backup User ID 9 (Vidusha)
INSERT INTO attendance_backup
SELECT
    id,
    user_id,
    work_date,
    check_in_time,
    check_out_time,
    latitude,
    longitude,
    status,
    total_minutes,
    manual_checkout,
    overtime_hours,
    deduction_hours,
    overtime_reason,
    deduction_reason,
    datetime('now') as backup_date
FROM attendance
WHERE user_id = 9;

-- ============================================
-- VERIFICATION
-- ============================================

SELECT '========================================' as info;
SELECT 'BACKUP COMPLETED' as info;
SELECT '========================================' as info;

SELECT 'Total records backed up:' as info;
SELECT COUNT(*) as total_backed_up FROM attendance_backup;

SELECT '' as blank;
SELECT 'Breakdown by user:' as info;
SELECT
    user_id,
    COUNT(*) as record_count
FROM attendance_backup
GROUP BY user_id
ORDER BY user_id;

SELECT '' as blank;
SELECT 'Sample of backed up data:' as info;
SELECT id, user_id, work_date, status
FROM attendance_backup
LIMIT 10;

SELECT '' as blank;
SELECT '========================================' as info;
SELECT 'Backup table: attendance_backup' as info;
SELECT 'To restore: INSERT INTO attendance SELECT * FROM attendance_backup WHERE user_id = ?' as restore_command;
SELECT '========================================' as info;

