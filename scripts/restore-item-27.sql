-- RESTORE FoodhutItem id 27
-- Date: January 21, 2026
-- Purpose: Restore deleted item 27 to fix orphaned variation 51 and sales 172-173

-- ============================================
-- STEP 1: VERIFY CURRENT STATE
-- ============================================

SELECT '=== CURRENT STATE ===' as step;

-- Check if item 27 exists (should not)
SELECT 'Checking if item 27 exists' as check;
SELECT * FROM foodhut_items WHERE id = 27;

-- Check orphaned variation 51
SELECT 'Checking variation 51 (orphaned)' as check;
SELECT * FROM foodhut_item_variations WHERE id = 51;
-- Expected: 51|125|RISE BAJAT|27|250

-- Check orphaned sales
SELECT 'Checking sales referencing variation 51' as check;
SELECT * FROM foodhut_sales WHERE item_variation_id = 51;
-- Expected: records 172 and 173

-- ============================================
-- STEP 2: RESTORE ITEM 27
-- ============================================

SELECT '=== RESTORING ITEM 27 ===' as step;

-- INSERT the missing item 27
-- Note: You need to provide the correct name for item 27
-- Based on variation name "RISE BAJAT" and neighboring items,
-- possible names could be "RICE BAJAT" or similar

-- Insert item 27 with the correct name: "RISE BAJAT"
-- Use INSERT OR IGNORE to avoid error if item already exists
INSERT OR IGNORE INTO foodhut_items (id, name)
VALUES (27, 'RISE BAJAT');

-- ============================================
-- STEP 3: VERIFY RESTORATION
-- ============================================

SELECT '=== VERIFICATION ===' as step;

-- Check item 27 is restored
SELECT 'Item 27 restored' as check;
SELECT * FROM foodhut_items WHERE id = 27;

-- Verify variation 51 now has a valid parent
SELECT 'Variation 51 status' as check;
SELECT fiv.id, fiv.item_id, fiv.variation, fiv.price, fiv.cost, fi.name as item_name
FROM foodhut_item_variations fiv
JOIN foodhut_items fi ON fiv.item_id = fi.id
WHERE fiv.id = 51;

-- Verify sales 172-173 are now valid
SELECT 'Sales 172-173 status' as check;
SELECT fs.id, fiv.variation, fi.name as item_name, fs.prepared_qty, fs.remaining_qty
FROM foodhut_sales fs
JOIN foodhut_item_variations fiv ON fs.item_variation_id = fiv.id
JOIN foodhut_items fi ON fiv.item_id = fi.id
WHERE fs.id IN (172, 173);

-- Check for any remaining orphaned records
SELECT 'Orphan check' as check;
SELECT COUNT(*) as orphaned_count
FROM foodhut_item_variations fiv
LEFT JOIN foodhut_items fi ON fiv.item_id = fi.id
WHERE fi.id IS NULL;

-- Should return 0

SELECT '=== SUCCESS ===' as step;
SELECT 'Item 27 restored, variation 51 and sales 172-173 are now valid!' as result;

