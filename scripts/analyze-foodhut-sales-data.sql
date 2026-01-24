-- Analysis of foodhut_sales data provided by user
-- Date: January 21, 2026
-- Purpose: Identify which records reference specific item_variation_id values

-- ============================================
-- RECORDS REFERENCING VARIATION 51
-- ============================================
-- Found records 172 and 173 referencing item_variation_id = 51

SELECT 'Records with variation_id 51' as analysis;
SELECT * FROM foodhut_sales WHERE item_variation_id = 51;
-- Expected: Records 172 and 173

-- ============================================
-- CHECK IF VARIATION 51 EXISTS
-- ============================================
SELECT 'Check if variation 51 exists' as analysis;
SELECT fiv.id, fiv.item_id, fiv.variation, fiv.price, fiv.cost,
       fi.id as foodhut_item_id, fi.name as item_name
FROM foodhut_item_variations fiv
LEFT JOIN foodhut_items fi ON fiv.item_id = fi.id
WHERE fiv.id = 51;

-- ============================================
-- CHECK IF ITEM_ID 27 HAD VARIATION 51
-- ============================================
SELECT 'Check if variation 51 belonged to deleted item 27' as analysis;
SELECT * FROM foodhut_item_variations WHERE id = 51 AND item_id = 27;

-- ============================================
-- ALL UNIQUE VARIATION IDs IN SALES DATA
-- ============================================
SELECT 'All unique variation IDs in sales' as analysis;
SELECT DISTINCT item_variation_id, COUNT(*) as usage_count
FROM foodhut_sales
GROUP BY item_variation_id
ORDER BY item_variation_id;

-- ============================================
-- IDENTIFY ALL ORPHANED SALES
-- ============================================
SELECT 'Sales with non-existent variations' as analysis;
SELECT fs.id, fs.item_variation_id, fs.prepared_qty, fs.remaining_qty,
       fs.transaction_time, fs.recorded_by, fs.action_type
FROM foodhut_sales fs
WHERE fs.item_variation_id NOT IN (
    SELECT fiv.id FROM foodhut_item_variations fiv
);

-- ============================================
-- RECORDS 172 & 173 DETAILS
-- ============================================
-- From your data:
-- 172|1|0|1768813460705|51|12|PREPARED
-- This means: id=172, prepared_qty=1, remaining_qty=0, timestamp=1768813460705,
--             item_variation_id=51, recorded_by=12, action_type=PREPARED

-- 173|0|1|1768813489984|51|12|REMAINING
-- This means: id=173, prepared_qty=0, remaining_qty=1, timestamp=1768813489984,
--             item_variation_id=51, recorded_by=12, action_type=REMAINING

SELECT 'Details of records 172 and 173' as analysis;
SELECT * FROM foodhut_sales WHERE id IN (172, 173);

