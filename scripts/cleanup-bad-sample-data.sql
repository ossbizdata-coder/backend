-- Cleanup Script - Remove BAD sample data for User ID 1
-- This removes all the incorrectly formatted sample records
-- Keeps only the REAL attendance data (Unix timestamps)

-- ============================================
-- STEP 1: Delete all BAD sample data (IDs 79+)
-- ============================================

DELETE FROM attendance
WHERE user_id = 1
AND id >= 79;

-- Verify cleanup
SELECT 'Remaining User ID 1 records (should be real data only):' as info;
SELECT COUNT(*) as count FROM attendance WHERE user_id = 1;

SELECT 'Sample of remaining records:' as info;
SELECT id, user_id, work_date, check_in_time
FROM attendance
WHERE user_id = 1
ORDER BY id
LIMIT 5;

