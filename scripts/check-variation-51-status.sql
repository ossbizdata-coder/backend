-- Check if variation 51 is orphaned and causing the error
-- Date: January 21, 2026
-- This will determine if records 172 and 173 need to be deleted

-- ============================================
-- CHECK VARIATION 51 STATUS
-- ============================================

SELECT '=== VARIATION 51 STATUS ===' as step;

-- Check if variation 51 exists and what item it points to
SELECT
    fiv.id as variation_id,
    fiv.item_id,
    fiv.variation,
    fiv.price,
    fiv.cost,
    CASE
        WHEN fi.id IS NULL THEN 'ORPHANED - Item does not exist!'
        WHEN fi.id = 27 THEN 'ORPHANED - Points to deleted item 27!'
        ELSE 'VALID'
    END as status,
    fi.name as item_name
FROM foodhut_item_variations fiv
LEFT JOIN foodhut_items fi ON fiv.item_id = fi.id
WHERE fiv.id = 51;

-- ============================================
-- CHECK SALES RECORDS USING VARIATION 51
-- ============================================

SELECT '=== SALES USING VARIATION 51 ===' as step;

SELECT
    fs.id as sale_id,
    fs.item_variation_id,
    fs.prepared_qty,
    fs.remaining_qty,
    FROM_UNIXTIME(fs.transaction_time/1000) as transaction_time,
    fs.recorded_by as user_id,
    fs.action_type,
    u.name as user_name
FROM foodhut_sales fs
LEFT JOIN users u ON fs.recorded_by = u.id
WHERE fs.item_variation_id = 51
ORDER BY fs.id;

-- ============================================
-- DECISION LOGIC
-- ============================================

SELECT '=== DECISION ===' as step;

SELECT
    CASE
        WHEN NOT EXISTS (SELECT 1 FROM foodhut_item_variations WHERE id = 51)
            THEN 'Variation 51 does NOT exist - Records 172 & 173 are ORPHANED and will be DELETED'
        WHEN EXISTS (
            SELECT 1 FROM foodhut_item_variations fiv
            WHERE fiv.id = 51 AND fiv.item_id = 27
        )
            THEN 'Variation 51 points to DELETED item 27 - Records 172 & 173 are ORPHANED and will be DELETED'
        WHEN EXISTS (
            SELECT 1 FROM foodhut_item_variations fiv
            LEFT JOIN foodhut_items fi ON fiv.item_id = fi.id
            WHERE fiv.id = 51 AND fi.id IS NULL
        )
            THEN 'Variation 51 points to NON-EXISTENT item - Records 172 & 173 are ORPHANED and will be DELETED'
        ELSE 'Variation 51 is VALID - Records 172 & 173 will be KEPT'
    END as decision;

-- ============================================
-- IF ORPHANED, SHOW WHAT WILL BE DELETED
-- ============================================

SELECT '=== RECORDS TO BE DELETED (if orphaned) ===' as step;

SELECT fs.*
FROM foodhut_sales fs
WHERE fs.item_variation_id = 51
  AND fs.item_variation_id NOT IN (
      SELECT fiv.id FROM foodhut_item_variations fiv
  );

