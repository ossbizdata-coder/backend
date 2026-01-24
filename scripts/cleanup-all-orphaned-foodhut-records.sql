-- Comprehensive cleanup of ALL orphaned foodhut records
-- Created: January 21, 2026
-- Purpose: Clean up any foodhut_sales or foodhut_item_variations that reference deleted items

-- ============================================
-- STEP 1: IDENTIFY ORPHANED RECORDS
-- ============================================

-- Check for orphaned foodhut_sales (references non-existent variations)
SELECT 'Orphaned Sales' as type, COUNT(*) as count
FROM foodhut_sales fs
WHERE fs.item_variation_id NOT IN (
    SELECT fiv.id FROM foodhut_item_variations fiv
);

-- Check for orphaned item_variations (references non-existent items)
SELECT 'Orphaned Variations' as type, COUNT(*) as count
FROM foodhut_item_variations fiv
WHERE fiv.item_id NOT IN (
    SELECT fi.id FROM foodhut_items fi
);

-- ============================================
-- STEP 2: LIST SPECIFIC ORPHANED RECORDS
-- ============================================

-- List orphaned sales with details
SELECT fs.id, fs.item_variation_id, fs.prepared_qty, fs.remaining_qty,
       fs.transaction_time, fs.recorded_by, fs.action_type
FROM foodhut_sales fs
WHERE fs.item_variation_id NOT IN (
    SELECT fiv.id FROM foodhut_item_variations fiv
);

-- List orphaned variations with details
SELECT fiv.id, fiv.item_id, fiv.variation, fiv.price, fiv.cost
FROM foodhut_item_variations fiv
WHERE fiv.item_id NOT IN (
    SELECT fi.id FROM foodhut_items fi
);

-- ============================================
-- STEP 3: DELETE ORPHANED RECORDS
-- ============================================

-- Delete orphaned foodhut_sales (safe to delete - references broken data)
DELETE FROM foodhut_sales
WHERE item_variation_id NOT IN (
    SELECT fiv.id FROM foodhut_item_variations fiv
);

-- Delete orphaned item_variations (safe to delete - references broken data)
DELETE FROM foodhut_item_variations
WHERE item_id NOT IN (
    SELECT fi.id FROM foodhut_items fi
);

-- ============================================
-- STEP 4: VERIFY CLEANUP
-- ============================================

-- Verify no more orphaned sales
SELECT 'Remaining Orphaned Sales' as check_type, COUNT(*) as count
FROM foodhut_sales fs
WHERE fs.item_variation_id NOT IN (
    SELECT fiv.id FROM foodhut_item_variations fiv
);

-- Verify no more orphaned variations
SELECT 'Remaining Orphaned Variations' as check_type, COUNT(*) as count
FROM foodhut_item_variations fiv
WHERE fiv.item_id NOT IN (
    SELECT fi.id FROM foodhut_items fi
);

-- Show summary of remaining valid data
SELECT 'Valid Items' as data_type, COUNT(*) as count FROM foodhut_items
UNION ALL
SELECT 'Valid Variations' as data_type, COUNT(*) as count FROM foodhut_item_variations
UNION ALL
SELECT 'Valid Sales' as data_type, COUNT(*) as count FROM foodhut_sales;

