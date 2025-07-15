#!/bin/bash

# Test script for the streaming flight data API endpoints
# This script tests the main production endpoint: /api/flights/process-packet

echo "🧪 Testing Streaming Flight Data API Endpoints"
echo "=============================================="

# Base URL
BASE_URL="http://localhost:8080"

# Test 1: Health Check
echo ""
echo "1️⃣ Testing Health Check..."
curl -s "$BASE_URL/api/flights/health"
echo ""

# Test 2: Get Initial Stats
echo ""
echo "2️⃣ Getting Initial Statistics..."
curl -s "$BASE_URL/api/flights/stats" | jq '.' 2>/dev/null || curl -s "$BASE_URL/api/flights/stats"
echo ""

# Test 3: Process a Sample ReplayPath Packet (Production Endpoint)
echo ""
echo "3️⃣ Testing Production Endpoint: /api/flights/process-packet"
echo "Processing sample ReplayPath packet..."

# Sample ReplayPath packet (this is what the external system will send)
SAMPLE_PACKET='{
  "listFlightIntention": [
    {
      "planId": 12345,
      "indicative": "TEST123",
      "aircraftType": "B737",
      "airline": "TEST",
      "eobt": "2025-01-15T10:00:00Z",
      "eta": "2025-01-15T12:00:00Z",
      "finished": false
    }
  ],
  "listRealPath": [
    {
      "planId": 99999,
      "indicativeSafe": "TEST123",
      "flightLevel": 350,
      "trackSpeed": 450,
      "seqNum": 1,
      "simulating": false,
      "kinematic": {
        "position": {
          "latitude": -15.7942,
          "longitude": -47.8822
        }
      }
    }
  ],
  "time": 1752022863073
}'

# Send the packet to the production endpoint
RESPONSE=$(curl -s -X POST "$BASE_URL/api/flights/process-packet" \
  -H "Content-Type: application/json" \
  -d "$SAMPLE_PACKET")

echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

# Test 4: Get Updated Stats
echo ""
echo "4️⃣ Getting Updated Statistics..."
curl -s "$BASE_URL/api/flights/stats" | jq '.' 2>/dev/null || curl -s "$BASE_URL/api/flights/stats"
echo ""

# Test 5: Process Another Packet (Update Existing Flight)
echo ""
echo "5️⃣ Testing Flight Update with Additional Tracking Data..."

UPDATE_PACKET='{
  "listRealPath": [
    {
      "planId": 99999,
      "indicativeSafe": "TEST123",
      "flightLevel": 360,
      "trackSpeed": 460,
      "seqNum": 2,
      "simulating": false,
      "kinematic": {
        "position": {
          "latitude": -15.8000,
          "longitude": -47.8900
        }
      }
    }
  ],
  "time": 1752022864073
}'

RESPONSE2=$(curl -s -X POST "$BASE_URL/api/flights/process-packet" \
  -H "Content-Type: application/json" \
  -d "$UPDATE_PACKET")

echo "Response:"
echo "$RESPONSE2" | jq '.' 2>/dev/null || echo "$RESPONSE2"
echo ""

# Test 6: Final Stats
echo ""
echo "6️⃣ Final Statistics..."
curl -s "$BASE_URL/api/flights/stats" | jq '.' 2>/dev/null || curl -s "$BASE_URL/api/flights/stats"
echo ""

echo ""
echo "✅ Testing Complete!"
echo ""
echo "📋 Summary:"
echo "   - Health check: Should return 'Streaming Flight Service is running'"
echo "   - Process packet: Should create/update flights in MongoDB"
echo "   - Stats: Should show total flights and tracking points"
echo ""
echo "🔍 To verify data in MongoDB:"
echo "   mongo aviation_db"
echo "   db.flights.find().pretty()"
echo "" 