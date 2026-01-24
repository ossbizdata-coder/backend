-- Restore foodhut_sales records 172 and 173
-- Created: January 21, 2026
-- Data provided by user for restoration

-- First, verify that the referenced variation (id 51) and user (id 12) exist
SELECT 'Checking item_variation_id 51' as check_type;
SELECT * FROM foodhut_item_variations WHERE id = 51;

SELECT 'Checking user id 12' as check_type;
SELECT * FROM users WHERE id = 12;

-- Check if these sale records already exist
SELECT 'Checking if sales 172, 173 exist' as check_type;
SELECT * FROM foodhut_sales WHERE id IN (172, 173);

-- Insert the sales records
-- Record 172: item_variation_id=51, prepared=1, remaining=0, time=1768813460705 (unix timestamp in ms), user=12
INSERT INTO foodhut_sales (id, item_variation_id, prepared_qty, remaining_qty, transaction_time, recorded_by, action_type)
VALUES (
    172,
    51,
    1,
    0,
    FROM_UNIXTIME(1768813460705/1000),
    12,
    'PREPARED'
);

-- Record 173: item_variation_id=51, prepared=0, remaining=1, time=1768813489984 (unix timestamp in ms), user=12
INSERT INTO foodhut_sales (id, item_variation_id, prepared_qty, remaining_qty, transaction_time, recorded_by, action_type)
VALUES (
    173,
    51,
    0,
    1,
    FROM_UNIXTIME(1768813489984/1000),
    12,
    'REMAINING'
);

-- Verify insertion
SELECT 'Verification - Inserted records' as verification;
SELECT * FROM foodhut_sales WHERE id IN (172, 173);

-- Convert timestamps to readable format for verification
SELECT
    id,
    item_variation_id,
    prepared_qty,
    remaining_qty,
    transaction_time,
    FROM_UNIXTIME(1768813460705/1000) as expected_time_172,
    FROM_UNIXTIME(1768813489984/1000) as expected_time_173,
    recorded_by,
    action_type
FROM foodhut_sales
WHERE id IN (172, 173);

