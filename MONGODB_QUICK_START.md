# 🚀 MongoDB Integration - Quick Start (5 Minutes)

## What's New?

Your CSV CRUD app now uses **MongoDB instead of file system**!

```
Before: Upload CSV → Save to /Users/abhinav.harsh/Downloads/
After:  Upload CSV → Store in MongoDB database
```

---

## Quick Setup

### 1. Install MongoDB (2 minutes)
```bash
# macOS with Homebrew
brew install mongodb-community

# Start MongoDB
brew services start mongodb-community

# Verify (should show running)
brew services list | grep mongodb
```

### 2. Build Application (30 seconds)
```bash
cd /Users/abhinav.harsh/Downloads/demo
mvn clean package
```

✅ **BUILD SUCCESS**

### 3. Run Application (10 seconds)
```bash
mvn spring-boot:run
# or
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

✅ **Application starts on port 8080**

---

## Test It (2 minutes)

### Create test file
```bash
cat > /tmp/users.csv << 'EOF'
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
EOF
```

### Upload to MongoDB
```bash
curl -X POST http://localhost:8080/users/getAll \
  -F "file=@/tmp/users.csv"
```

✅ **Response includes `mongoDbId` - confirms MongoDB storage!**

### Create new user
```bash
curl -X POST http://localhost:8080/users/create \
  -F "filename=users.csv" \
  -F "id=3" -F "email=bob@example.com" -F "name=Bob"
```

✅ **User 3 added to MongoDB**

### Update user
```bash
curl -X POST http://localhost:8080/users/1/update \
  -F "filename=users.csv" \
  -F "email=john.new@example.com" -F "name=John Updated"
```

✅ **User 1 updated in MongoDB**

### Verify in MongoDB
```bash
mongosh mongodb://localhost:27017/csv_crud_db
csv_crud_db> db.csv_files.find().pretty()
```

✅ **See all operations reflected in database!**

---

## Key Changes

| What | How | Example |
|------|-----|---------|
| Upload | POST /users/getAll | `curl -F "file=@users.csv" ...` |
| Create | POST /users/create | `curl -F "filename=users.csv" -F "id=3" ...` |
| Update | POST /users/{id}/update | `curl -F "filename=users.csv" ...` |
| Delete | POST /users/{id}/delete | `curl -F "filename=users.csv" ...` |
| Info | GET /users/info/{filename} | `curl http://localhost:8080/users/info/users.csv` |

---

## API Differences

### Before (File System)
```bash
curl -X POST http://localhost:8080/users/create \
  -F "file=@users.csv" \        # Upload file
  -F "id=3" -F "email=..." -F "name=..."
```

### After (MongoDB)
```bash
curl -X POST http://localhost:8080/users/create \
  -F "filename=users.csv" \     # Use filename from first upload
  -F "id=3" -F "email=..." -F "name=..."
```

**Key Difference:** Use `filename` instead of uploading file again!

---

## MongoDB Basics

### Connect to MongoDB
```bash
mongosh mongodb://localhost:27017/csv_crud_db
```

### View all files
```javascript
db.csv_files.find().pretty()
```

### Find specific file
```javascript
db.csv_files.findOne({ filename: "users.csv" })
```

### Count users in file
```javascript
db.csv_files.findOne({ filename: "users.csv" }).users.length
```

### Exit MongoDB
```javascript
exit
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Connection refused | Start MongoDB: `brew services start mongodb-community` |
| File not found in DB | Upload file first with `/getAll` endpoint |
| Port 27017 in use | Kill: `lsof -ti:27017 \| xargs kill -9` |
| Build fails | Clean cache: `mvn clean` then `mvn package` |
| App won't start | Check MongoDB is running: `pgrep mongod` |

---

## Complete Workflow

```bash
# Terminal 1: Start MongoDB
brew services start mongodb-community

# Terminal 2: Start Application
cd /Users/abhinav.harsh/Downloads/demo
mvn spring-boot:run

# Terminal 3: Test
# 1. Create CSV
cat > /tmp/users.csv << 'EOF'
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
EOF

# 2. Upload (stores in MongoDB)
curl -X POST http://localhost:8080/users/getAll \
  -F "file=@/tmp/users.csv"
# Note the mongoDbId!

# 3. Create user 3
curl -X POST http://localhost:8080/users/create \
  -F "filename=users.csv" \
  -F "id=3" -F "email=bob@example.com" -F "name=Bob"

# 4. Update user 1
curl -X POST http://localhost:8080/users/1/update \
  -F "filename=users.csv" \
  -F "email=john.new@example.com" -F "name=John Updated"

# 5. Delete user 2
curl -X POST http://localhost:8080/users/2/delete \
  -F "filename=users.csv"

# 6. Check info
curl -X GET http://localhost:8080/users/info/users.csv

# 7. Verify in MongoDB
mongosh mongodb://localhost:27017/csv_crud_db
csv_crud_db> db.csv_files.find().pretty()
# Should show users 1 and 3 only, with user 1 updated
```

---

## What You Get

✅ **Persistent Storage** - Data survives app restarts  
✅ **Metadata Tracking** - Upload/modification timestamps  
✅ **No File System** - Works on any OS  
✅ **Scalable** - Handle unlimited files  
✅ **Secure** - Database authentication support  
✅ **Query Powerful** - Full MongoDB query language  

---

## Next: Full Documentation

Read these for more details:

1. **MONGODB_SETUP.md** - Detailed installation options
2. **MONGODB_INTEGRATION_GUIDE.md** - Technical deep dive
3. **MONGODB_TESTING_GUIDE.md** - Complete test scenarios
4. **MONGODB_COMPLETE_SUMMARY.md** - Full overview

---

## Status

✅ Build: SUCCESS  
✅ Code: COMPILED  
✅ Ready: YES  
✅ Next: Run and test!  

---

**5-minute setup complete!** 🎉

Your CSV CRUD app is now MongoDB-enabled!

**Start with: `mvn spring-boot:run`**

