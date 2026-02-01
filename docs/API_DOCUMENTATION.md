# OSS Backend API Documentation

**Complete API Reference for Dashboard Development**

**Base URL (Production):** `http://74.208.132.78`  
**Date Generated:** February 2, 2026  
**Authentication:** Bearer JWT tokens (obtain via `/api/auth/login`)

---

## Table of Contents

1. [Authentication](#authentication)
2. [Users](#users)
3. [Attendance](#attendance)
4. [Daily Cash & Shops](#daily-cash--shops)
5. [Transactions](#transactions)
6. [Admin Endpoints](#admin-endpoints)
7. [Credits](#credits)
8. [Reports](#reports)
9. [Audit Logs](#audit-logs)
10. [Salary](#salary)
11. [Expense Types](#expense-types)
12. [Foodhut](#foodhut)
13. [Ideas & Improvements](#ideas--improvements)
14. [Migration](#migration)
15. [Common DTOs](#common-dtos)

---

## Authentication

### POST /api/auth/login

**Description:** Login and obtain JWT token

**Auth Required:** No

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "SUPERADMIN",
  "userId": 123
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid email or password

**Dashboard Usage:** Store the token in localStorage/sessionStorage and include in all subsequent requests as `Authorization: Bearer {token}`

---

### POST /api/auth/register

**Description:** Register a new user

**Auth Required:** No

**Request Body:**
```json
{
  "name": "Alice Smith",
  "email": "alice@example.com",
  "password": "securepass",
  "role": "ADMIN"
}
```

**Success Response (201):** User object

**Notes:**
- For role `CUSTOMER`, email and password are auto-generated if not provided
- Email is generated as: `{name}_timestamp@oss-customer.com`
- Password is generated as: `customer@{timestamp}`

---

### GET /api/auth/all-users

**Description:** Get all users (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Success Response (200):** Array of User objects

---

## Users

### GET /api/users

**Description:** Get all users

**Auth Required:** Bearer token

**Success Response (200):** Array of User objects

---

### GET /api/users/{id}

**Description:** Get user by ID

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): User ID

**Success Response (200):** User object

---

### POST /api/users

**Description:** Create new user

**Auth Required:** Bearer token

**Request Body:** User object

**Success Response (200):** Created User object

---

### PUT /api/users/{id}

**Description:** Update user

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): User ID

**Request Body:** User object with updated fields

**Success Response (200):** Updated User object

---

### DELETE /api/users/{id}

**Description:** Delete user

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): User ID

**Success Response (200):** "User deleted."

---

## Attendance

### GET /api/attendance/today

**Description:** Get today's attendance record for authenticated user

**Auth Required:** Optional

**Success Response (200):**
```json
{
  "id": 123,
  "user": {
    "id": 45,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "workDate": "2026-02-02",
  "status": "WORKING",
  "isWorking": true,
  "overtimeHours": 0.0,
  "deductionHours": 0.0,
  "overtimeReason": null,
  "deductionReason": null
}
```

**No Record Response (200):**
```json
{
  "status": "NOT_STARTED",
  "message": "No attendance record for today"
}
```

---

### PUT /api/attendance/today

**Description:** Update today's attendance status

**Auth Required:** Bearer token

**Request Body:**
```json
{
  "status": "NOT_WORKING"
}
```

**Success Response (200):** Updated Attendance object

**Error Responses:**
- `400 Bad Request`: Status is required or invalid

---

### POST /api/attendance/check-in

**Description:** Check in for today (mark as working)

**Auth Required:** Bearer token

**Request Body (Optional):**
```json
{
  "checkInTime": "2026-02-02T08:00:00Z",
  "timezone": "Asia/Colombo"
}
```

**Success Response (200):** Attendance object

---

### POST /api/attendance/check-out

**Description:** Check out for today

**Auth Required:** Bearer token

**Request Body (Optional):**
```json
{
  "checkOutTime": "2026-02-02T17:00:00Z",
  "timezone": "Asia/Colombo"
}
```

**Success Response (200):** Attendance object

---

### POST /api/attendance/not-working

**Description:** Mark today as NOT WORKING (NO button)

**Auth Required:** Bearer token

**Request Body (Optional):**
```json
{
  "timezone": "Asia/Colombo"
}
```

**Success Response (200):** Attendance object

---

### POST /api/attendance/working

**Description:** Mark today as WORKING (YES button)

**Auth Required:** Bearer token

**Request Body (Optional):**
```json
{
  "timezone": "Asia/Colombo"
}
```

**Success Response (200):** Attendance object

---

### GET /api/attendance/history

**Description:** Get attendance history for authenticated user

**Auth Required:** Bearer token

**Success Response (200):** Array of AttendanceHistory objects

---

### GET /api/attendance/all

**Description:** Get all attendance records (admin view)

**Auth Required:** Bearer token

**Success Response (200):** Array of Maps with aggregated attendance data

---

### PUT /api/attendance/{id}/adjustments

**Description:** Update overtime/deduction adjustments (ADMIN or SUPERADMIN only)

**Auth Required:** Bearer token (ADMIN or SUPERADMIN role)

**Path Parameters:**
- `id` (Long): Attendance record ID

**Request Body:**
```json
{
  "overtimeHours": 2.0,
  "deductionHours": 0.5,
  "overtimeReason": "Extra project work",
  "deductionReason": "Late arrival"
}
```

**Success Response (200):**
```json
{
  "message": "Attendance adjustments updated successfully",
  "attendanceId": 123,
  "overtimeHours": 2.0,
  "deductionHours": 0.5
}
```

**Error Responses:**
- `403 Forbidden`: Access denied. Admin privileges required.
- `404 Not Found`: Attendance record not found

---

### PUT /api/attendance/update-status

**Description:** Update attendance status by userId and workDate (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Request Body:**
```json
{
  "userId": 1,
  "workDate": "2026-02-02",
  "status": "NOT_WORKING"
}
```

**Success Response (200):**
```json
{
  "message": "Status updated successfully"
}
```

**Error Responses:**
- `400 Bad Request`: userId, workDate and status are required OR Invalid status value
- `403 Forbidden`: Only SUPERADMIN can access
- `404 Not Found`: Attendance record not found

**Valid Status Values:**
- `WORKING`
- `NOT_WORKING`
- Other values defined in `AttendanceStatus` enum

---

### PUT /api/attendance/update-adjustments

**Description:** Update attendance adjustments by userId and workDate (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Request Body:**
```json
{
  "userId": 1,
  "workDate": "2026-02-02",
  "overtimeHours": 2.0,
  "deductionHours": 0.5,
  "overtimeReason": "Extra work on deadline",
  "deductionReason": "Late by 30 minutes"
}
```

**Success Response (200):**
```json
{
  "message": "Adjustments updated successfully"
}
```

**Error Responses:**
- `400 Bad Request`: userId and workDate are required OR Overtime and deduction hours must be non-negative
- `403 Forbidden`: Only SUPERADMIN can access
- `404 Not Found`: Attendance record not found

---

## Daily Cash & Shops

### GET /api/shops/summary

**Description:** Get latest closing cash per shop (main menu view)

**Auth Required:** Bearer token

**Success Response (200):** Array of ShopSummaryDTO objects

**Example Response:**
```json
[
  {
    "shopId": 1,
    "shopCode": "CAFE",
    "shopName": "Main Cafe",
    "latestClosingCash": 12500.50,
    "lastUpdated": "2026-02-01T18:00:00Z"
  }
]
```

---

### GET /api/daily-cash/{shopId}/{date}

**Description:** Get daily cash summary for specific shop and date

**Auth Required:** Bearer token

**Path Parameters:**
- `shopId` (Long): Shop ID
- `date` (String): Date in ISO format (YYYY-MM-DD)

**Success Response (200):** DailyCashSummaryDTO object

---

### GET /api/daily-cash/{shopId}

**Description:** Get today's daily cash summary for a shop

**Auth Required:** Bearer token

**Path Parameters:**
- `shopId` (Long): Shop ID

**Success Response (200):** DailyCashSummaryDTO object

---

### GET /api/daily-cash/{shopId}/latest-closing-balance

**Description:** Get the latest closing balance within past N days (optimized endpoint)

**Auth Required:** Bearer token

**Path Parameters:**
- `shopId` (Long): Shop ID

**Query Parameters:**
- `daysBack` (int, optional): Number of days to look back (default: 7)

**Success Response (200):**
```json
{
  "closingBalance": 12500.50,
  "date": "2026-02-01",
  "shopId": 1
}
```

**Dashboard Usage:** Use this to prefill opening balance when creating new daily cash entry. This replaces 7 API calls with 1 optimized query.

---

### POST /api/daily-cash/{id}/expenses

**Description:** Add expense to daily cash

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): Daily cash ID

**Request Body:**
```json
{
  "amount": 1500.00,
  "expenseTypeId": 71,
  "description": "Office supplies"
}
```

**Success Response (200):** "Expense added successfully"

**Error Responses:**
- `400 Bad Request`: amount is required OR Invalid amount format

---

### POST /api/daily-cash/{id}/sales

**Description:** Add manual sale to daily cash

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): Daily cash ID

**Request Body:**
```json
{
  "amount": 2500.00,
  "description": "Cash sale"
}
```

**Success Response (200):** "Sale added successfully"

---

### POST /api/daily-cash/{id}/close

**Description:** Close the day by setting closing cash and locking

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): Daily cash ID

**Request Body:**
```json
{
  "closingCash": 15000.00
}
```

**Success Response (200):** "Day closed successfully"

**Error Responses:**
- `400 Bad Request`: closingCash is required OR Invalid closingCash format

---

### PATCH /api/daily-cash/{id}/opening

**Description:** Update opening balance (only if not locked)

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): Daily cash ID

**Request Body:**
```json
{
  "openingCash": 10000.00
}
```

**Success Response (200):** "Opening balance updated"

**Error Responses:**
- `400 Bad Request`: openingCash is required OR Invalid openingCash format OR Day is locked

---

## Transactions

### POST /api/transactions

**Description:** Create a new transaction

**Auth Required:** Bearer token

**Request Body:** Map with transaction properties

**Success Response (200):** "Transaction recorded"

---

### GET /api/transactions/daily

**Description:** Get today's transactions (or by date)

**Auth Required:** Bearer token

**Query Parameters:**
- `department` (String, optional): Filter by department (CAFE, BOOKSHOP, FOODHUT, etc.)
- `category` (String, optional): Filter by category (EXPENSE, SALE)
- `date` (String, optional): Date in YYYY-MM-DD format

**Success Response (200):** Array of OSD_TransactionResponse objects

**Example Response:**
```json
[
  {
    "id": 456,
    "itemName": "Supplies",
    "category": "EXPENSE",
    "amount": 1500.00,
    "department": "CAFE",
    "expenseTypeName": "Office Supplies",
    "comment": "Monthly supplies",
    "transactionTime": "2026-02-02T10:30:00Z",
    "recordedBy": "John Doe",
    "canEdit": true,
    "canDelete": true
  }
]
```

---

### GET /api/transactions/by-date

**Description:** Get transactions by specific date

**Auth Required:** Bearer token

**Query Parameters:**
- `date` (String, required): Date in YYYY-MM-DD format
- `department` (String, optional): Filter by department
- `category` (String, optional): Filter by category

**Success Response (200):** Array of OSD_TransactionResponse objects

---

### GET /api/transactions/department-summary

**Description:** Get department summary for a date

**Auth Required:** Bearer token

**Query Parameters:**
- `department` (String, required): Department name
- `date` (String, optional): Date in YYYY-MM-DD format (defaults to today)

**Success Response (200):**
```json
{
  "department": "CAFE",
  "date": "2026-02-02",
  "openingBalance": 10000.00,
  "closingBalance": 15000.00,
  "totalExpenses": 2000.00,
  "totalCredits": 500.00,
  "calculatedSales": 7500.00,
  "profit": 900.00,
  "profitPercentage": 0.12,
  "profitMargin": 12.0,
  "expenseItems": [...],
  "creditItems": [...],
  "salesItems": [...],
  "expenseBreakdown": [...]
}
```

---

### GET /api/transactions/department-cash-total

**Description:** Get current cash total for a department

**Auth Required:** Bearer token

**Query Parameters:**
- `department` (String, required): Department name

**Success Response (200):**
```json
{
  "department": "CAFE",
  "cashTotal": 15000.00,
  "lastUpdated": "2026-02-02T18:00:00Z"
}
```

---

### PUT /api/transactions/{id}

**Description:** Update a transaction (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role enforced in controller)

**Path Parameters:**
- `id` (Long): Transaction ID

**Request Body:** OSD_TransactionUpdateRequest object

**Success Response (200):** OSD_TransactionResponse object

**Error Responses:**
- `401 Unauthorized`: Invalid user
- `403 Forbidden`: Only SUPERADMIN can edit transactions
- `400 Bad Request`: Validation error

---

### DELETE /api/transactions/{id}

**Description:** Delete a transaction (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role enforced in controller)

**Path Parameters:**
- `id` (Long): Transaction ID

**Success Response (200):**
```json
{
  "message": "Transaction deleted successfully",
  "id": 456
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid user
- `403 Forbidden`: Only SUPERADMIN can delete transactions

---

## Admin Endpoints

### PUT /api/admin/transactions/{id}

**Description:** Edit cash transaction (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Path Parameters:**
- `id` (Long): Transaction ID

**Request Body:**
```json
{
  "amount": 6000.00,
  "description": "Updated expense description",
  "expenseTypeId": 71
}
```

**Success Response (200):**
```json
{
  "message": "Transaction updated successfully",
  "note": "Daily summary has been recalculated",
  "transaction": { ... }
}
```

---

### DELETE /api/admin/transactions/{id}

**Description:** Delete cash transaction (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Success Response (200):**
```json
{
  "message": "Transaction deleted successfully",
  "note": "Daily summary has been recalculated"
}
```

---

### GET /api/admin/transactions/{id}

**Description:** Get transaction details for editing (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Success Response (200):** Transaction details map

---

### PUT /api/admin/daily-cash/{id}

**Description:** Edit daily cash record (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Request Body:** Map with updates (openingCash, closingCash, etc.)

**Success Response (200):**
```json
{
  "message": "Daily cash updated successfully",
  "note": "Daily summary has been recalculated"
}
```

---

### DELETE /api/admin/daily-cash/{id}

**Description:** Delete daily cash record (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Warning:** This will cascade delete all transactions for that day!

**Success Response (200):**
```json
{
  "message": "Daily cash deleted successfully",
  "warning": "All associated transactions were also deleted"
}
```

---

### POST /api/admin/daily-cash/{id}/recalculate-summary

**Description:** Manually recalculate daily summary (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Success Response (200):**
```json
{
  "message": "Daily summary recalculated successfully",
  "dailyCashId": 123
}
```

---

## Credits

### GET /api/credits

**Description:** Get all credits

**Auth Required:** Bearer token

**Success Response (200):** Array of OSD_CreditDTO objects

---

### GET /api/credits/unpaid

**Description:** Get all unpaid credits

**Auth Required:** Bearer token

**Success Response (200):** Array of OSD_CreditDTO objects

---

### GET /api/credits/filter

**Description:** Filter credits by paid status

**Auth Required:** Bearer token

**Query Parameters:**
- `isPaid` (Boolean, optional): Filter by paid status

**Success Response (200):** Array of OSD_CreditDTO objects

---

### POST /api/credits

**Description:** Add new credit

**Auth Required:** Bearer token

**Request Body:** Map with credit properties

**Success Response (200):** "Credit added successfully"

---

### PATCH /api/credits/{id}

**Description:** Update credit paid status

**Auth Required:** Bearer token

**Path Parameters:**
- `id` (Long): Credit ID

**Request Body:**
```json
{
  "isPaid": true
}
```

**Success Response (200):** "Status updated"

---

### DELETE /api/credits/{id}

**Description:** Delete credit record (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Success Response (200):** "Credit deleted successfully"

---

### PUT /api/credits/{id}/edit

**Description:** Edit credit record (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Request Body:** Map with credit updates

**Success Response (200):** "Credit updated successfully"

---

### GET /api/credits/me

**Description:** Get all credits for current logged-in user

**Auth Required:** Bearer token

**Success Response (200):** Array of OSD_CreditDTO objects

---

### GET /api/credits/me/summary

**Description:** Get credits summary for current user

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "totalCredits": 5000.00,
  "unpaidCredits": 2000.00,
  "paidCredits": 3000.00,
  "userId": 123,
  "userName": "John Doe"
}
```

---

### GET /api/credits/me/total

**Description:** Get total credits (all time) for current user

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "userId": 123,
  "userName": "John Doe",
  "totalCredits": 5000.00
}
```

---

### GET /api/credits/user/{userId}

**Description:** Get all credits for specific user (Admin only)

**Auth Required:** Bearer token (ADMIN or SUPERADMIN role)

**Security:** `@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")`

**Success Response (200):** Array of OSD_CreditDTO objects

---

### GET /api/credits/user/{userId}/summary

**Description:** Get credits summary for specific user (Admin only)

**Auth Required:** Bearer token (ADMIN or SUPERADMIN role)

**Security:** `@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")`

**Success Response (200):**
```json
{
  "totalCredits": 5000.00,
  "unpaidCredits": 2000.00,
  "paidCredits": 3000.00,
  "userId": 123,
  "userName": "John Doe",
  "userEmail": "john@example.com"
}
```

---

### GET /api/credits/user/{userId}/total

**Description:** Get total credits for specific user (Admin only)

**Auth Required:** Bearer token (ADMIN or SUPERADMIN role)

**Security:** `@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")`

**Success Response (200):**
```json
{
  "userId": 123,
  "userName": "John Doe",
  "totalCredits": 5000.00
}
```

---

### GET /api/credits/summary

**Description:** Get unpaid credits summary

**Auth Required:** Bearer token

**Success Response (200):** Array of summary maps

---

### GET /api/credits/outstanding-total

**Description:** Get total outstanding credits

**Auth Required:** Bearer token

**Success Response (200):** Map with outstanding total

---

## Reports

**Note:** All report endpoints require SUPERADMIN role (`@PreAuthorize("hasRole('SUPERADMIN')")`)

### GET /api/reports/bank-deposits

**Description:** Get all bank deposits for date range

**Auth Required:** Bearer token (SUPERADMIN role)

**Query Parameters:**
- `startDate` (String, required): Start date in YYYY-MM-DD format
- `endDate` (String, required): End date in YYYY-MM-DD format

**Success Response (200):** Array of BankDepositDTO objects

---

### GET /api/reports/expenses/daily/{date}

**Description:** Get daily expense report

**Auth Required:** Bearer token (SUPERADMIN role)

**Path Parameters:**
- `date` (String): Date in YYYY-MM-DD format

**Success Response (200):** DailyExpenseReportDTO object

---

### GET /api/reports/expenses/monthly/{year}/{month}

**Description:** Get monthly expense report

**Auth Required:** Bearer token (SUPERADMIN role)

**Path Parameters:**
- `year` (int): Year (e.g., 2026)
- `month` (int): Month (1-12)

**Success Response (200):** MonthlyExpenseReportDTO object

---

### GET /api/reports/expenses/by-category

**Description:** Get expenses grouped by category

**Auth Required:** Bearer token (SUPERADMIN role)

**Query Parameters:**
- `startDate` (String, required): Start date in YYYY-MM-DD format
- `endDate` (String, required): End date in YYYY-MM-DD format

**Success Response (200):** ExpenseByCategoryReportDTO object

---

### GET /api/reports/expenses/by-shop

**Description:** Get expenses grouped by shop

**Auth Required:** Bearer token (SUPERADMIN role)

**Query Parameters:**
- `startDate` (String, required): Start date in YYYY-MM-DD format
- `endDate` (String, required): End date in YYYY-MM-DD format

**Success Response (200):** ExpenseByShopReportDTO object

---

### GET /api/reports/business-summary

**Description:** Get overall business performance summary

**Auth Required:** Bearer token (SUPERADMIN role)

**Success Response (200):** BusinessSummaryDTO object

---

### GET /api/reports/staff-performance

**Description:** Get staff performance metrics (optimized using daily_summaries)

**Auth Required:** Bearer token (SUPERADMIN role)

**Query Parameters:**
- `startDate` (String, required): Start date in YYYY-MM-DD format
- `endDate` (String, required): End date in YYYY-MM-DD format

**Success Response (200):** Staff performance data

---

### GET /api/reports/shop-performance/{shopId}

**Description:** Get shop performance metrics (optimized using daily_summaries)

**Auth Required:** Bearer token (SUPERADMIN role)

**Path Parameters:**
- `shopId` (Long): Shop ID

**Query Parameters:**
- `startDate` (String, required): Start date in YYYY-MM-DD format
- `endDate` (String, required): End date in YYYY-MM-DD format

**Success Response (200):** Shop performance data

---

## Audit Logs

**Note:** All audit log endpoints require SUPERADMIN role (`@PreAuthorize("hasRole('SUPERADMIN')")`)

### GET /api/audit-logs

**Description:** Get all audit logs

**Auth Required:** Bearer token (SUPERADMIN role)

**Success Response (200):** Array of audit log maps

**Example Response:**
```json
[
  {
    "id": 789,
    "entityType": "DAILY_CASH",
    "entityId": 123,
    "action": "UPDATE",
    "userId": 45,
    "userName": "Admin User",
    "timestamp": "2026-02-02T14:30:00Z",
    "data": {
      "old": {"openingCash": 10000.00},
      "new": {"openingCash": 12000.00}
    }
  }
]
```

**Dashboard Usage:** Use this to show audit trails for critical operations

---

### GET /api/audit-logs/entity/{entityType}/{entityId}

**Description:** Get audit logs for specific entity (e.g., DAILY_CASH opening balance history)

**Auth Required:** Bearer token (SUPERADMIN role)

**Path Parameters:**
- `entityType` (String): Entity type (e.g., DAILY_CASH, TRANSACTION, CREDIT)
- `entityId` (Long): Entity ID

**Success Response (200):** Array of audit log maps

**Dashboard Usage:** Use `/api/audit-logs/entity/DAILY_CASH/{dailyCashId}` to show opening balance change history with timestamps and users

---

### GET /api/audit-logs/user/{userId}

**Description:** Get audit logs for specific user's actions

**Auth Required:** Bearer token (SUPERADMIN role)

**Path Parameters:**
- `userId` (Long): User ID

**Success Response (200):** Array of audit log maps

---

### GET /api/audit-logs/filter

**Description:** Filter audit logs by entity type or action

**Auth Required:** Bearer token (SUPERADMIN role)

**Query Parameters:**
- `entityType` (String, optional): Entity type filter
- `action` (String, optional): Action filter (CREATE, UPDATE, DELETE)

**Success Response (200):** Array of audit log maps

---

## Salary

### GET /api/salary/today

**Description:** Get today's salary calculation for current user

**Auth Required:** Bearer token

**Success Response (200):** Map with salary calculation

---

### GET /api/salary/admin/monthly

**Description:** Get monthly staff salary report (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Query Parameters:**
- `year` (int, required): Year
- `month` (int, required): Month (1-12)

**Success Response (200):** Array of StaffSalaryReport objects

---

### GET /api/salary/me/monthly

**Description:** Get monthly salary for current user

**Auth Required:** Bearer token

**Query Parameters:**
- `year` (int, required): Year
- `month` (int, required): Month (1-12)

**Success Response (200):** Map with salary calculation

---

### GET /api/salary/user/{userId}/monthly

**Description:** Get monthly salary for specific user (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Path Parameters:**
- `userId` (Long): User ID

**Query Parameters:**
- `year` (int, required): Year
- `month` (int, required): Month (1-12)

**Success Response (200):** Map with salary calculation

---

## Expense Types

### POST /api/expense-types

**Description:** Add new expense type

**Auth Required:** Bearer token

**Alternate URL:** `/api/expenses/types`

**Request Body:**
```json
{
  "name": "Office Supplies",
  "shopType": "CAFE"
}
```

**Success Response (200):** Created ExpenseType object

**Error Responses:**
- `400 Bad Request`: Expense type name is required OR Shop type is required

---

### GET /api/expense-types

**Description:** Get all expense types (with optional filter)

**Auth Required:** Bearer token

**Alternate URL:** `/api/expenses/types`

**Query Parameters:**
- `shopType` (String, optional): Filter by shop type
- `department` (String, optional): Filter by department (alias for shopType)

**Success Response (200):** Array of ExpenseType objects

---

## Foodhut

### POST /api/items

**Description:** Add menu item

**Auth Required:** Bearer token

**Request Body:**
```json
{
  "name": "Cappuccino",
  "variations": [
    {
      "variation": "Small",
      "price": 250,
      "cost": 100
    }
  ]
}
```

**Success Response (200):** Empty response

---

### GET /api/items

**Description:** Get all menu items

**Auth Required:** Bearer token

**Success Response (200):** Array of FoodhutItem objects

---

### POST /api/sales

**Description:** Record a sale

**Auth Required:** Bearer token

**Request Body:**
```json
{
  "variationId": 1,
  "preparedQty": 10,
  "remainingQty": 2,
  "actionType": "SALE"
}
```

**Success Response (200):** Empty response

---

### GET /api/sales/day

**Description:** Get sales for a day

**Auth Required:** Bearer token

**Query Parameters:**
- `date` (String, optional): Date in YYYY-MM-DD format (defaults to today)

**Success Response (200):** Array of Foodhut_TransactionResponse objects

---

### GET /api/sales/day/summary

**Description:** Get day summary

**Auth Required:** Bearer token

**Query Parameters:**
- `date` (String, optional): Date in YYYY-MM-DD format

**Success Response (200):** Foodhut_DaySummaryResponse object

---

### GET /api/sales/remaining/list

**Description:** Get remaining items for day

**Auth Required:** Bearer token

**Query Parameters:**
- `date` (String, optional): Date in YYYY-MM-DD format

**Success Response (200):** Array of Foodhut_RemainingItemDto objects

---

## Ideas & Improvements

### POST /api/messages/idea

**Description:** Submit new idea

**Auth Required:** Bearer token

**Request Body:**
```json
{
  "message": "My great idea",
  "userId": 123
}
```

**Success Response (200):**
```json
{
  "message": "Idea submitted successfully"
}
```

---

### GET /api/messages/idea

**Description:** Get all ideas

**Auth Required:** Bearer token

**Alternate URL:** `/api/messages/ideas`

**Success Response (200):** Array of IdeaOfTheWeek objects

---

### DELETE /api/messages/idea/{id}

**Description:** Delete idea

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "message": "Idea deleted successfully"
}
```

---

### GET /api/messages/ideas/summary

**Description:** Get ideas summary statistics

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "latestIdea": { ... },
  "ideasThisWeek": 5,
  "totalIdeas": 42,
  "uniqueUsers": 12,
  "weekStart": "2026-01-27T00:00:00Z"
}
```

---

### GET /api/messages/ideas/latest

**Description:** Get latest idea

**Auth Required:** Bearer token

**Success Response (200):** IdeaOfTheWeek object

---

### GET /api/messages/ideas/week/{userId}

**Description:** Get user's ideas for current week

**Auth Required:** Bearer token

**Path Parameters:**
- `userId` (Long): User ID

**Success Response (200):** Array of IdeaOfTheWeek objects

---

### POST /api/messages/improvement

**Description:** Submit improvement suggestion

**Auth Required:** Bearer token

**Request Body:**
```json
{
  "message": "My improvement suggestion",
  "userId": 123
}
```

**Success Response (200):**
```json
{
  "message": "Improvement submitted successfully"
}
```

---

### GET /api/messages/improvement

**Description:** Get all improvements

**Auth Required:** Bearer token

**Success Response (200):** Array of Improvement objects

---

### DELETE /api/messages/improvement/{id}

**Description:** Delete improvement

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "message": "Improvement deleted successfully"
}
```

---

### GET /api/messages/improvement/summary

**Description:** Get improvements summary statistics

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "latestImprovement": { ... },
  "improvementsThisWeek": 3,
  "totalImprovements": 28,
  "uniqueUsers": 8,
  "weekStart": "2026-01-27T00:00:00Z"
}
```

---

### GET /api/messages/improvement/latest

**Description:** Get latest improvement

**Auth Required:** Bearer token

**Success Response (200):** Improvement object

---

### GET /api/messages/improvement/week/{userId}

**Description:** Get user's improvements for current week

**Auth Required:** Bearer token

**Path Parameters:**
- `userId` (Long): User ID

**Success Response (200):** Array of Improvement objects

---

## Migration

**Warning:** These endpoints are for one-time data migration and maintenance. Use with caution.

### POST /api/admin/migration/init-shops

**Description:** Initialize shop data

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "message": "Shops initialized successfully"
}
```

---

### POST /api/admin/migration/migrate-transactions

**Description:** Migrate old shop_transactions to new structure

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "message": "Transactions migrated successfully"
}
```

---

### POST /api/admin/migration/update-credits

**Description:** Update credits with shop references

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "message": "Credits updated successfully"
}
```

---

### POST /api/admin/migration/run-full

**Description:** Run complete migration (all steps)

**Auth Required:** Bearer token

**Success Response (200):**
```json
{
  "message": "Full migration completed successfully",
  "note": "Please verify data and update application.properties if needed"
}
```

---

### POST /api/admin/migration/backfill-summaries

**Description:** Backfill daily summaries for date range (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Query Parameters:**
- `startDate` (String, required): Start date in YYYY-MM-DD format
- `endDate` (String, required): End date in YYYY-MM-DD format

**Success Response (200):**
```json
{
  "message": "Daily summaries backfilled successfully",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "note": "Performance optimization enabled - reports will now be 10-100x faster!"
}
```

---

### POST /api/admin/migration/backfill-summaries-all

**Description:** Backfill all summaries for last 3 months (SUPERADMIN only)

**Auth Required:** Bearer token (SUPERADMIN role)

**Security:** `@PreAuthorize("hasRole('SUPERADMIN')")`

**Success Response (200):**
```json
{
  "message": "All daily summaries backfilled successfully",
  "startDate": "2025-11-02",
  "endDate": "2026-02-02",
  "note": "Performance optimization enabled - reports will now be 10-100x faster!"
}
```

---

## Common DTOs

### AttendanceAdjustmentRequest
```json
{
  "overtimeHours": 2.0,
  "deductionHours": 0.5,
  "overtimeReason": "Extra project work",
  "deductionReason": "Late arrival"
}
```

### DailyCashSummaryDTO
```json
{
  "dailyCashId": 123,
  "shopId": 1,
  "shopCode": "CAFE",
  "shopName": "Main Cafe",
  "businessDate": "2026-02-02",
  "openingCash": 10000.00,
  "openingConfirmed": true,
  "closingCash": 15000.00,
  "locked": false,
  "closedByName": null,
  "totalExpenses": 2000.00,
  "manualSales": 500.00,
  "totalCredits": 300.00,
  "totalSales": 7800.00,
  "variance": 0.00,
  "expenses": [...],
  "sales": [...],
  "credits": [...]
}
```

### LatestBalanceDTO
```json
{
  "closingBalance": 12500.50,
  "date": "2026-02-01",
  "shopId": 1
}
```

### User
```json
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "SUPERADMIN"
}
```

### Attendance
```json
{
  "id": 456,
  "user": { ... },
  "workDate": "2026-02-02",
  "status": "WORKING",
  "isWorking": true,
  "overtimeHours": 0.0,
  "deductionHours": 0.0,
  "overtimeReason": null,
  "deductionReason": null
}
```

---

## Common Response Patterns

### Success Response
Most endpoints return the requested data directly with HTTP 200.

### Error Response
```json
{
  "error": "Error message description"
}
```

### Message Response
```json
{
  "message": "Operation completed successfully"
}
```

---

## Authentication Flow

1. Call `POST /api/auth/login` with credentials
2. Store the returned `token` and `userId`
3. Include token in all subsequent requests: `Authorization: Bearer {token}`
4. Token contains role information - server validates role permissions

---

## Dashboard Integration Tips

### Opening Balance Audit Trail
```
GET /api/audit-logs/entity/DAILY_CASH/{dailyCashId}
```
Display a timeline showing who changed opening balance and when.

### Prefill Opening Balance
```
GET /api/daily-cash/{shopId}/latest-closing-balance?daysBack=7
```
Use the returned `closingBalance` to prefill the opening balance field.

### Real-time Department Cash
```
GET /api/transactions/department-cash-total?department=CAFE
```
Display current cash position per department.

### Staff Performance Dashboard
```
GET /api/reports/staff-performance?startDate=2026-01-01&endDate=2026-01-31
```
Requires SUPERADMIN. Uses optimized daily_summaries for fast loading.

### User Credits Widget
```
GET /api/credits/me/summary
```
Show total/paid/unpaid credits for logged-in user.

---

## Error Codes Reference

- **200 OK**: Successful request
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid input or validation error
- **401 Unauthorized**: Missing or invalid authentication token
- **403 Forbidden**: Insufficient permissions (role check failed)
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

---

## Notes

- All dates use ISO 8601 format: `YYYY-MM-DD`
- All timestamps use ISO 8601 format: `YYYY-MM-DDTHH:mm:ssZ`
- Timezone handling: Server uses UTC internally, accepts `Asia/Colombo` timezone parameter where applicable
- Role hierarchy: `SUPERADMIN` > `ADMIN` > `USER` > `CUSTOMER`
- Audit logs are automatically created for critical operations (opening/closing balance changes, transaction edits/deletes, credit changes)

---

**End of Documentation**

For questions or issues, refer to the source code:
- Controllers: `src/main/java/com/oss/controller/`
- DTOs: `src/main/java/com/oss/dto/`
- Models: `src/main/java/com/oss/model/`
