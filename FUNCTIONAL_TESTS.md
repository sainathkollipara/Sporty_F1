# F1 Betting API - Functional Testing Guide

This guide provides functional tests for the F1 Betting API using cURL commands. These tests demonstrate the complete betting workflow and validate all API endpoints.

## 🔄 Recent Updates

**✅ Event Filtering Fixed** (Aug 2025)
- **Issue**: Event filtering by query parameters (`sessionType`, `country`, `year`) was not working correctly
- **Root Cause**: `StubF1ProviderAdapter` was returning all sessions regardless of filter parameters
- **Solution**: Implemented proper filtering logic using Java streams with AND logic
- **Result**: Query parameters now work correctly - you can filter by session type, country, year, or combinations
- **Bonus Fix**: Event persistence issue resolved - selection IDs are now consistent across API calls

## Prerequisites
- Application running on `http://localhost:8080`
- `curl` and `jq` installed for JSON formatting

## Start the Application
```bash
./gradlew bootRun
```

---

## Test Suite Overview

| Test | Endpoint | Purpose |
|------|----------|---------|
| 1 | Health Check | Verify application is running |
| 2 | List Events | Get all available F1 events |
| 3 | Event Filtering | Test query parameters (✅ **FIXED**) |
| 4 | User Balance | Create user and check initial balance |
| 5 | Place Bet | Place a valid bet |
| 6 | Balance After Bet | Verify balance deduction |
| 7 | Idempotency Test | Duplicate bet with same key |
| 8 | Record Outcome | Set race winner |
| 9 | Winning Payout | Check balance after winning |
| 10 | Error Handling | Insufficient balance scenario |

---

## Functional Tests

### Test 1: Health Check ✅
**Purpose:** Verify the application is running
```bash
curl -X GET http://localhost:8080/actuator/healthz
```
**Expected:** `OK`

---

### Test 2: List All Events ✅
**Purpose:** Get all available F1 events
```bash
curl -X GET "http://localhost:8080/api/v1/events" \
  -H "Content-Type: application/json" | jq .
```
**Expected:** JSON response with 3 events (Australian GP Race, Monaco GP Qualifying, British GP Practice) with drivers, odds, and selection IDs.

---

### Test 3: List Events with Filters ✅
**Purpose:** Test query parameter filtering
```bash
# Filter by sessionType only (should return 1 race)
curl -X GET "http://localhost:8080/api/v1/events?sessionType=RACE" \
  -H "Content-Type: application/json" | jq .

# Filter by country only (should return 1 Australian event) 
curl -X GET "http://localhost:8080/api/v1/events?country=Australia" \
  -H "Content-Type: application/json" | jq .

# Filter by multiple parameters (should return 1 specific event)
curl -X GET "http://localhost:8080/api/v1/events?sessionType=RACE&country=Australia&year=2025" \
  -H "Content-Type: application/json" | jq .

# Filter with no matches (should return empty array)
curl -X GET "http://localhost:8080/api/v1/events?country=France" \
  -H "Content-Type: application/json" | jq .
```
**Expected:** Filtered results matching the query parameters. Multiple filters work with AND logic.

---

### Test 4: Check User Balance (Creates New User) ✅
**Purpose:** Get user balance (creates user with default €100 if not exists)
```bash
curl -X GET "http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/balance" \
  -H "Content-Type: application/json" | jq .
```
**Expected:** `{"userId": "123e4567-e89b-12d3-a456-426614174000", "balance": 100.00}`

---

### Test 5: Place a Valid Bet ✅
**Purpose:** Place a €25 bet on Lewis Hamilton to win Australian GP
```bash
curl -X POST "http://localhost:8080/api/v1/bets" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-bet-001" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "eventId": "550e8400-e29b-41d4-a716-446655440001",
    "selectionId": "f3c44ead-d00c-49cd-b733-f66930c55d85",
    "stakeAmount": 25.00,
    "currency": "EUR"
  }' | jq .
```
**Expected:** Bet confirmation with `betId`, `status: "PENDING"`, captured odds, etc.

---

### Test 6: Verify Balance After Bet ✅
**Purpose:** Confirm balance was deducted (€100 - €25 = €75)
```bash
curl -X GET "http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/balance" \
  -H "Content-Type: application/json" | jq .
```
**Expected:** `{"userId": "123e4567-e89b-12d3-a456-426614174000", "balance": 75.00}`

---

### Test 7: Test Idempotency ✅
**Purpose:** Duplicate bet request should return same bet without double charging
```bash
curl -X POST "http://localhost:8080/api/v1/bets" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-bet-001" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "eventId": "550e8400-e29b-41d4-a716-446655440001",
    "selectionId": "f3c44ead-d00c-49cd-b733-f66930c55d85",
    "stakeAmount": 25.00,
    "currency": "EUR"
  }' | jq .
```
**Expected:** Same `betId` returned, balance remains at €75.

---

### Test 8: Record Race Outcome ✅
**Purpose:** Set Lewis Hamilton (d1) as race winner
```bash
curl -X POST "http://localhost:8080/api/v1/events/550e8400-e29b-41d4-a716-446655440001/outcome" \
  -H "Content-Type: application/json" \
  -d '{
    "winningDriverId": "d1"
  }'
```
**Expected:** Empty response (HTTP 200 OK)

---

### Test 9: Check Winning Balance ✅
**Purpose:** Verify winning payout (€75 + €25 stake × 3.0 odds = €150)
```bash
curl -X GET "http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/balance" \
  -H "Content-Type: application/json" | jq .
```
**Expected:** `{"userId": "123e4567-e89b-12d3-a456-426614174000", "balance": 150.00}`

---

### Test 10: Error Handling - Insufficient Balance ✅
**Purpose:** Attempt to bet more than available balance
```bash
curl -X POST "http://localhost:8080/api/v1/bets" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-bet-002" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "eventId": "550e8400-e29b-41d4-a716-446655440002",
    "selectionId": "cf9f10d7-257f-49d2-8a68-5715c3a2c330",
    "stakeAmount": 200.00,
    "currency": "EUR"
  }' | jq .
```
**Expected:** `{"status": 422, "title": "Domain validation error", "detail": "Insufficient balance"}`

---

## Complete Test Script

Run all tests in sequence:

```bash
#!/bin/bash

echo "=== F1 Betting API Functional Tests ==="
echo

echo "Test 1: Health Check"
curl -s -X GET http://localhost:8080/actuator/healthz
echo -e "\n"

echo "Test 2: List Events"
curl -s -X GET "http://localhost:8080/api/v1/events" -H "Content-Type: application/json" | jq .
echo

echo "Test 3: Check Initial Balance"
curl -s -X GET "http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/balance" -H "Content-Type: application/json" | jq .
echo

echo "Test 4: Place Bet"
curl -s -X POST "http://localhost:8080/api/v1/bets" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-bet-001" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "eventId": "550e8400-e29b-41d4-a716-446655440001",
    "selectionId": "f3c44ead-d00c-49cd-b733-f66930c55d85",
    "stakeAmount": 25.00,
    "currency": "EUR"
  }' | jq .
echo

echo "Test 5: Check Balance After Bet"
curl -s -X GET "http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/balance" -H "Content-Type: application/json" | jq .
echo

echo "Test 6: Record Outcome"
curl -s -X POST "http://localhost:8080/api/v1/events/550e8400-e29b-41d4-a716-446655440001/outcome" \
  -H "Content-Type: application/json" \
  -d '{"winningDriverId": "d1"}'
echo

echo "Test 7: Check Winning Balance"
curl -s -X GET "http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/balance" -H "Content-Type: application/json" | jq .
echo

echo "=== Tests Complete ==="
```

## API Reference Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/healthz` | Health check |
| GET | `/api/v1/events` | List events (supports filters: sessionType, year, country, page, size) |
| POST | `/api/v1/bets` | Place bet (supports Idempotency-Key header) |
| GET | `/api/v1/users/{userId}/balance` | Get user balance |
| POST | `/api/v1/events/{eventId}/outcome` | Record race outcome |

## Notes

- ✅ **Working correctly:** Health check, event listing, event filtering, betting workflow, balance management, idempotency, outcome recording, error handling
- 🔍 **Event Filtering:** Query parameters work with AND logic - `sessionType`, `country`, `year`, `page`, `size` all supported
- 💰 **Business Logic:** New users start with €100, winning bets pay out (stake × odds), losing bets forfeit the stake
- 🔄 **Idempotency:** Duplicate requests with same `Idempotency-Key` return the same result without side effects
- 🎲 **Odds:** Randomly generated odds between 2.0-4.0 for each driver per session
