-- Fix all existing attendance records with date-only work_date values
-- This updates them to proper timestamp format

-- First, let's see what we have
SELECT id, user_id, work_date, check_in_time FROM attendance LIMIT 5;

-- Update all attendance records to add time component to work_date
-- SQLite stores dates as text, so we need to append time if missing
UPDATE attendance
SET work_date = work_date || ' 00:00:00'
WHERE work_date NOT LIKE '%:%';

-- Verify the fix
SELECT id, user_id, work_date, check_in_time FROM attendance LIMIT 5;

-- Count updated records
SELECT
    COUNT(*) as total_records,
    COUNT(CASE WHEN work_date LIKE '%:%' THEN 1 END) as with_time,
    COUNT(CASE WHEN work_date NOT LIKE '%:%' THEN 1 END) as without_time
FROM attendance;

