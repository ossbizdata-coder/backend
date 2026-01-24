-- ============================================
-- FIX ORPHANED FOODHUT_SALES RECORDS
-- Date: January 18, 2026
-- Purpose: Fix foodhut_sales records that reference non-existent users
-- ============================================

-- Step 1: Identify orphaned records
SELECT '========================================' as info;
SELECT 'ORPHANED FOODHUT_SALES RECORDS' as info;
SELECT '========================================' as info;

SELECT '' as blank;
SELECT 'Records with non-existent users:' as info;
SELECT
    fs.id,
    fs.recorded_by,
    fs.transaction_time,
    fs.action_type
FROM foodhut_sales fs
LEFT JOIN users u ON fs.recorded_by = u.id
WHERE u.id IS NULL;

-- Step 2: Count orphaned records
SELECT '' as blank;
SELECT CONCAT('Total orphaned records: ', COUNT(*)) as summary
FROM foodhut_sales fs
LEFT JOIN users u ON fs.recorded_by = u.id
WHERE u.id IS NULL;

-- ============================================
-- FIX OPTION 1: Update orphaned records to use existing user (user_id = 1)
-- ============================================

SELECT '' as blank;
SELECT '========================================' as info;
SELECT 'FIXING ORPHANED RECORDS' as info;
SELECT 'Updating recorded_by to user_id = 1' as info;
SELECT '========================================' as info;

UPDATE foodhut_sales
SET recorded_by = 1
WHERE recorded_by IN (
    SELECT recorded_by
    FROM (
        SELECT fs.recorded_by
        FROM foodhut_sales fs
        LEFT JOIN users u ON fs.recorded_by = u.id
        WHERE u.id IS NULL
    ) AS orphaned
);

-- ============================================
-- VERIFICATION
-- ============================================

SELECT '' as blank;
SELECT '========================================' as info;
SELECT 'VERIFICATION' as info;
SELECT '========================================' as info;

SELECT '' as blank;
SELECT 'Remaining orphaned records (should be 0):' as info;
SELECT COUNT(*) as remaining_orphaned_count
FROM foodhut_sales fs
LEFT JOIN users u ON fs.recorded_by = u.id
WHERE u.id IS NULL;

SELECT '' as blank;
SELECT '========================================' as info;
SELECT 'Fix complete!' as info;
SELECT '========================================' as info;

