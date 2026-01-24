-- Fix orphaned attendance records that reference deleted user ID 3
-- Update them to reference user ID 1 instead

-- First, check which attendance records reference user ID 3
SELECT
    id,
    user_id,
    work_date,
    check_in_time,
    check_out_time,
    status
FROM attendance
WHERE user_id = 3;

-- Update all attendance records from user ID 3 to user ID 1
UPDATE attendance
SET user_id = 1
WHERE user_id = 3;

-- Verify the update
SELECT
    COUNT(*) as updated_count
FROM attendance
WHERE user_id = 1;

-- Check if any other orphaned user references exist
SELECT DISTINCT a.user_id
FROM attendance a
LEFT JOIN users u ON a.user_id = u.id
WHERE u.id IS NULL;

