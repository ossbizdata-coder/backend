#!/bin/bash
# Check for ACTUAL duplicate dates in the database

echo "=== CHECKING FOR DUPLICATE DATES ==="
echo ""

# This query will ACTUALLY check for duplicates by DATE (not timestamp)
sqlite3 /dev/oss/oss.db << 'EOF'
.mode column
.headers on

-- Check for duplicates by converting timestamp to date
SELECT
    user_id,
    date(datetime(work_date, 'unixepoch')) as date_only,
    COUNT(*) as record_count,
    GROUP_CONCAT(id || ':' || status) as all_records
FROM attendance
WHERE user_id = 47
GROUP BY user_id, date(datetime(work_date, 'unixepoch'))
HAVING COUNT(*) > 1
ORDER BY date_only DESC;

EOF

echo ""
echo "=== If empty above, NO DUPLICATES exist ==="
echo ""

# Also show all records with readable dates
echo "=== ALL RECORDS WITH READABLE DATES ==="
sqlite3 /dev/oss/oss.db << 'EOF'
.mode column
.headers on

SELECT
    id,
    datetime(work_date, 'unixepoch') as full_datetime,
    date(datetime(work_date, 'unixepoch')) as date_only,
    is_working,
    status
FROM attendance
WHERE user_id = 47
ORDER BY work_date DESC
LIMIT 10;
EOF
