# Quick Remote Access Setup with ngrok

## Step 1: Install ngrok
1. Go to https://ngrok.com/
2. Sign up for a free account
3. Download ngrok for Windows
4. Extract to a folder (e.g., `C:\ngrok`)

## Step 2: Authenticate ngrok
```bash
# Open Command Prompt as Administrator
cd C:\ngrok
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

## Step 3: Start your Spring Boot application
```bash
# In your project directory
mvn spring-boot:run
```

## Step 4: Create ngrok tunnel
```bash
# In a new Command Prompt
cd C:\ngrok
ngrok http 8080
```

You'll see output like:
```
Forwarding    https://abc123.ngrok.io -> http://localhost:8080
```

## Step 5: Update PathVoGeneratorTest.java
On the remote machine, replace:
```java
.uri(URI.create("http://localhost:8080/api/flights/process-packet"))
```

With:
```java
.uri(URI.create("https://abc123.ngrok.io/api/flights/process-packet"))
```

## Step 6: Test the connection
From the remote machine:
```bash
curl https://abc123.ngrok.io/api/flights/health
```

## Important Notes:
- **Free ngrok** has limitations (40 connections/minute)
- **URL changes** each time you restart ngrok (unless you have a paid plan)
- **Use for testing only** - not recommended for production
- **Keep ngrok running** while testing

## For Production:
Use one of the cloud deployment options from DEPLOYMENT_GUIDE.md 