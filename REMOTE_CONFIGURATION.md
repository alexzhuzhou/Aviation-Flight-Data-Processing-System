# Remote Configuration Guide for PathVoGeneratorTest.java

## Overview
This guide shows you exactly how to modify `PathVoGeneratorTest.java` when it's running on a remote machine to connect to your streaming flight data system.

## Key Changes Required

### 1. Add Remote URL Configuration
Add this constant at the top of your class (after the logger declaration):

```java
// Configuration for remote access
private static final String REMOTE_BASE_URL = "http://50.229.159.38:8080"; // Replace with your endpoint
```

### 2. Replace All localhost URLs
Find all instances of `http://localhost:8080` and replace them with `REMOTE_BASE_URL + "/api/flights/..."`.

## Complete Modified PathVoGeneratorTest.java

Here are the specific changes you need to make to your existing `PathVoGeneratorTest.java`:

### Step 1: Add the Remote URL Constant
```java
Logger log = LoggerFactory.getLogger(getClass());

// ADD THIS LINE - Configuration for remote access
private static final String REMOTE_BASE_URL = "http://50.229.159.38:8080"; // Replace with your endpoint
```

### Step 2: Update the Single Packet Processing Method
In the `_00_run()` method, change:
```java
// OLD CODE:
.uri(URI.create("http://localhost:8080/api/flights/process-packet"))

// NEW CODE:
.uri(URI.create(REMOTE_BASE_URL + "/api/flights/process-packet"))
```

And:
```java
// OLD CODE:
.uri(URI.create("http://localhost:8080/api/flights/stats"))

// NEW CODE:
.uri(URI.create(REMOTE_BASE_URL + "/api/flights/stats"))
```

### Step 3: Update the Batch Processing Method
In the `_01_runBatch()` method, change:
```java
// OLD CODE:
.uri(URI.create("http://localhost:8080/api/flights/process-batch-packets"))

// NEW CODE:
.uri(URI.create(REMOTE_BASE_URL + "/api/flights/process-batch-packets"))
```

And:
```java
// OLD CODE:
.uri(URI.create("http://localhost:8080/api/flights/stats"))

// NEW CODE:
.uri(URI.create(REMOTE_BASE_URL + "/api/flights/stats"))
```

## Different Remote URL Options

### Option 1: Public IP (if you set up port forwarding)
```java
private static final String REMOTE_BASE_URL = "http://50.229.159.38:8080";
```

### Option 2: ngrok (for testing)
```java
private static final String REMOTE_BASE_URL = "https://abc123.ngrok.io";
```

### Option 3: Heroku (cloud deployment)
```java
private static final String REMOTE_BASE_URL = "https://your-app-name.herokuapp.com";
```

### Option 4: AWS EC2
```java
private static final String REMOTE_BASE_URL = "http://your-ec2-public-ip:8080";
```

### Option 5: Google Cloud Run
```java
private static final String REMOTE_BASE_URL = "https://your-service-url.a.run.app";
```

## Testing the Connection

Add this test method to verify connectivity:

```java
@Test
public void _02_testConnectivity() throws Exception {
    HttpClient httpClient = HttpClient.newHttpClient();
    
    // Test health endpoint
    HttpRequest healthRequest = HttpRequest.newBuilder()
        .uri(URI.create(REMOTE_BASE_URL + "/api/flights/health"))
        .GET()
        .build();
    
    HttpResponse<String> healthResponse = httpClient.send(healthRequest, 
        HttpResponse.BodyHandlers.ofString());
    
    log.info("Health check response: {} - {}", 
        healthResponse.statusCode(), healthResponse.body());
    
    // Test stats endpoint
    HttpRequest statsRequest = HttpRequest.newBuilder()
        .uri(URI.create(REMOTE_BASE_URL + "/api/flights/stats"))
        .GET()
        .build();
    
    HttpResponse<String> statsResponse = httpClient.send(statsRequest, 
        HttpResponse.BodyHandlers.ofString());
    
    log.info("Stats response: {} - {}", 
        statsResponse.statusCode(), statsResponse.body());
}
```

## Quick Setup Steps

### For Testing (ngrok):
1. **On your machine**: Start ngrok: `ngrok http 8080`
2. **Copy the HTTPS URL** from ngrok output
3. **On remote machine**: Update `REMOTE_BASE_URL` with the ngrok URL
4. **Test**: Run the `_02_testConnectivity()` method first

### For Production (Public IP):
1. **Configure router** to forward port 8080 to your machine
2. **On remote machine**: Update `REMOTE_BASE_URL` with your public IP
3. **Test**: Run the connectivity test

### For Cloud Deployment:
1. **Deploy** your application to cloud (Heroku, AWS, etc.)
2. **On remote machine**: Update `REMOTE_BASE_URL` with cloud URL
3. **Test**: Run the connectivity test

## Troubleshooting

### Common Issues:

1. **Connection Refused**
   - Check if your streaming service is running
   - Verify the URL is correct
   - Check firewall settings

2. **Timeout**
   - Check network connectivity
   - Verify the remote service is accessible
   - Try the connectivity test first

3. **SSL/TLS Errors** (with ngrok)
   - ngrok uses HTTPS, which is normal
   - Make sure you're using the HTTPS URL from ngrok

### Debug Commands:
From the remote machine, test connectivity:
```bash
# Test health endpoint
curl http://50.229.159.38:8080/api/flights/health

# Test with ngrok
curl https://abc123.ngrok.io/api/flights/health
```

## Performance Considerations

### For High-Volume Data:
- Use the batch processing method (`_01_runBatch()`) for better performance
- Consider increasing batch size if network allows
- Monitor response times and adjust accordingly

### Network Optimization:
- The HTTP client already uses connection pooling
- Consider adding timeout configurations if needed
- Monitor for network latency issues 

---

## How to Check if Port Forwarding is Set Up

1. **Test from an External Network:**
   - On a device NOT connected to your home/office WiFi (e.g., your phone on mobile data, or a remote server), open a browser and go to:
     ```
     http://50.229.159.38:8080/api/flights/health
     ```
   - Or, from a remote terminal:
     ```
     curl http://50.229.159.38:8080/api/flights/health
     ```
   - If you see `Streaming Flight Service is running`, port forwarding is working!

2. **If You Get a Timeout or Connection Refused:**
   - Port forwarding is NOT set up, or your firewall is blocking the connection.

---

## How to Set Up Port Forwarding (if not already done)

1. **Log in to your router’s admin page** (usually at http://192.168.1.1 or http://192.168.0.1).
2. **Find the Port Forwarding section** (sometimes under “Advanced” or “NAT”).
3. **Add a rule:**
   - **External Port:** 8080
   - **Internal IP:** (your computer’s local IP, e.g., 192.168.1.100)
   - **Internal Port:** 8080
   - **Protocol:** TCP
4. **Save and apply the changes.**
5. **Make sure your Windows Firewall allows inbound connections on port 8080.**

---

## Alternative: Use ngrok for Instant Remote Access

If you don’t want to mess with router settings, you can use ngrok (see `setup_ngrok.md`). It will give you a public URL instantly.

---

**Let me know if you want step-by-step help with port forwarding or ngrok!** 