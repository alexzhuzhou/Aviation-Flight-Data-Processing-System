# Database Backup

This folder contains a pre-populated MongoDB backup with sample flight data.

## aviation_db_backup.tar

**Contents:**
- **1,214 flights** from date **07/11/2025**
- Complete flight data with tracking points
- Predicted flight data for analysis
- Processing history records

**Why use this backup:**
This data took approximately **6 days to retrieve** from the Oracle database. Using this backup allows you to:
- Skip the lengthy data extraction process
- Start testing and analysis immediately
- Work with a complete dataset for development

## How to Use

### 1. Stop the current MongoDB container
```bash
docker stop aviation_mongodb
docker rm aviation_mongodb
```

### 2. Extract and restore the backup
```bash
# Extract the backup
tar -xf aviation_db_backup.tar

# Start new MongoDB container
docker run -d \
  --name aviation_mongodb \
  -p 27017:27017 \
  -v $(pwd)/aviation_db_backup:/backup \
  mongo:latest

# Restore the database
docker exec -it aviation_mongodb mongorestore /backup
```

### 3. Verify the data
```bash
# Check collections
docker exec -it aviation_mongodb mongosh aviation_db --eval "show collections"

# Count flights
docker exec -it aviation_mongodb mongosh aviation_db --eval "db.flights.countDocuments()"

# Count predicted flights
docker exec -it aviation_mongodb mongosh aviation_db --eval "db.predicted_flights.countDocuments()"
```

## Data Overview

- **Date Range**: 07/11/2025 (single day)
- **Total Flights**: ~1,214 flights
- **Collections**: flights, predicted_flights, processing_history
- **Use Case**: Development, testing, analysis without waiting for data extraction
