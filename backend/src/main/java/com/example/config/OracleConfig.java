package com.example.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import br.atech.commons.spring.orm.ExtendedHibernateOperations;
import br.atech.commons.spring.orm.ExtendedHibernateTemplate;

/**
 * Oracle database configuration for Sigma data extraction
 * 
 * Uses the EXACT same configuration as SimpleHibernateTest with ExtendedHibernateOperations
 * to properly handle serialization issues.
 * 
 * Sensitive credentials are loaded from application.yml environment variables.
 */
@Configuration
@EnableTransactionManagement
public class OracleConfig {
    
    @Value("${oracle.host}")
    private String oracleHost;
    
    @Value("${oracle.port}")
    private String oraclePort;
    
    @Value("${oracle.service}")
    private String oracleService;
    
    @Value("${oracle.username}")
    private String oracleUsername;
    
    @Value("${oracle.password}")
    private String oraclePassword;
    
    /**
     * Oracle DataSource configuration
     * Simple configuration with basic connection pooling
     * MODIFIED: Added error handling to prevent startup failures and reads from application.yml
     */
    @Bean("sigmaOracleDataSource")
    public DataSource sigmaOracleDataSource() {
        BasicDataSource ds = new BasicDataSource();
        
        // Build connection URL from properties
        String url = String.format("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=%s)(PORT=%s))(CONNECT_DATA=(SERVER=DEDICATED)(service_name=%s)))",
                oracleHost, oraclePort, oracleService);
        
        ds.setUrl(url);
        ds.setUsername(oracleUsername);
        ds.setPassword(oraclePassword);
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        
        // Basic connection pool settings - MODIFIED: Reduced for testing
        ds.setInitialSize(0);                    // Start with 0 connections to avoid startup failure
        ds.setMaxTotal(20);                      // Maximum 20 connections
        ds.setMaxIdle(10);                       // Maximum 10 idle connections
        ds.setMinIdle(0);                        // Minimum 0 idle connections
        ds.setMaxWaitMillis(5000);               // Max wait time for connection (5 seconds)
        
        // Connection validation
        ds.setValidationQuery("SELECT 1 FROM DUAL");
        ds.setTestOnBorrow(true);                // Test connection before use
        ds.setTestWhileIdle(true);               // Test idle connections
        
        // ADDED: Additional settings to handle connection failures gracefully
        ds.setTestOnCreate(false);               // Don't test on create to avoid startup failure
        ds.setValidationQueryTimeout(3);        // 3 second timeout for validation
        
        return ds;
    }
    
    /**
     * Transaction manager for Oracle database operations
     */
    @Bean("oracleTransactionManager")
    public PlatformTransactionManager oracleTransactionManager() {
        return new DataSourceTransactionManager(sigmaOracleDataSource());
    }
    
    /**
     * JDBC Template for Sigma Oracle database operations
     */
    @Bean("jdbcTemplateSigma")
    public JdbcOperations jdbcTemplateSigma() {
        final var template = new JdbcTemplate();
        template.setDataSource(sigmaOracleDataSource());
        return template;
    }
    
    /**
     * Hibernate Session Factory
     * EXACT same configuration as SimpleHibernateTest
     */
    @Bean("oracleSessionFactory")
    public LocalSessionFactoryBean oracleSessionFactory() throws Exception {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(sigmaOracleDataSource());
        
        // EXACT same properties as SimpleHibernateTest with Sigma's custom dialect
        Properties props = new Properties();
        props.put(AvailableSettings.DIALECT, "br.atech.sigma.gfx.dialect.CustomOracle10gDialect");
        props.put(AvailableSettings.HBM2DDL_AUTO, "none");
        
        // Additional properties for better Oracle compatibility
        props.put(AvailableSettings.SHOW_SQL, "false");
        props.put(AvailableSettings.FORMAT_SQL, "false");
        props.put(AvailableSettings.USE_SQL_COMMENTS, "false");
        
        // Ensure proper serialization handling
        props.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "false");
        props.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, "16");
        
        factory.setHibernateProperties(props);
        
        // EXACT same package scanning as SimpleHibernateTest
        factory.setPackagesToScan(
            "br.atech.sigma.gfx.session.historical.flight.domain",
            "br.atech.sigma.gfx.session.historical.domain"
        );
        factory.afterPropertiesSet();
        
        // Log the configuration for verification
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OracleConfig.class);
        logger.info("Oracle SessionFactory configured with dialect: {}", props.get(AvailableSettings.DIALECT));
        logger.info("Oracle SessionFactory package scanning: br.atech.sigma.gfx.session.historical.flight.domain, br.atech.sigma.gfx.session.historical.domain");
        
        return factory;
    }
    
 
    
    /**
     * Standard HibernateTemplate for OracleFlightDataService
     * Uses the same session factory as ExtendedHibernateOperations
     */
    @Bean("hibernateTemplate")
    public org.springframework.orm.hibernate5.HibernateTemplate hibernateTemplate() throws Exception {
        org.springframework.orm.hibernate5.HibernateTemplate template = new org.springframework.orm.hibernate5.HibernateTemplate();
        template.setSessionFactory(oracleSessionFactory().getObject());
        return template;
    }
    
    /**
     * ExtendedHibernateOperations - EXACT same as SimpleHibernateTest
     * This handles serialization issues better than standard HibernateTemplate
     */
    @Bean("hibernateOps")
    public ExtendedHibernateOperations hibernateOps() throws Exception {
        ExtendedHibernateTemplate template = new ExtendedHibernateTemplate();
        template.setSessionFactory(oracleSessionFactory().getObject());
        return template;
    }
}
