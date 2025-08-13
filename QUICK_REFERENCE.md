# 🚀 Quick API Reference

## Essential Endpoints

### 🔧 Setup & Health
```bash
# Health check
curl http://localhost:8080/api/flights/health

# Test Oracle connection
curl http://localhost:8080/api/flights/test-oracle-connection
```

### 🗄️ Oracle Data Processing
```bash
# Process real flight data from Oracle
curl -X POST http://localhost:8080/api/flights/process-packet

# Get available planIds
curl http://localhost:8080/api/flights/plan-ids
```

### 🎯 Predicted Flights (Oracle-based)
```bash
# Process single planId
curl -X POST http://localhost:8080/api/predicted-flights/process \
  -H "Content-Type: application/json" \
  -d '{"planId": 17879345}'

# Process multiple planIds
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d '{"planIds": [17879345, 17879346, 17879347]}'
```

### 📈 Punctuality Analysis
```bash
# Match flights
curl http://localhost:8080/api/punctuality-analysis/match-flights

# Run full analysis (ICAO KPI14)
curl http://localhost:8080/api/punctuality-analysis/run
```

### 📊 Statistics
```bash
# Flight stats
curl http://localhost:8080/api/flights/stats

# Predicted flight stats  
curl http://localhost:8080/api/predicted-flights/stats

# Analysis stats
curl http://localhost:8080/api/punctuality-analysis/stats
```

## 🔄 Complete Workflow

```bash
# 1. Test connection
curl http://localhost:8080/api/flights/test-oracle-connection

# 2. Process Oracle data
curl -X POST http://localhost:8080/api/flights/process-packet

# 3. Get planIds
curl http://localhost:8080/api/flights/plan-ids

# 4. Process predictions (example)
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d '{"planIds": [17879345, 17879346, 17879347]}'

# 5. Run analysis
curl http://localhost:8080/api/punctuality-analysis/run
```

## 📝 Key Features

- ✅ **Oracle Integration**: Direct database access, no JSON files needed
- ✅ **Option A Strategy**: Skip missing planIds, detailed reporting
- ✅ **Batch Processing**: Efficient handling of multiple planIds
- ✅ **Performance Metrics**: Timing information in all responses
- ✅ **ICAO KPI14**: Full punctuality analysis compliance

## 📖 Full Documentation

- [API_USAGE_GUIDE.md](API_USAGE_GUIDE.md) - Complete examples with responses
- [README.md](README.md) - Full project documentation
- [SETUP.md](SETUP.md) - Setup instructions
