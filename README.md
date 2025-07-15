# Aviation Replay Data Processor

A Java application for processing and analyzing aviation tracking replay data using Maven for build management.

## Overview

This application processes aviation replay data from JSON files, specifically designed to work with data containing:
- **Real Path Points** (`listRealPath`) - Real-time aircraft tracking data
- **Flight Intentions** (`listFlightIntention`) - Planned flight schedules and information
- **Timestamp** - Global reference time for the dataset

## Features

- **Data Loading**: Parse large JSON replay files efficiently
- **Statistical Analysis**: Generate summaries and statistics about flight data
- **Interactive Exploration**: Search flights, analyze tracking points, and explore correlations
- **Real-time Tracking Analysis**: Analyze flight levels, speeds, detector sources, and control sectors
- **Flight Planning Analysis**: Examine aircraft types, airlines, RVSM capabilities, and route information
- **Data Correlation**: Match flight intentions with actual tracking data

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- A `replay.json` file in the project root directory

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/example/
│   │   │   │   ├── App.java              # Main application
│   │   │   │   ├── model/                # Data models
│   │   │   │   │   ├── ReplayData.java
│   │   │   │   │   ├── RealPathPoint.java
│   │   │   │   │   ├── FlightIntention.java
│   │   │   │   │   └── Kinematic.java
│   │   │   │   └── service/              # Business logic
│   │   │   │       └── ReplayDataService.java
│   │   │   └── resources/                # Application resources
│   │   └── test/
│   │       ├── java/                     # Test source code
│   │       └── resources/                # Test resources
├── pom.xml                               # Maven configuration
├── replay.json                           # Aviation data file
└── README.md                            # This file
```

## Building the Project

To compile the project:
```bash
mvn compile
```

To run tests:
```bash
mvn test
```

To build the JAR file:
```bash
mvn package
```

## Running the Application

Make sure you have a `replay.json` file in the project root, then run:

```bash
mvn exec:java -Dexec.mainClass="com.example.App"
```

Or after building the JAR:
```bash
java -cp target/java-project-1.0.0.jar com.example.App
```

## Application Features

### Automatic Analysis
When you run the application, it will automatically:
1. Load and parse the replay.json file
2. Display a data summary (timestamp, number of records)
3. Analyze real path tracking data (flight levels, speeds, detector sources)
4. Analyze flight intentions (aircraft types, airlines, RVSM capabilities)
5. Show correlations between planned and actual flight data
6. Display sample data for inspection

### Interactive Menu
The application provides an interactive menu with options to:
1. **Search flights by call sign** - Find specific flights by their call sign
2. **Get tracking points for a flight plan** - View detailed tracking data for a specific plan ID
3. **Show data summary** - Display basic statistics about the dataset
4. **Analyze real path data** - Detailed analysis of tracking points
5. **Analyze flight intentions** - Detailed analysis of flight plans
6. **Show sample data** - Display sample records for inspection

### Data Models

The application uses strongly-typed Java models that map to the JSON structure:

- **ReplayData**: Main container with listRealPath, listFlightIntention, and timestamp
- **RealPathPoint**: Individual tracking points with position, speed, flight level, etc.
- **FlightIntention**: Planned flight data with call signs, aircraft types, routes, etc.
- **Kinematic**: Position and movement data including lat/lon coordinates

## Dependencies

- **Jackson**: For JSON parsing and data binding
- **JUnit 5**: For testing
- **Java Time API**: For timestamp handling

## Sample Usage

```java
// Load replay data
ReplayDataService service = new ReplayDataService();
ReplayData data = service.loadReplayData("replay.json");

// Get basic statistics
service.printDataSummary(data);

// Find flights by call sign
List<FlightIntention> flights = service.findFlightsByCallSign(data, "GLO");

// Get tracking points for a flight
List<RealPathPoint> points = service.getTrackingPointsForPlan(data, 12345);
```

## Data Format

The application expects JSON data with this structure:
```json
{
  "listRealPath": [
    {
      "planId": 12345,
      "flightLevel": 350,
      "trackSpeed": 450,
      "kinematic": {
        "position": {
          "latitude": -15.7942,
          "longitude": -47.8822
        }
      }
    }
  ],
  "listFlightIntention": [
    {
      "planId": 12345,
      "indicative": "GLO1234",
      "aircraftType": "B737",
      "airline": "GOL"
    }
  ],
  "time": 1752022863073
}
```

## Contributing

1. Add new analysis methods to `ReplayDataService`
2. Create additional data models as needed
3. Extend the interactive menu with new features
4. Add comprehensive tests for new functionality 