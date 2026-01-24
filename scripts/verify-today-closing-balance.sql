-- Verify and fix TODAY's closing balance for shop 3 (Food Hut)
-- Date: January 20, 2026 (TODAY)
-- Record ID: 107 (NOT 110!)

-- Step 1: Check current state of record 107 (today - Jan 20)
SELECT 'Current state of record 107 (TODAY):' as info;
SELECT id, shop_id,
       datetime(business_date/1000, 'unixepoch') as date,
       opening_cash, closing_cash, locked,
       datetime(closed_at/1000, 'unixepoch') as closed_time,
       closed_by_id,
       opening_confirmed
FROM daily_cash
WHERE id = 107;

-- Step 2: Update record 107 with closing balance 26510.0
UPDATE daily_cash
SET closing_cash = 26510.0,
    locked = 1,
    closed_by_id = 12,
    closed_at = strftime('%s', 'now') * 1000
WHERE id = 107 AND shop_id = 3;

-- Step 3: Verify the update
SELECT 'After update (record 107):' as info;
SELECT id, shop_id,
       datetime(business_date/1000, 'unixepoch') as date,
       opening_cash, closing_cash, locked,
       datetime(closed_at/1000, 'unixepoch') as closed_time
FROM daily_cash
WHERE id = 107;

-- Step 4: Reset record 110 (tomorrow) back to original state
UPDATE daily_cash
SET closing_cash = NULL,
    locked = 0,
    closed_by_id = NULL,
    closed_at = NULL
WHERE id = 110 AND shop_id = 3;

-- Step 5: Show both records
SELECT 'Today and Tomorrow for shop 3:' as info;
SELECT id, shop_id,
       datetime(business_date/1000, 'unixepoch') as date,
       opening_cash, closing_cash, locked
FROM daily_cash
WHERE id IN (107, 110)
ORDER BY id;

