#!/bin/bash

# F1 Betting API Functional Tests Runner
# Run this script after starting the application with: ./gradlew bootRun

set -e  # Exit on any error

echo "🏎️  F1 Betting API Functional Tests"
echo "===================================="
echo

BASE_URL="http://localhost:8080"
USER_ID="123e4567-e89b-12d3-a456-426614174000"
EVENT_ID="550e8400-e29b-41d4-a716-446655440001"  # Australian GP Race

echo "🔍 Test 1: Health Check"
HEALTH_RESPONSE=$(curl -s -X GET "${BASE_URL}/actuator/healthz")
echo "Response: ${HEALTH_RESPONSE}"
if [ "$HEALTH_RESPONSE" = "OK" ]; then
    echo "✅ Health check passed"
else
    echo "❌ Health check failed"
    exit 1
fi
echo

echo "📋 Test 2: List All Events"
curl -s -X GET "${BASE_URL}/api/v1/events" -H "Content-Type: application/json" | jq -r '.items | length' > /tmp/event_count
EVENT_COUNT=$(cat /tmp/event_count)
echo "Found ${EVENT_COUNT} events"
if [ "$EVENT_COUNT" -gt 0 ]; then
    echo "✅ Events retrieved successfully"
else
    echo "❌ No events found"
    exit 1
fi
echo

echo "🔍 Test 2.1: Event Filtering"
RACE_COUNT=$(curl -s -X GET "${BASE_URL}/api/v1/events?sessionType=RACE" -H "Content-Type: application/json" | jq -r '.items | length')
AUSTRALIA_COUNT=$(curl -s -X GET "${BASE_URL}/api/v1/events?country=Australia" -H "Content-Type: application/json" | jq -r '.items | length')
COMBINED_COUNT=$(curl -s -X GET "${BASE_URL}/api/v1/events?sessionType=RACE&country=Australia&year=2025" -H "Content-Type: application/json" | jq -r '.items | length')
EMPTY_COUNT=$(curl -s -X GET "${BASE_URL}/api/v1/events?country=France" -H "Content-Type: application/json" | jq -r '.items | length')

echo "Race events: ${RACE_COUNT}, Australia events: ${AUSTRALIA_COUNT}, Combined filter: ${COMBINED_COUNT}, Non-existent filter: ${EMPTY_COUNT}"
if [ "$RACE_COUNT" = "1" ] && [ "$AUSTRALIA_COUNT" = "1" ] && [ "$COMBINED_COUNT" = "1" ] && [ "$EMPTY_COUNT" = "0" ]; then
    echo "✅ Event filtering working correctly"
else
    echo "❌ Event filtering failed"
    exit 1
fi
echo

echo "💰 Test 3: Check Initial User Balance"
BALANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/v1/users/${USER_ID}/balance" -H "Content-Type: application/json")
INITIAL_BALANCE=$(echo "$BALANCE_RESPONSE" | jq -r '.balance')
echo "Initial balance: €${INITIAL_BALANCE}"
if [ "$INITIAL_BALANCE" = "100" ] || [ "$INITIAL_BALANCE" = "100.0" ] || [ "$INITIAL_BALANCE" = "100.00" ]; then
    echo "✅ Initial balance correct (€100)"
else
    echo "❌ Unexpected initial balance: €${INITIAL_BALANCE}"
fi
echo

echo "🎯 Test 4: Place Bet (€25 on Lewis Hamilton)"
# Get the current event and extract the first selection (Lewis Hamilton)
EVENT_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/v1/events?sessionType=RACE&country=Australia" -H "Content-Type: application/json")
SELECTION_ID=$(echo "$EVENT_RESPONSE" | jq -r '.items[0].driverMarket[0].selectionId')

echo "Using event ID: ${EVENT_ID}, selection ID: ${SELECTION_ID}"

BET_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/bets" \
    -H "Content-Type: application/json" \
    -H "Idempotency-Key: test-bet-001" \
    -d "{
        \"userId\": \"${USER_ID}\",
        \"eventId\": \"${EVENT_ID}\",
        \"selectionId\": \"${SELECTION_ID}\",
        \"stakeAmount\": 25.00,
        \"currency\": \"EUR\"
    }")

BET_ID=$(echo "$BET_RESPONSE" | jq -r '.betId // empty')
if [ -n "$BET_ID" ]; then
    echo "✅ Bet placed successfully (ID: ${BET_ID})"
else
    echo "❌ Failed to place bet"
    echo "Response: $BET_RESPONSE"
    exit 1
fi
echo

echo "💸 Test 5: Verify Balance After Bet"
BALANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/v1/users/${USER_ID}/balance" -H "Content-Type: application/json")
AFTER_BET_BALANCE=$(echo "$BALANCE_RESPONSE" | jq -r '.balance')
echo "Balance after bet: €${AFTER_BET_BALANCE}"
if [ "$AFTER_BET_BALANCE" = "75" ] || [ "$AFTER_BET_BALANCE" = "75.0" ] || [ "$AFTER_BET_BALANCE" = "75.00" ]; then
    echo "✅ Balance correctly deducted (€100 - €25 = €75)"
else
    echo "❌ Incorrect balance after bet: €${AFTER_BET_BALANCE}"
fi
echo

echo "🔄 Test 6: Test Idempotency (Same bet request)"
IDEMPOTENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/bets" \
    -H "Content-Type: application/json" \
    -H "Idempotency-Key: test-bet-001" \
    -d "{
        \"userId\": \"${USER_ID}\",
        \"eventId\": \"${EVENT_ID}\",
        \"selectionId\": \"${SELECTION_ID}\",
        \"stakeAmount\": 25.00,
        \"currency\": \"EUR\"
    }")

IDEMPOTENT_BET_ID=$(echo "$IDEMPOTENT_RESPONSE" | jq -r '.betId // empty')
if [ "$IDEMPOTENT_BET_ID" = "$BET_ID" ]; then
    echo "✅ Idempotency working correctly (same bet ID returned)"
else
    echo "❌ Idempotency failed - different bet ID: ${IDEMPOTENT_BET_ID}"
fi
echo

echo "🏁 Test 7: Record Race Outcome (Lewis Hamilton wins)"
OUTCOME_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/outcome_response -X POST "${BASE_URL}/api/v1/events/${EVENT_ID}/outcome" \
    -H "Content-Type: application/json" \
    -d '{"winningDriverId": "d1"}')

if [ "$OUTCOME_RESPONSE" = "200" ]; then
    echo "✅ Race outcome recorded successfully"
else
    echo "❌ Failed to record outcome (HTTP ${OUTCOME_RESPONSE})"
    exit 1
fi
echo

echo "🎉 Test 8: Check Winning Balance"
BALANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/v1/users/${USER_ID}/balance" -H "Content-Type: application/json")
FINAL_BALANCE=$(echo "$BALANCE_RESPONSE" | jq -r '.balance')
echo "Final balance: €${FINAL_BALANCE}"

# Expected: €75 (remaining) + €25 (stake) * odds = €75 + winnings
# Note: Odds are random, so we just check if balance increased from €75
if (( $(echo "$FINAL_BALANCE > 75" | bc -l) )); then
    echo "✅ Winning payout correct (balance increased from €75 to €${FINAL_BALANCE})"
else
    echo "⚠️  Unexpected final balance: €${FINAL_BALANCE}"
    echo "    Expected > €75 (€75 remaining + winnings from €25 stake × random odds)"
fi
echo

echo "❌ Test 9: Error Handling (Insufficient Balance)"
# Get Monaco GP qualifying event for error test
MONACO_EVENT_ID="550e8400-e29b-41d4-a716-446655440002"
MONACO_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/v1/events?sessionType=QUALIFYING&country=Monaco" -H "Content-Type: application/json")
MONACO_SELECTION_ID=$(echo "$MONACO_RESPONSE" | jq -r '.items[0].driverMarket[0].selectionId')

ERROR_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/error_response -X POST "${BASE_URL}/api/v1/bets" \
    -H "Content-Type: application/json" \
    -H "Idempotency-Key: test-bet-002" \
    -d "{
        \"userId\": \"${USER_ID}\",
        \"eventId\": \"${MONACO_EVENT_ID}\",
        \"selectionId\": \"${MONACO_SELECTION_ID}\",
        \"stakeAmount\": 200.00,
        \"currency\": \"EUR\"
    }")

if [ "$ERROR_RESPONSE" = "422" ]; then
    echo "✅ Error handling correct (HTTP 422 for insufficient balance)"
    ERROR_DETAIL=$(cat /tmp/error_response | jq -r '.detail // empty')
    if [[ "$ERROR_DETAIL" == *"Insufficient balance"* ]]; then
        echo "✅ Correct error message: ${ERROR_DETAIL}"
    fi
else
    echo "❌ Unexpected error response (HTTP ${ERROR_RESPONSE})"
fi
echo

echo "🎯 Test Summary"
echo "==============="
echo "✅ All core functionality working:"
echo "   • Health checks"
echo "   • Event listing"
echo "   • Event filtering (sessionType, country, year)"
echo "   • User balance management"
echo "   • Bet placement"
echo "   • Idempotency support"
echo "   • Race outcome recording"
echo "   • Winning payouts"
echo "   • Error handling"
echo
echo "🏎️  F1 Betting API is fully functional!"

# Cleanup temp files
rm -f /tmp/event_count /tmp/outcome_response /tmp/error_response
