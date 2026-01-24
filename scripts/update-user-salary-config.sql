-- ============================================
-- UPDATE USER SALARY CONFIGURATIONS
-- Date: January 18, 2026
-- Purpose: Set daily salary rates for staff members
-- ============================================

-- Update Piumi (user_id = 7)
-- Daily salary: Rs 3,000
-- Deduction rate: Rs 200/hour
UPDATE users
SET daily_salary = 3000,
    deduction_rate_per_hour = 200
WHERE id = 7;

-- Update Dammi (user_id = 8)
-- Daily salary: Rs 1,500
-- Deduction rate: Rs 125/hour
UPDATE users
SET daily_salary = 1500,
    deduction_rate_per_hour = 125
WHERE id = 8;

-- Update Vidusha (user_id = 9)
-- Daily salary: Rs 750
-- Deduction rate: Rs 125/hour
UPDATE users
SET daily_salary = 750,
    deduction_rate_per_hour = 125
WHERE id = 9;

-- ============================================
-- VERIFICATION
-- ============================================

SELECT '========================================' as info;
SELECT 'USER SALARY CONFIGURATION UPDATED' as info;
SELECT '========================================' as info;

SELECT '' as blank;
SELECT 'Updated Users:' as info;
SELECT
    id,
    name,
    email,
    daily_salary,
    deduction_rate_per_hour
FROM users
WHERE id IN (7, 8, 9)
ORDER BY id;

SELECT '' as blank;
SELECT '========================================' as info;
SELECT 'Configuration complete!' as info;
SELECT 'Next: Restart backend service' as info;
SELECT '========================================' as info;

