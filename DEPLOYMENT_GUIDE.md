# Remote Access Deployment Guide

## Overview
This guide covers deploying your streaming flight data system for remote access when `PathVoGeneratorTest.java` is running on a different network.

## Option 1: Public IP with Port Forwarding

### Step 1: Router Configuration
1. **Access your router admin panel** (usually http://192.168.1.1 or http://192.168.0.1)
2. **Find Port Forwarding settings** (may be under "Advanced" → "Port Forwarding")
3. **Add a new rule:**
   - **External Port**: 8080
   - **Internal IP**: Your machine's local IP (e.g., 192.168.1.100)
   - **Internal Port**: 8080
   - **Protocol**: TCP

### Step 2: Update PathVoGeneratorTest.java
On the remote machine, update the URI:

```java
// Replace with your public IP
.uri(URI.create("http://50.229.159.38:8080/api/flights/process-packet"))
```

### Step 3: Test Remote Access
```bash
# From the remote machine
curl http://50.229.159.38:8080/api/flights/health
```

## Option 2: Cloud Deployment (Recommended)

### A. Heroku Deployment
1. **Create Heroku account** and install Heroku CLI
2. **Add Heroku configuration:**

```yaml
# Add to application.yml
spring:
  profiles:
    active: heroku
  data:
    mongodb:
      uri: ${MONGODB_URI}  # Set in Heroku config vars

server:
  port: ${PORT}  # Heroku sets this automatically
```

3. **Create Procfile:**
```
web: java -jar target/streaming-flight-service-0.0.1-SNAPSHOT.jar
```

4. **Deploy:**
```bash
heroku create your-streaming-flight-app
heroku config:set MONGODB_URI=your_mongodb_atlas_uri
git push heroku main
```

### B. AWS EC2 Deployment
1. **Launch EC2 instance**
2. **Configure security group** to allow port 8080
3. **Deploy application** to EC2
4. **Use EC2 public IP** in PathVoGeneratorTest.java

### C. Google Cloud Run
1. **Create Dockerfile:**
```dockerfile
FROM openjdk:11-jre-slim
COPY target/streaming-flight-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

2. **Deploy to Cloud Run:**
```bash
gcloud run deploy streaming-flight-service \
  --source . \
  --platform managed \
  --allow-unauthenticated \
  --port 8080
```

## Option 3: Reverse Proxy with Domain

### Using ngrok (Quick Testing)
1. **Install ngrok**
2. **Create tunnel:**
```bash
ngrok http 8080
```
3. **Use ngrok URL** in PathVoGeneratorTest.java:
```java
.uri(URI.create("https://abc123.ngrok.io/api/flights/process-packet"))
```

### Using Cloudflare Tunnel
1. **Install cloudflared**
2. **Create tunnel:**
```bash
cloudflared tunnel create streaming-flight
cloudflared tunnel route dns streaming-flight your-domain.com
cloudflared tunnel run streaming-flight
```

## Option 4: VPN Solution

### WireGuard VPN
1. **Set up WireGuard server** on your machine
2. **Configure remote machine** as WireGuard client
3. **Use local IP** within VPN network

## Security Considerations

### For Public IP Access:
- **Use HTTPS** (Let's Encrypt certificates)
- **Implement authentication**
- **Rate limiting**
- **Monitor access logs**

### For Cloud Deployment:
- **Use managed databases** (MongoDB Atlas)
- **Enable auto-scaling**
- **Set up monitoring** (CloudWatch, etc.)
- **Use secrets management**

## Testing Remote Access

### 1. Health Check
```bash
curl http://YOUR_PUBLIC_IP:8080/api/flights/health
```

### 2. Test Packet Processing
```bash
curl -X POST http://YOUR_PUBLIC_IP:8080/api/flights/process-packet \
  -H "Content-Type: application/json" \
  -d '{
    "listRealPath": [],
    "listFlightIntention": [],
    "time": "2025-01-15T10:00:00Z"
  }'
```

### 3. Update PathVoGeneratorTest.java
```java
// Replace localhost with your public endpoint
String baseUrl = "http://YOUR_PUBLIC_IP:8080";  // or cloud URL
// or
String baseUrl = "https://your-app.herokuapp.com";

.uri(URI.create(baseUrl + "/api/flights/process-packet"))
```

## Performance Considerations

### For High-Volume Data:
- **Use batch endpoints** for better performance
- **Implement connection pooling**
- **Consider message queues** (RabbitMQ, Kafka)
- **Use load balancers** for multiple instances

### Monitoring:
- **Application metrics** (CPU, memory, response times)
- **Database performance**
- **Network latency**
- **Error rates**

## Troubleshooting

### Common Issues:
1. **Connection timeout**: Check firewall/router settings
2. **CORS errors**: Verify @CrossOrigin configuration
3. **SSL/TLS errors**: Use HTTP for testing, HTTPS for production
4. **Performance issues**: Monitor resource usage, consider scaling

### Debug Commands:
```bash
# Check if port is accessible
telnet YOUR_PUBLIC_IP 8080

# Test with curl
curl -v http://YOUR_PUBLIC_IP:8080/api/flights/health

# Check application logs
tail -f logs/application.log
``` 