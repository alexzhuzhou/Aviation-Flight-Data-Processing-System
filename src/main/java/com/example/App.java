package com.example;

import com.example.model.*;
import com.example.service.ReplayDataService;
import com.example.service.DataAnalysisService;
import com.example.service.FlightDataJoinService;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class for Aviation Replay Data Processor
 */
public class App {
    
    private static final String REPLAY_FILE = "replay2.json";
    private static final ReplayDataService dataService = new ReplayDataService();
    private static final DataAnalysisService analysisService = new DataAnalysisService();
    private static final FlightDataJoinService joinService = new FlightDataJoinService();
    
    public static void main(String[] args) {
        System.out.println("=== Aviation Replay Data Processor ===");
        System.out.println("Loading replay data from: " + REPLAY_FILE);
        System.out.println();
        
        try {
            // Load the replay data
            ReplayData replayData = dataService.loadReplayData(REPLAY_FILE);
            
            // Print basic summary
            dataService.printDataSummary(replayData);
            
            // Analyze different aspects of the data
            dataService.analyzeRealPathData(replayData);
            dataService.analyzeFlightIntentions(replayData);
            dataService.analyzeCorrelations(replayData);
            
            // Show sample data
            dataService.showSampleData(replayData);
            
            // Analyze join strategy
            analysisService.recommendJoinStrategy(replayData);
            
            // Join the flight data
            List<JoinedFlightData> joinedFlights = joinService.joinFlightData(replayData);
            joinService.analyzeJoinResults(joinedFlights);
            joinService.showSampleJoinedData(joinedFlights);
            
            // Interactive menu
            runInteractiveMenu(replayData, joinedFlights);
            
        } catch (IOException e) {
            System.err.println("Error loading replay data: " + e.getMessage());
            System.err.println("Make sure the replay2.json file exists in the project root.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Interactive menu for exploring the data
     */
    private static void runInteractiveMenu(ReplayData replayData, List<JoinedFlightData> joinedFlights) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("=== INTERACTIVE MENU ===");
            System.out.println("1. Search flights by call sign");
            System.out.println("2. Get tracking points for a flight plan");
            System.out.println("3. Show data summary again");
            System.out.println("4. Analyze real path data");
            System.out.println("5. Analyze flight intentions");
            System.out.println("6. Show sample data");
            System.out.println("7. Analyze join strategy");
            System.out.println("8. Show joined flight data");
            System.out.println("9. Search joined flights");
            System.out.println("10. Export to JSON for MongoDB");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    searchFlightsByCallSign(replayData, scanner);
                    break;
                case "2":
                    getTrackingPointsForPlan(replayData, scanner);
                    break;
                case "3":
                    dataService.printDataSummary(replayData);
                    break;
                case "4":
                    dataService.analyzeRealPathData(replayData);
                    break;
                case "5":
                    dataService.analyzeFlightIntentions(replayData);
                    break;
                case "6":
                    dataService.showSampleData(replayData);
                    break;
                case "7":
                    analysisService.recommendJoinStrategy(replayData);
                    break;
                case "8":
                    joinService.showSampleJoinedData(joinedFlights);
                    break;
                case "9":
                    searchJoinedFlights(joinedFlights, scanner);
                    break;
                case "10":
                    exportJoinedFlights(joinedFlights, scanner);
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Search for flights by call sign
     */
    private static void searchFlightsByCallSign(ReplayData replayData, Scanner scanner) {
        System.out.print("Enter call sign to search for: ");
        String callSign = scanner.nextLine().trim();
        
        if (callSign.isEmpty()) {
            System.out.println("Call sign cannot be empty.");
            return;
        }
        
        List<FlightIntention> flights = dataService.findFlightsByCallSign(replayData, callSign);
        
        if (flights.isEmpty()) {
            System.out.println("No flights found with call sign containing: " + callSign);
        } else {
            System.out.println("Found " + flights.size() + " flight(s):");
            flights.forEach(flight -> {
                System.out.println("  Plan ID: " + flight.getPlanId() + 
                                 ", Call Sign: " + flight.getIndicative() + 
                                 ", Aircraft: " + flight.getAircraftType() + 
                                 ", Airline: " + flight.getAirline() + 
                                 ", Finished: " + flight.isFinished());
            });
        }
    }
    
    /**
     * Get tracking points for a specific flight plan
     */
    private static void getTrackingPointsForPlan(ReplayData replayData, Scanner scanner) {
        System.out.print("Enter plan ID: ");
        String planIdStr = scanner.nextLine().trim();
        
        try {
            int planId = Integer.parseInt(planIdStr);
            List<RealPathPoint> trackingPoints = dataService.getTrackingPointsForPlan(replayData, planId);
            
            if (trackingPoints.isEmpty()) {
                System.out.println("No tracking points found for plan ID: " + planId);
            } else {
                System.out.println("Found " + trackingPoints.size() + " tracking point(s) for plan " + planId + ":");
                trackingPoints.stream()
                        .limit(10) // Show first 10 points
                        .forEach(point -> {
                            String position = "N/A";
                            if (point.getKinematic() != null && point.getKinematic().getPosition() != null) {
                                position = String.format("%.4f, %.4f", 
                                        point.getKinematic().getPosition().getLatitude(),
                                        point.getKinematic().getPosition().getLongitude());
                            }
                            System.out.println("  Seq: " + point.getSeqNum() + 
                                             ", FL: " + point.getFlightLevel() + 
                                             ", Speed: " + point.getTrackSpeed() + 
                                             ", Position: " + position + 
                                             ", Simulated: " + point.isSimulating());
                        });
                
                if (trackingPoints.size() > 10) {
                    System.out.println("  ... and " + (trackingPoints.size() - 10) + " more points");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid plan ID. Please enter a number.");
        }
    }
    
    /**
     * Search joined flights by various criteria
     */
    private static void searchJoinedFlights(List<JoinedFlightData> joinedFlights, Scanner scanner) {
        System.out.print("Enter search term (call sign, airline, or aircraft type): ");
        String searchTerm = scanner.nextLine().trim();
        
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }
        
        List<JoinedFlightData> results = joinService.searchFlights(joinedFlights, searchTerm);
        
        if (results.isEmpty()) {
            System.out.println("No flights found matching: " + searchTerm);
        } else {
            System.out.println("Found " + results.size() + " flight(s) matching '" + searchTerm + "':");
            results.forEach(flight -> {
                System.out.println("  " + flight.getIndicative() + 
                                 " (" + flight.getAircraftType() + ", " + flight.getAirline() + ")" +
                                 " - " + flight.getTotalTrackingPoints() + " tracking points" +
                                 (flight.isHasTrackingData() ? "" : " (No tracking data)"));
            });
        }
    }
    
    /**
     * Export joined flights to JSON file
     */
    private static void exportJoinedFlights(List<JoinedFlightData> joinedFlights, Scanner scanner) {
        System.out.print("Enter filename (default: joined_flights.json): ");
        String filename = scanner.nextLine().trim();
        
        if (filename.isEmpty()) {
            filename = "joined_flights.json";
        }
        
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }
        
        try {
            joinService.exportToJson(joinedFlights, filename);
        } catch (IOException e) {
            System.err.println("Error exporting to JSON: " + e.getMessage());
        }
    }
} 