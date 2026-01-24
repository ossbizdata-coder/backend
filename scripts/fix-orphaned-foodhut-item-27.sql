-- Fix orphaned foodhut_sales records that reference deleted FoodhutItem id 27
-- Created: January 21, 2026
-- Issue: FoodhutItem with id 27 was deleted, causing EntityNotFoundException

-- Step 1: Check for orphaned foodhut_sales records
SELECT fs.id, fs.item_variation_id, fs.prepared_qty, fs.remaining_qty, fs.transaction_time, fs.recorded_by
FROM foodhut_sales fs
WHERE fs.item_variation_id IN (
    SELECT fiv.id
    FROM foodhut_item_variations fiv
    WHERE fiv.item_id = 27
);

-- Step 2: Check if there are any item_variations pointing to deleted item 27
SELECT fiv.id, fiv.item_id, fiv.variation, fiv.price, fiv.cost
FROM foodhut_item_variations fiv
WHERE fiv.item_id = 27;

-- Step 3: Delete orphaned foodhut_sales records that reference variations of deleted item 27
DELETE FROM foodhut_sales
WHERE item_variation_id IN (
    SELECT fiv.id
    FROM foodhut_item_variations fiv
    WHERE fiv.item_id = 27
);

-- Step 4: Delete orphaned item_variations that reference deleted item 27
DELETE FROM foodhut_item_variations
WHERE item_id = 27;

-- Step 5: Verify cleanup
SELECT COUNT(*) as remaining_orphaned_sales
FROM foodhut_sales fs
WHERE fs.item_variation_id NOT IN (
    SELECT fiv.id FROM foodhut_item_variations fiv
);

SELECT COUNT(*) as remaining_orphaned_variations
FROM foodhut_item_variations fiv
WHERE fiv.item_id NOT IN (
    SELECT fi.id FROM foodhut_items fi
);

