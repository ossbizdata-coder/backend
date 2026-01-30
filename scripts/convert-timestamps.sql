-- Convert all timestamps to readable dates to identify actual duplicates
SELECT
    id,
    user_id,
    work_date,
    datetime(work_date, 'unixepoch', 'localtime') as readable_date,
    date(datetime(work_date, 'unixepoch', 'localtime')) as date_only,
    is_working,
    status,
    overtime_hours,
    deduction_hours
FROM attendance
WHERE user_id = 47
ORDER BY work_date, id;
