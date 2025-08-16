# Development Setup Guide

This guide helps you set up the Aviation Flight Data Processing System for development.

## Prerequisites
- Put this project under modules/test folder!!!!
- Java 15 or higher
- Maven 3.8+ (tested with 3.8.3)
- MongoDB (for streaming features) - can be run via Docker
- Oracle Database access (for production Sigma integration)

## Quick Setup

### 1. Clone and Build
```bash
git clone <repository-url>
cd streaming-flight-data-system
mvn clean compile
```

### 2. Database Setup

#### MongoDB (Required)
```bash
# Using Docker (recommended)
docker run -d --name aviation_mongodb -p 27017:27017 mongo:latest

# Verify it's running
docker ps | grep mongo
```

#### Oracle Database (For Sigma Integration)
You'll need access to the Sigma Oracle database. Contact your system administrator for:
- Database host and port
- Service name
- Username and password

### 3. Configuration Files

#### Step 1: Create Oracle Configuration
```bash
# Copy the template file
cp src/main/java/com/example/config/OracleConfig.java.template src/main/java/com/example/config/OracleConfig.java
```

#### Step 2: Create Application Configuration
```bash
# Copy the example configuration
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

#### Step 3: Update Configuration
Edit `src/main/resources/application.yml` with your actual database details:

```yaml
oracle:
  host: your-actual-oracle-host
  port: 1521
  service: your-actual-service-name
  username: ${ORACLE_USERNAME}
  password: ${ORACLE_PASSWORD}
```

#### Step 4: Set Environment Variables
```bash
# Set your Oracle credentials as environment variables
export ORACLE_USERNAME=your-actual-username
export ORACLE_PASSWORD=your-actual-password
```

### 4. Run the Application
```bash
# Start the application
mvn spring-boot:run

# The API will be available at http://localhost:8080
```

### 5. Test the Setup
```bash
# Test MongoDB connection
curl http://localhost:8080/api/flights/health

# Test Oracle connection (if configured)
curl http://localhost:8080/api/flights/test-oracle-connection

# Get integration summary
curl http://localhost:8080/api/flights/integration-summary
```

## Configuration Files Structure

```
src/main/
├── java/com/example/config/
│   ├── OracleConfig.java.template     # Template (committed to git)
│   ├── OracleConfig.java             # Your actual config (NOT committed)
│   ├── SigmaConfig.java              # Sigma integration (NOT committed)
│   └── SecurityConfig.java           # Security config (NOT committed)
└── resources/
    ├── application.yml.example       # Template (committed to git)
    └── application.yml               # Your actual config (NOT committed)
```

## Security Notes

⚠️ **IMPORTANT**: Never commit files containing actual credentials!

- `OracleConfig.java` - Contains actual database credentials
- `SigmaConfig.java` - Contains database connection logic
- `application.yml` - Contains actual configuration values

These files are automatically ignored by `.gitignore` to prevent accidental commits.

## Troubleshooting

### Common Issues

1. **Oracle Connection Failed**
   - Verify your Oracle host, port, and service name
   - Check that your username/password are correct
   - Ensure the Oracle database is accessible from your network

2. **MongoDB Connection Failed**
   - Ensure MongoDB is running: `docker ps | grep mongo`
   - Check if port 27017 is available
   - Verify MongoDB is accessible at localhost:27017

3. **Application Won't Start**
   - Check that all required configuration files exist
   - Verify environment variables are set
   - Check the application logs for specific error messages

### Getting Help

1. Check the main [README.md](README.md) for API documentation
2. Review the [controller documentation](src/main/java/com/example/controller/README.md)
3. Check the [service documentation](src/main/java/com/example/service/README.md)

## Development Workflow

1. **Make changes** to your code
2. **Test locally** using the development endpoints
3. **Commit your changes** (configuration files with credentials are automatically excluded)
4. **Push to repository** - sensitive files won't be included

## Production Deployment

For production deployment:
1. Use proper secret management (AWS Secrets Manager, Azure Key Vault, etc.)
2. Set environment variables through your deployment platform
3. Never hardcode credentials in configuration files
4. Use secure connection strings and encrypted passwords
