# MongoDB Setup Guide - 3 Options

## Option 1: Docker (Recommended & Easiest)

### Prerequisites
- Docker and Docker Compose installed

### Steps
```bash
# Navigate to project directory
cd /Users/abhinav.harsh/Downloads/demo

# Start MongoDB using docker-compose
docker-compose up -d

# Verify MongoDB is running
docker ps | grep mongodb

# View MongoDB logs
docker-compose logs -f mongodb

# Stop MongoDB when done
docker-compose down

# Access MongoDB Express UI (optional)
# Open browser: http://localhost:8081
```

### Connection String
```
mongodb://admin:password123@localhost:27017/csv_crud_db
```

---

## Option 2: Homebrew (macOS)

### Prerequisites
- Homebrew installed

### Steps
```bash
# Install MongoDB Community Edition
brew tap mongodb/brew
brew install mongodb-community

# Create MongoDB data directory
mkdir -p /usr/local/var/mongodb
mkdir -p /usr/local/var/log/mongodb
chown $(whoami) /usr/local/var/mongodb
chown $(whoami) /usr/local/var/log/mongodb

# Start MongoDB as a service
brew services start mongodb-community

# Verify MongoDB is running
brew services list | grep mongodb

# Connect to MongoDB
mongosh

# Stop MongoDB when done
brew services stop mongodb-community
```

### Connection String
```
mongodb://localhost:27017/csv_crud_db
```

---

## Option 3: Manual Download & Run

### Steps
```bash
# Download MongoDB from https://www.mongodb.com/try/download/community
# Extract to desired location (e.g., /usr/local/mongodb)

# Create data directory
mkdir -p ~/mongodb_data

# Start MongoDB
/usr/local/mongodb/bin/mongod --dbpath ~/mongodb_data

# In another terminal, connect
/usr/local/mongodb/bin/mongosh
```

### Connection String
```
mongodb://localhost:27017/csv_crud_db
```

---

## Configuration in Application

### For No Authentication (Option 2 & 3)
Edit `application.properties`:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/csv_crud_db
```

### For Authentication (Option 1 - Docker)
Edit `application.properties`:
```properties
spring.data.mongodb.uri=mongodb://admin:password123@localhost:27017/csv_crud_db
```

---

## Verify MongoDB Connection

### Using mongosh (if installed)
```bash
mongosh mongodb://localhost:27017/csv_crud_db

# List databases
show dbs

# Switch to csv_crud_db
use csv_crud_db

# List collections
show collections

# Exit
exit
```

### From Java Application
When you run the Spring Boot app, check the logs:
```
✅ Successfully connected to MongoDB
✅ Database: csv_crud_db
✅ Collections available
```

---

## Testing the Connection

Once MongoDB is running and app is started:

```bash
# Upload a CSV file
curl -X POST http://localhost:8080/users/getAll \
  -F "file=@users.csv"

# Should return:
{
    "message": "Users retrieved successfully",
    "filename": "users.csv",
    "mongoDbId": "507f1f77bcf86cd799439011",
    "count": 2,
    "users": [...],
    "note": "File stored in MongoDB. Use filename in subsequent requests."
}
```

The `mongoDbId` is the MongoDB ObjectId - confirm MongoDB is working!

---

## Troubleshooting

### MongoDB Connection Refused
```
Solution: Make sure MongoDB server is running on port 27017
```

### Authentication Failed (Option 1)
```
Solution: Use credentials from docker-compose.yml (admin/password123)
```

### Database Not Found
```
Solution: MongoDB creates database automatically on first insert
```

### Port 27017 Already in Use
```
Solution: Kill process: lsof -ti:27017 | xargs kill -9
Or change MongoDB port in configuration
```

---

## Recommended Setup

For development/testing: **Use Docker (Option 1)**
- Easy to start/stop
- Reproducible environment
- Self-contained
- No pollution of system

For production: Use managed MongoDB service (Atlas, AWS, etc.)

---

## Next Steps

1. Choose an option above
2. Install/Start MongoDB
3. Update `application.properties` if needed
4. Run the Spring Boot application
5. Test the API endpoints

