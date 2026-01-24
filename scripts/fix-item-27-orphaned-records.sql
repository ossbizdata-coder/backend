-- CONFIRMED FIX for orphaned variation 51 and item 27
-- Date: January 21, 2026
-- Item 27 was deleted, leaving variation 51 orphaned
-- This caused sales records 172 and 173 to fail with EntityNotFoundException

-- ============================================
-- STEP 1: VERIFY THE PROBLEM
-- ============================================

SELECT '=== ORPHANED VARIATION 51 ===' as step;
SELECT * FROM foodhut_item_variations WHERE id = 51;
-- Shows: 51|125|RISE BAJAT|27|250 (points to deleted item 27)

SELECT '=== CONFIRM ITEM 27 DOES NOT EXIST ===' as step;
SELECT * FROM foodhut_items WHERE id = 27;
-- Should return no rows

SELECT '=== ORPHANED SALES RECORDS ===' as step;
SELECT * FROM foodhut_sales WHERE item_variation_id = 51;
-- Shows: records 172 and 173

-- ============================================
-- STEP 2: DELETE ORPHANED SALES FIRST
-- ============================================

SELECT '=== DELETING ORPHANED SALES 172 & 173 ===' as step;
DELETE FROM foodhut_sales WHERE item_variation_id = 51;
-- Deletes records 172 and 173

-- ============================================
-- STEP 3: DELETE ORPHANED VARIATION 51
-- ============================================

SELECT '=== DELETING ORPHANED VARIATION 51 ===' as step;
DELETE FROM foodhut_item_variations WHERE id = 51;
-- Deletes variation 51 that points to deleted item 27

-- ============================================
-- STEP 4: VERIFY CLEANUP
-- ============================================

SELECT '=== VERIFICATION ===' as step;

SELECT 'Checking for remaining orphaned sales' as check;
SELECT COUNT(*) as orphaned_sales_count
FROM foodhut_sales fs
WHERE fs.item_variation_id NOT IN (
    SELECT fiv.id FROM foodhut_item_variations fiv
);
-- Should return 0

SELECT 'Checking for remaining orphaned variations' as check;
SELECT COUNT(*) as orphaned_variations_count
FROM foodhut_item_variations fiv
WHERE fiv.item_id NOT IN (
    SELECT fi.id FROM foodhut_items fi
);
-- Should return 0

SELECT '=== SUMMARY ===' as step;
SELECT
    (SELECT COUNT(*) FROM foodhut_items) as total_items,
    (SELECT COUNT(*) FROM foodhut_item_variations) as total_variations,
    (SELECT COUNT(*) FROM foodhut_sales) as total_sales;

-- ============================================
-- WHAT WAS DELETED
-- ============================================
-- 1. foodhut_sales record 172 (1 RISE BAJAT prepared by user 12)
-- 2. foodhut_sales record 173 (1 RISE BAJAT remaining by user 12)
-- 3. foodhut_item_variations record 51 (RISE BAJAT variation)
-- Note: Item 27 was already deleted by the user

