package com.example.config;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.JdbcUtils;

import br.atech.commons.Pair;
import br.atech.gsa.commons.helper.IMergeSupport;
import br.atech.gsa.commons.helper.IRepo;
import br.atech.gsa.commons.historic.StoreReplay;

/**
 * Sigma integration configuration
 * 
 * Provides the necessary Sigma components for Oracle data extraction,
 * specifically the IMergeSupport implementation that streams packets
 * from the Sigma Oracle database.
 */
@Configuration
public class SigmaConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SigmaConfig.class);
    
    /**
     * Extended interface for IMergeSupport with time filtering capability
     */
    public interface ExtendedIMergeSupport extends IMergeSupport {
        Stream<Pair<Long, byte[]>> streamPackets(LocalDate date, LocalTime startTime, LocalTime endTime);
    }
    
    /**
     * IMergeSupport implementation for streaming packets from Sigma Oracle database
     * 
     * This implementation replicates the functionality from the Sigma GSA server
     * MergeSupport class, providing the streamPackets method that queries
     * TB_STORE_REPLAY table for flight data packets.
     */
    @Bean
    public ExtendedIMergeSupport mergeSupport(@Qualifier("sigmaOracleDataSource") DataSource dataSource,
                                     @Qualifier("jdbcTemplateSigma") JdbcOperations jdbcOperations) {
        return new ExtendedIMergeSupport() {
            
            @Override
            public Stream<Pair<Long, byte[]>> streamPackets(final LocalDate ts) {
                LOGGER.debug("Streaming packets from Oracle database for date: {}", ts);
                
                Connection c = null;
                PreparedStatement p = null;
                ResultSet r = null;
                
                try {
                    final var con = c = dataSource.getConnection();
                    final var pst = p = con.prepareStatement(
                        "select NR_TIMESTAMP, DS_DATA from TB_STORE_REPLAY " +
                        "where nr_timestamp >= TO_DATE('" + IMergeSupport.formatName(ts, "/") + " 00:00:00', 'DD/MM/RR HH24:MI:SS') and " +
                        "nr_timestamp <= TO_DATE('" + IMergeSupport.formatName(ts, "/") + " 23:59:59', 'DD/MM/RR HH24:MI:SS') " +
                        "order by nr_timestamp asc"
                    );
                    final var rs = r = pst.executeQuery();
                    
                    final var itr = new Iterator<Pair<Long, byte[]>>() {
                        boolean ready;
                        
                        @Override
                        public boolean hasNext() {
                            if (!ready) {
                                tryAdvance();
                            }
                            return ready;
                        }
                        
                        @Override
                        public Pair<Long, byte[]> next() {
                            if (!ready) {
                                throw new IllegalStateException();
                            }
                            
                            ready = false;
                            
                            try {
                                final Long l = rs.getTimestamp(1).getTime();
                                final byte[] chunk = rs.getBytes(2);
                                return Pair.of(l, chunk);
                            } catch (final SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        
                        private void tryAdvance() {
                            try {
                                ready = rs.next();
                            } catch (final SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    
                    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(itr, 0), false)
                        .onClose(() -> {
                            closeConnection(con, pst, rs);
                        });
                        
                } catch (final Exception e) {
                    LOGGER.warn("Error streaming packets from Oracle database", e);
                    closeConnection(c, p, r);
                }
                
                return Stream.empty();
            }
            
            /**
             * Stream packets from Oracle database with time range filtering
             * 
             * @param date The date to query
             * @param startTime Start time for filtering (inclusive)
             * @param endTime End time for filtering (inclusive)
             * @return Stream of timestamp-data pairs
             */
            public Stream<Pair<Long, byte[]>> streamPackets(final LocalDate date, final LocalTime startTime, final LocalTime endTime) {
                LOGGER.debug("Streaming packets from Oracle database for date: {} from {} to {}", date, startTime, endTime);
                
                Connection c = null;
                PreparedStatement p = null;
                ResultSet r = null;
                
                try {
                    // Format date and time strings for Oracle query
                    String startDateTime = String.format("%s %02d:%02d:00", 
                        IMergeSupport.formatName(date, "/"), 
                        startTime.getHour(), 
                        startTime.getMinute());
                    String endDateTime = String.format("%s %02d:%02d:59", 
                        IMergeSupport.formatName(date, "/"), 
                        endTime.getHour(), 
                        endTime.getMinute());
                    
                    final var con = c = dataSource.getConnection();
                    final var pst = p = con.prepareStatement(
                        "select NR_TIMESTAMP, DS_DATA from TB_STORE_REPLAY " +
                        "where nr_timestamp >= TO_DATE('" + startDateTime + "', 'DD/MM/RR HH24:MI:SS') and " +
                        "nr_timestamp <= TO_DATE('" + endDateTime + "', 'DD/MM/RR HH24:MI:SS') " +
                        "order by nr_timestamp asc"
                    );
                    final var rs = r = pst.executeQuery();
                    
                    final var itr = new Iterator<Pair<Long, byte[]>>() {
                        boolean ready;
                        
                        @Override
                        public boolean hasNext() {
                            if (!ready) {
                                tryAdvance();
                            }
                            return ready;
                        }
                        
                        @Override
                        public Pair<Long, byte[]> next() {
                            if (!ready) {
                                throw new IllegalStateException();
                            }
                            
                            ready = false;
                            
                            try {
                                final Long l = rs.getTimestamp(1).getTime();
                                final byte[] chunk = rs.getBytes(2);
                                return Pair.of(l, chunk);
                            } catch (final SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        
                        private void tryAdvance() {
                            try {
                                ready = rs.next();
                            } catch (final SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    
                    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(itr, 0), false)
                        .onClose(() -> {
                            closeConnection(con, pst, rs);
                        });
                        
                } catch (final Exception e) {
                    LOGGER.warn("Error streaming packets from Oracle database with time filter", e);
                    closeConnection(c, p, r);
                }
                
                return Stream.empty();
            }
            
            @Override
            public boolean existDataLastDay(final LocalDate ts) {
                // Simple implementation - check if any data exists for the date
                try {
                    Integer count = jdbcOperations.queryForObject(
                        "select count(*) from TB_STORE_REPLAY " +
                        "where nr_timestamp >= TO_DATE('" + IMergeSupport.formatName(ts, "/") + " 00:00:00', 'DD/MM/RR HH24:MI:SS') and " +
                        "nr_timestamp <= TO_DATE('" + IMergeSupport.formatName(ts, "/") + " 23:59:59', 'DD/MM/RR HH24:MI:SS')",
                        Integer.class
                    );
                    return count != null && count > 0;
                } catch (Exception e) {
                    LOGGER.warn("Error checking if data exists for date: {}", ts, e);
                    return false;
                }
            }
            
            @Override
            public void finish(LocalDate ts, IRepo<byte[]> repo) {
                // Implementation for finishing merge operation
                LOGGER.info("Finished processing packets for date: {}", ts);
            }
            
            @Override
            public void saveStoreReplay(final StoreReplay store) {
                // Implementation for saving replay data
                jdbcOperations.update("INSERT INTO TB_STORE_REPLAY (NR_TIMESTAMP, DS_DATA) VALUES (?,?)", 
                    (PreparedStatementSetter) ps -> {
                        ps.setTimestamp(1, new Timestamp(store.getTimestamp()));
                        ps.setBlob(2, new ByteArrayInputStream(store.getData()));
                    });
            }
            
            /**
             * Helper method to safely close database resources
             */
            private void closeConnection(Connection con, PreparedStatement pst, ResultSet rs) {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(pst);
                JdbcUtils.closeConnection(con);
            }
        };
    }
}
