-- ============================================
-- BACKUP SCRIPT - User ID 1 REAL Attendance Data Only
-- Date: January 18, 2026
-- Purpose: Backup User ID 1's real attendance (IDs 1-78)
-- ============================================

-- Create backup table if it doesn't exist
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

-- Backup User ID 1 REAL data (before the bad sample data)
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
WHERE user_id = 1
AND id < 79;  -- Only real data, not the duplicates

-- ============================================
-- VERIFICATION
-- ============================================

SELECT '========================================' as info;
SELECT 'BACKUP COMPLETED - User ID 1 Real Data' as info;
SELECT '========================================' as info;

SELECT 'User ID 1 records backed up:' as info;
SELECT COUNT(*) as records_backed_up
FROM attendance_backup
WHERE user_id = 1;

SELECT '' as blank;
SELECT 'Sample of User ID 1 backed up data:' as info;
SELECT id, work_date, check_in_time, status
FROM attendance_backup
WHERE user_id = 1
ORDER BY id
LIMIT 5;

SELECT '' as blank;
SELECT '========================================' as info;
SELECT 'These are the REAL records (IDs 1-78)' as info;
SELECT 'The duplicates (IDs 79+) were NOT backed up' as info;
SELECT '========================================' as info;

