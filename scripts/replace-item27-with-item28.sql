-- Replace item 27 references with item 28
-- Update variation 51 to point to item 28 instead of item 27
-- Then sales 172-173 will automatically reference item 28

-- Check item 28 and its variations
SELECT * FROM foodhut_items WHERE id = 28;
-- Expected: 28|RISE BATTA

SELECT * FROM foodhut_item_variations WHERE item_id = 28;
-- Expected: 52|250|batta|28|125

-- Check current variation 51
SELECT * FROM foodhut_item_variations WHERE id = 51;
-- Expected: 51|125|RISE BAJAT|27|250

-- Update variation 51 to point to item 28 instead of item 27
UPDATE foodhut_item_variations
SET item_id = 28
WHERE id = 51;

-- Verify the update
SELECT * FROM foodhut_item_variations WHERE id = 51;
-- Should now show: 51|125|RISE BAJAT|28|250

-- Verify sales 172-173 now reference item 28 through variation 51
SELECT fs.id, fs.item_variation_id, fiv.variation, fi.name as item_name
FROM foodhut_sales fs
JOIN foodhut_item_variations fiv ON fs.item_variation_id = fiv.id
JOIN foodhut_items fi ON fiv.item_id = fi.id
WHERE fs.id IN (172, 173);

-- Now you can safely delete item 27
DELETE FROM foodhut_items WHERE id = 27;

-- Final verification
SELECT 'Item 27 deleted' as status;
SELECT * FROM foodhut_items WHERE id = 27;
-- Should return no rows

SELECT 'Variation 51 now points to item 28' as status;
SELECT * FROM foodhut_item_variations WHERE id = 51;

