#!/bin/bash

# MongoDB Restore Script from tar backup
echo "Restoring MongoDB from aviation_db_backup.tar.gz..."

# Stop existing container if running
docker stop aviation_mongodb_restored 2>/dev/null || true
docker rm aviation_mongodb_restored 2>/dev/null || true

# Create new container
echo "Creating new MongoDB container..."
docker run -d --name aviation_mongodb_restored -p 27018:27017 mongo:latest

# Wait for container to start
echo "Waiting for MongoDB to initialize..."
sleep 10

# Restore data from backup
echo "Restoring database files..."
docker run --rm --volumes-from aviation_mongodb_restored -v $(pwd):/backup mongo:latest tar -xzf /backup/aviation_db_backup.tar.gz -C /

# Restart container to load restored data
echo "Restarting container to load restored data..."
docker restart aviation_mongodb_restored

echo "Restore completed!"
echo "MongoDB restored container is running on port 27018"
echo "Original container (if running) is still on port 27017"
