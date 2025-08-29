package com.example.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.model.OracleProcessingResult;
import com.example.model.ReplayPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.atech.gsa.commons.helper.IMergeSupport;
import br.atech.gsa.commons.helper.ReplaySerializer;
import com.example.config.SigmaConfig;

/**
 * Service for extracting flight data from Sigma Oracle database
 * 
 * This service replicates the functionality of PathVoGeneratorTest but as a
 * Spring service that can be called from REST endpoints. It connects to the
 * Sigma Oracle database, extracts ReplayPath packets, and processes them
 * through the existing StreamingFlightService.
 */
@Service
public class OracleDataExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(OracleDataExtractionService.class);
    
    // Hardcoded date as requested (same as PathVoGeneratorTest)
    private static final LocalDate EXTRACTION_DATE = LocalDate.of(2025, 7, 11);
    
    @Autowired
    @Qualifier("jdbcTemplateSigma")
    private org.springframework.jdbc.core.JdbcOperations jdbcTemplate;
    
    @Autowired
    private SigmaConfig.ExtendedIMergeSupport mergeSupport;
    
    @Autowired
    private StreamingFlightService streamingFlightService;
    
    private final ObjectMapper objectMapper;
    
    public OracleDataExtractionService() {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Extract and process flight data from Oracle database with custom date and time range
     * 
     * @param dateStr Optional date string (format: YYYY-MM-DD). If null, uses hardcoded date
     * @param startTimeStr Optional start time string (format: HH:mm). If provided, endTimeStr must also be provided
     * @param endTimeStr Optional end time string (format: HH:mm). If provided, startTimeStr must also be provided
     */
    public OracleProcessingResult extractAndProcessFlightData(String dateStr, String startTimeStr, String endTimeStr) {
        // Parse date parameter or use default
        LocalDate extractionDate = EXTRACTION_DATE; // Default
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                extractionDate = LocalDate.parse(dateStr);
            } catch (Exception e) {
                logger.error("Invalid date format: {}. Using default date: {}", dateStr, EXTRACTION_DATE);
            }
        }
        
        // Parse time parameters
        LocalTime startTime = null;
        LocalTime endTime = null;
        if (startTimeStr != null && endTimeStr != null) {
            try {
                startTime = LocalTime.parse(startTimeStr);
                endTime = LocalTime.parse(endTimeStr);
            } catch (Exception e) {
                logger.error("Invalid time format. StartTime: {}, EndTime: {}. Using full day processing.", startTimeStr, endTimeStr);
                startTime = null;
                endTime = null;
            }
        }
        
        // Log extraction parameters
        if (startTime != null && endTime != null) {
            logger.info("Starting Oracle data extraction for date: {} from {} to {}", extractionDate, startTime, endTime);
        } else {
            logger.info("Starting Oracle data extraction for date: {} (entire day)", extractionDate);
        }
        
        long startTimeMs = System.currentTimeMillis();
        long dbConnectionStart = System.currentTimeMillis();
        
        // Initialize counters
        AtomicInteger totalPacketsProcessed = new AtomicInteger(0);
        AtomicInteger packetsWithErrors = new AtomicInteger(0);
        AtomicInteger totalNewFlights = new AtomicInteger(0);
        AtomicInteger totalUpdatedFlights = new AtomicInteger(0);
        
        try {
            // Test database connection
            logger.info("Testing Oracle database connection...");
            jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            long dbConnectionTime = System.currentTimeMillis() - dbConnectionStart;
            logger.info("Oracle database connection successful ({}ms)", dbConnectionTime);
            
            // Start data extraction
            long extractionStart = System.currentTimeMillis();
            
            // Stream packets from database with time filtering if provided
            var streamPackets = (startTime != null && endTime != null) 
                ? mergeSupport.streamPackets(extractionDate, startTime, endTime)
                : mergeSupport.streamPackets(extractionDate);
            
            long extractionTime = System.currentTimeMillis() - extractionStart;
            logger.info("Data extraction from Oracle completed ({}ms)", extractionTime);
            
            // Process each packet individually (same logic as existing method)
            long processingStart = System.currentTimeMillis();
            logger.info("Starting individual packet processing...");
            
            AtomicInteger totalPackets = new AtomicInteger(0);
            AtomicInteger successfulPackets = new AtomicInteger(0);
            AtomicInteger skippedPackets = new AtomicInteger(0);
            
            streamPackets.forEach(packet -> {
                try {
                    totalPackets.incrementAndGet();
                    
                    // Extract timestamp when packet was stored (from packet.getKey())
                    var timestamp = packet.getKey();
                    String timestampStr = timestamp != null ? timestamp.toString() : null;
                    
                    // Extract and deserialize the ReplayPath data with error handling
                    final byte[] value = packet.getValue();
                    br.atech.gsa.commons.historic.ReplayPath sigmaReplayPath = null;
                    
                    try {
                        sigmaReplayPath = ReplaySerializer.input(value);
                        logger.debug("Successfully deserialized ReplayPath packet with {} bytes", value.length);
                    } catch (Exception e) {
                        logger.warn("Failed to deserialize ReplayPath packet due to Genesis serialization issue: {}. " +
                                   "This is a known compatibility issue with Java 17 and ASM. Skipping packet.", e.getMessage());
                        skippedPackets.incrementAndGet();
                        return; // Skip this packet and continue with the next one
                    }
                    
                    if (sigmaReplayPath == null) {
                        logger.warn("Deserialized ReplayPath is null, skipping packet");
                        skippedPackets.incrementAndGet();
                        return;
                    }
                    
                    // Convert Sigma ReplayPath to our ReplayPath format
                    ReplayPath ourReplayPath = convertSigmaReplayPathToOurs(sigmaReplayPath, timestampStr);
                    
                    // Process through existing StreamingFlightService
                    StreamingFlightService.ProcessingResult result = streamingFlightService.processReplayPath(ourReplayPath);
                    
                    // Update counters
                    successfulPackets.incrementAndGet();
                    totalPacketsProcessed.incrementAndGet();
                    totalNewFlights.addAndGet(result.getNewFlights());
                    totalUpdatedFlights.addAndGet(result.getUpdatedFlights());
                    
                    // Log progress every 100 packets
                    if (totalPacketsProcessed.get() % 100 == 0) {
                        logger.info("Processed {} packets (New flights: {}, Updated flights: {})", 
                            totalPacketsProcessed.get(), totalNewFlights.get(), totalUpdatedFlights.get());
                    }
                    
                } catch (Exception e) {
                    packetsWithErrors.incrementAndGet();
                    logger.error("Failed to process packet {}: {}", totalPacketsProcessed.get() + 1, e.getMessage());
                    logger.debug("Packet processing error details", e);
                }
            });
            
            long processingTime = System.currentTimeMillis() - processingStart;
            long totalTime = System.currentTimeMillis() - startTimeMs;
            
            // Log comprehensive processing summary
            String timeRangeStr = (startTime != null && endTime != null) 
                ? String.format(" from %s to %s", startTime, endTime)
                : " (entire day)";
                
            logger.info("=== Oracle Data Processing Summary ===");
            logger.info("Date: {}{}", extractionDate, timeRangeStr);
            logger.info("Total packets encountered: {}", totalPackets.get());
            logger.info("Successfully processed: {}", successfulPackets.get());
            logger.info("Skipped due to serialization issues: {}", skippedPackets.get());
            logger.info("Processing errors: {}", packetsWithErrors.get());
            logger.info("New flights created: {}", totalNewFlights.get());
            logger.info("Existing flights updated: {}", totalUpdatedFlights.get());
            logger.info("Data extraction time: {}ms", extractionTime);
            logger.info("Packet processing time: {}ms", processingTime);
            logger.info("Total operation time: {}ms", totalTime);
            logger.info("=====================================");
            
            // Create comprehensive result
            String resultMessage = String.format("Successfully processed %d packets from Oracle database for date %s%s (%d new flights, %d updated flights, %d errors)",
                totalPacketsProcessed.get(), extractionDate, timeRangeStr, totalNewFlights.get(), totalUpdatedFlights.get(), packetsWithErrors.get());
                
            OracleProcessingResult result = new OracleProcessingResult(
                totalNewFlights.get(),
                totalUpdatedFlights.get(), 
                totalPacketsProcessed.get(),
                packetsWithErrors.get(),
                totalTime,
                "Sigma Oracle Database",
                extractionDate.toString(),
                resultMessage
            );
            
            // Set detailed timing information
            result.setDatabaseConnectionTime(dbConnectionTime);
            result.setDataExtractionTime(extractionTime);
            result.setDataProcessingTime(processingTime);
            
            logger.info("Oracle data extraction completed successfully: {}", result);
            return result;
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTimeMs;
            logger.error("Oracle data extraction failed", e);
            
            // Return error result
            OracleProcessingResult errorResult = new OracleProcessingResult(
                totalNewFlights.get(),
                totalUpdatedFlights.get(),
                totalPacketsProcessed.get(),
                packetsWithErrors.get(),
                totalTime,
                "Sigma Oracle Database (Failed)",
                extractionDate.toString(),
                "Error during Oracle data extraction: " + e.getMessage()
            );
            
            return errorResult;
        }
    }

    /**
     * Extract and process flight data from Oracle database
     * 
     * This method replicates the _00_run() test from PathVoGeneratorTest:
     * 1. Connects to Sigma Oracle database
     * 2. Streams packets for the specified date
     * 3. Processes each packet individually
     * 4. Returns comprehensive processing statistics
     */
    public OracleProcessingResult extractAndProcessFlightData() {
        logger.info("Starting Oracle data extraction for date: {}", EXTRACTION_DATE);
        
        long startTime = System.currentTimeMillis();
        long dbConnectionStart = System.currentTimeMillis();
        
        // Initialize counters
        AtomicInteger totalPacketsProcessed = new AtomicInteger(0);
        AtomicInteger packetsWithErrors = new AtomicInteger(0);
        AtomicInteger totalNewFlights = new AtomicInteger(0);
        AtomicInteger totalUpdatedFlights = new AtomicInteger(0);
        
        try {
            // Test database connection
            logger.info("Testing Oracle database connection...");
            jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            long dbConnectionTime = System.currentTimeMillis() - dbConnectionStart;
            logger.info("Oracle database connection successful ({}ms)", dbConnectionTime);
            
            // Start data extraction
            long extractionStart = System.currentTimeMillis();
            logger.info("Streaming packets from Oracle database for date: {}", EXTRACTION_DATE);
            
            // Stream packets from database (same as PathVoGeneratorTest)
            var streamPackets = mergeSupport.streamPackets(EXTRACTION_DATE);
            
            long extractionTime = System.currentTimeMillis() - extractionStart;
            logger.info("Data extraction from Oracle completed ({}ms)", extractionTime);
            
            // Process each packet individually
            long processingStart = System.currentTimeMillis();
            logger.info("Starting individual packet processing...");
            
            AtomicInteger totalPackets = new AtomicInteger(0);
            AtomicInteger successfulPackets = new AtomicInteger(0);
            AtomicInteger skippedPackets = new AtomicInteger(0);
            
            streamPackets.forEach(packet -> {
                try {
                    totalPackets.incrementAndGet();
                    
                    // Extract timestamp when packet was stored (from packet.getKey())
                    var timestamp = packet.getKey();
                    String timestampStr = timestamp != null ? timestamp.toString() : null;
                    
                    // Extract and deserialize the ReplayPath data with error handling
                    final byte[] value = packet.getValue();
                    br.atech.gsa.commons.historic.ReplayPath sigmaReplayPath = null;
                    
                    try {
                        sigmaReplayPath = ReplaySerializer.input(value);
                        logger.debug("Successfully deserialized ReplayPath packet with {} bytes", value.length);
                    } catch (Exception e) {
                        logger.warn("Failed to deserialize ReplayPath packet due to Genesis serialization issue: {}. " +
                                   "This is a known compatibility issue with Java 17 and ASM. Skipping packet.", e.getMessage());
                        skippedPackets.incrementAndGet();
                        return; // Skip this packet and continue with the next one
                    }
                    
                    if (sigmaReplayPath == null) {
                        logger.warn("Deserialized ReplayPath is null, skipping packet");
                        skippedPackets.incrementAndGet();
                        return;
                    }
                    
                    // Convert Sigma ReplayPath to our ReplayPath format
                    ReplayPath ourReplayPath = convertSigmaReplayPathToOurs(sigmaReplayPath, timestampStr);
                    
                    // Process through existing StreamingFlightService
                    StreamingFlightService.ProcessingResult result = streamingFlightService.processReplayPath(ourReplayPath);
                    
                    // Update counters
                    successfulPackets.incrementAndGet();
                    totalPacketsProcessed.incrementAndGet();
                    totalNewFlights.addAndGet(result.getNewFlights());
                    totalUpdatedFlights.addAndGet(result.getUpdatedFlights());
                    
                    // Log progress every 100 packets
                    if (totalPacketsProcessed.get() % 100 == 0) {
                        logger.info("Processed {} packets (New flights: {}, Updated flights: {})", 
                            totalPacketsProcessed.get(), totalNewFlights.get(), totalUpdatedFlights.get());
                    }
                    
                } catch (Exception e) {
                    packetsWithErrors.incrementAndGet();
                    logger.error("Failed to process packet {}: {}", totalPacketsProcessed.get() + 1, e.getMessage());
                    logger.debug("Packet processing error details", e);
                }
            });
            
            long processingTime = System.currentTimeMillis() - processingStart;
            long totalTime = System.currentTimeMillis() - startTime;
            
            // Log comprehensive processing summary
            logger.info("=== Oracle Data Processing Summary ===");
            logger.info("Total packets encountered: {}", totalPackets.get());
            logger.info("Successfully processed: {}", successfulPackets.get());
            logger.info("Skipped due to serialization issues: {}", skippedPackets.get());
            logger.info("Processing errors: {}", packetsWithErrors.get());
            logger.info("New flights created: {}", totalNewFlights.get());
            logger.info("Existing flights updated: {}", totalUpdatedFlights.get());
            logger.info("Data extraction time: {}ms", System.currentTimeMillis() - extractionStart);
            logger.info("Packet processing time: {}ms", processingTime);
            logger.info("Total operation time: {}ms", totalTime);
            logger.info("=====================================");
            
            // Create comprehensive result
            OracleProcessingResult result = new OracleProcessingResult(
                totalNewFlights.get(),
                totalUpdatedFlights.get(), 
                totalPacketsProcessed.get(),
                packetsWithErrors.get(),
                totalTime,
                "Sigma Oracle Database",
                EXTRACTION_DATE.toString(),
                String.format("Successfully processed %d packets from Oracle database (%d new flights, %d updated flights, %d errors)",
                    totalPacketsProcessed.get(), totalNewFlights.get(), totalUpdatedFlights.get(), packetsWithErrors.get())
            );
            
            // Set detailed timing information
            result.setDatabaseConnectionTime(dbConnectionTime);
            result.setDataExtractionTime(extractionTime);
            result.setDataProcessingTime(processingTime);
            
            logger.info("Oracle data extraction completed successfully: {}", result);
            return result;
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("Oracle data extraction failed", e);
            
            // Return error result
            OracleProcessingResult errorResult = new OracleProcessingResult(
                totalNewFlights.get(),
                totalUpdatedFlights.get(),
                totalPacketsProcessed.get(),
                packetsWithErrors.get(),
                totalTime,
                "Sigma Oracle Database (Failed)",
                EXTRACTION_DATE.toString(),
                "Error during Oracle data extraction: " + e.getMessage()
            );
            
            return errorResult;
        }
    }
    
    /**
     * Convert Sigma's ReplayPath format to our ReplayPath format
     * 
     * This handles the data transformation between the two systems,
     * ensuring compatibility with our existing processing logic.
     */
    private ReplayPath convertSigmaReplayPathToOurs(br.atech.gsa.commons.historic.ReplayPath sigmaReplayPath, String timestampStr) {
        try {
            // Convert to JSON first (same approach as PathVoGeneratorTest)
            String json = objectMapper.writeValueAsString(sigmaReplayPath);
            
            // Add timestamp to JSON if we have one
            if (timestampStr != null) {
                Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);
                jsonMap.put("packetStoredTimestamp", timestampStr);
                json = objectMapper.writeValueAsString(jsonMap);
            }
            
            // Convert back to our ReplayPath format
            ReplayPath ourReplayPath = objectMapper.readValue(json, ReplayPath.class);
            
            return ourReplayPath;
            
        } catch (Exception e) {
            logger.error("Failed to convert Sigma ReplayPath to our format", e);
            throw new RuntimeException("Data conversion failed", e);
        }
    }
    
    /**
     * Test Oracle database connectivity
     */
    public boolean testDatabaseConnection() {
        try {
            logger.info("Testing Oracle database connection...");
            Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            boolean connected = result != null && result == 1;
            logger.info("Oracle database connection test: {}", connected ? "SUCCESS" : "FAILED");
            return connected;
        } catch (Exception e) {
            logger.error("Oracle database connection test failed", e);
            return false;
        }
    }
}
