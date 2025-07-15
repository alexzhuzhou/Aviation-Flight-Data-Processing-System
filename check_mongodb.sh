#!/bin/bash

echo "🗄️  MongoDB Database Check"
echo "=========================="

# Check if MongoDB container is running
echo "1️⃣ Checking MongoDB container status..."
if docker ps | grep -q mongodb; then
    echo "✅ MongoDB container is running"
else
    echo "❌ MongoDB container is not running"
    exit 1
fi

echo ""
echo "2️⃣ Database Statistics:"
echo "======================="

# Get database statistics
docker exec mongodb mongosh --quiet --eval "
use aviation_db;
print('Database: aviation_db');
print('Collections:');
show collections;
print('');
print('Flight Statistics:');
var stats = db.flights.aggregate([
  {
    \$group: {
      _id: null,
      totalFlights: { \$sum: 1 },
      flightsWithTracking: { \$sum: { \$cond: ['\$hasTrackingData', 1, 0] } },
      totalTrackingPoints: { \$sum: '\$totalTrackingPoints' }
    }
  }
]).toArray();
if (stats.length > 0) {
  print('Total Flights: ' + stats[0].totalFlights);
  print('Flights with Tracking: ' + stats[0].flightsWithTracking);
  print('Total Tracking Points: ' + stats[0].totalTrackingPoints);
} else {
  print('No flights found in database');
}
"

echo ""
echo "3️⃣ Sample Flight Data:"
echo "======================"

# Show sample flights
docker exec mongodb mongosh --quiet --eval "
use aviation_db;
print('Sample flights:');
db.flights.find().limit(3).forEach(function(flight) {
  print('  Flight: ' + flight.indicative + 
        ' | Aircraft: ' + flight.aircraftType + 
        ' | Airline: ' + flight.airline + 
        ' | Tracking Points: ' + flight.totalTrackingPoints);
});
"

echo ""
echo "4️⃣ Recent Activity:"
echo "==================="

# Show recent flights by creation time (if available)
docker exec mongodb mongosh --quiet --eval "
use aviation_db;
print('Recent flights (by _id):');
db.flights.find().sort({_id: -1}).limit(3).forEach(function(flight) {
  print('  ' + flight.indicative + ' - ' + flight.aircraftType + ' (' + flight.airline + ')');
});
"

echo ""
echo "✅ Database check complete!"
echo ""
echo "💡 To explore more:"
echo "   docker exec -it mongodb mongosh"
echo "   use aviation_db"
echo "   db.flights.find().pretty()" 