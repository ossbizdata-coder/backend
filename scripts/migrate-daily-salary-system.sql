-- Daily Salary System Migration
-- Date: January 18, 2026
-- Description: Migrate from hourly to daily salary system with overtime/deduction tracking

-- ============================================
-- PART 1: Update Users Table
-- ============================================

-- Add daily salary and deduction rate columns
ALTER TABLE users ADD COLUMN daily_salary DOUBLE DEFAULT 0;
ALTER TABLE users ADD COLUMN deduction_rate_per_hour DOUBLE DEFAULT 0;

-- Update existing users with new rates
UPDATE users SET daily_salary = 3000, deduction_rate_per_hour = 200 WHERE id = 7; -- Piumi
UPDATE users SET daily_salary = 1500, deduction_rate_per_hour = 125 WHERE id = 8; -- Dammi
UPDATE users SET daily_salary = 750, deduction_rate_per_hour = 125 WHERE id = 9; -- Vidusha

-- Verify users table
SELECT id, name, email, daily_salary, deduction_rate_per_hour FROM users WHERE id IN (7, 8, 9);

-- ============================================
-- PART 2: Update Attendance Table
-- ============================================

-- Add overtime and deduction tracking columns
ALTER TABLE attendance ADD COLUMN overtime_hours DOUBLE DEFAULT 0;
ALTER TABLE attendance ADD COLUMN deduction_hours DOUBLE DEFAULT 0;
ALTER TABLE attendance ADD COLUMN overtime_reason TEXT;
ALTER TABLE attendance ADD COLUMN deduction_reason TEXT;

-- Verify attendance table structure
PRAGMA table_info(attendance);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Check updated users
SELECT
    id,
    name,
    email,
    role,
    daily_salary,
    deduction_rate_per_hour,
    hourly_rate as old_hourly_rate
FROM users
WHERE id IN (7, 8, 9);

-- Check attendance table columns
SELECT * FROM attendance LIMIT 1;

-- ============================================
-- ROLLBACK (if needed)
-- ============================================

/*
-- To rollback, run these commands:

-- Remove columns from users table
-- SQLite doesn't support DROP COLUMN directly, need to recreate table
-- Manual rollback required if needed

-- Remove columns from attendance table
-- SQLite doesn't support DROP COLUMN directly
-- Manual rollback required if needed
*/

-- ============================================
-- MIGRATION COMPLETE
-- ============================================

