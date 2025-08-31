# Configuration Package

This package contains all configuration classes for the Aviation Flight Data Processing System, handling database connections, security, and Sigma integration.

## Configuration Files

### `OracleConfig.java`
**Oracle Database Configuration**
- Configures Oracle database connection for Sigma production data
- Uses Apache Commons DBCP2 for connection pooling
- Integrates with Sigma's ExtendedHibernateOperations for proper serialization
- Credentials loaded from `application.yml` environment variables
- Provides JdbcTemplate and Hibernate SessionFactory beans

**Key Beans:**
- `oracleDataSource()` - Oracle database connection pool
- `oracleJdbcTemplate()` - JDBC operations template
- `oracleSessionFactory()` - Hibernate session factory
- `extendedHibernateOperations()` - Sigma-compatible Hibernate operations

### `SecurityConfig.java`
**Spring Security Configuration**
- Disables CSRF protection for REST API usage
- Permits all requests without authentication
- Designed for development/test environment within Sigma ecosystem
- Extends `WebSecurityConfigurerAdapter`

### `SigmaConfig.java`
**Sigma Integration Configuration**
- Provides Sigma-specific beans and utilities
- Configures `IMergeSupport` for data merging operations
- Handles Sigma repository patterns and data access
- Integrates with Sigma GSA commons and GFX domain libraries
- Contains specialized SQL query builders for Oracle database

**Key Features:**
- Stream-based data processing utilities
- Oracle-specific query optimizations
- Sigma domain object integration
- Custom data serialization support

### `CustomOracle10gDialect.java`
**Custom Hibernate Dialect**
- Extends `OracleSpatial10gDialect` for spatial data support
- Registers custom Oracle functions (e.g., `regexp_like`)
- Optimized for Sigma's Oracle database schema
- Handles spatial geometry operations with JTS integration

## Configuration Dependencies

### External Libraries
- **Spring Boot**: Core configuration framework
- **Hibernate**: ORM and spatial data support
- **Apache Commons DBCP2**: Database connection pooling
- **Sigma GSA Commons**: Sigma-specific utilities
- **Sigma GFX Domain**: Domain object integration

### Environment Variables
Configuration relies on these environment variables (defined in `application.yml`):
- `ORACLE_HOST` - Oracle database host (default: 10.103.3.8)
- `ORACLE_PORT` - Oracle database port (default: 1521)
- `ORACLE_SERVICE` - Oracle service name (default: SIGMA_PLT3_DEV1_APP)
- `ORACLE_USERNAME` - Oracle username (default: sigma)
- `ORACLE_PASSWORD` - Oracle password (default: mudar123)

## Usage

These configurations are automatically loaded by Spring Boot during application startup. No manual instantiation required.

### Oracle Connection Example
```java
@Autowired
private JdbcOperations oracleJdbcTemplate;

@Autowired
private ExtendedHibernateOperations extendedHibernateOperations;
```

### Security
All REST endpoints are accessible without authentication due to the permissive security configuration.

## Notes

- **Production Ready**: Oracle configuration matches Sigma production environment
- **Development Focused**: Security is disabled for ease of testing
- **Sigma Compatible**: Uses exact same patterns as Sigma's SimpleHibernateTest
- **Spatial Support**: Full Oracle Spatial and JTS geometry support enabled
