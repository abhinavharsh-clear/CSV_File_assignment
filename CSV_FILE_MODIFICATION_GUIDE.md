# CSV CRUD Operations - File Modification Implementation Guide

## Overview
The application now supports uploading a CSV file with each API request, performing CRUD operations, and **saving the changes directly to the file**. Users upload the CSV file, operations are performed on it, and the modified file is saved to disk.

---

## How It Works

### Architecture Flow:
```
User Uploads CSV File
    ↓
File is saved to: /tmp/csv_uploads/[filename]
    ↓
Read the file and parse into User objects
    ↓
Perform CRUD operation (Create/Read/Update/Delete)
    ↓
Write modified User objects back to the same file
    ↓
Return success message with file path
    ↓
User can download modified file from the path
```

---

## Project Structure

### Modified Files:
1. **UserService.java** - Core service with CRUD operations and file I/O
2. **UserController.java** - REST API endpoints
3. **User.java** - Model class (unchanged, but has setId method)
4. **DemoApplication.java** - Main Spring Boot application (unchanged)

---

## Key Features

### 1. **File Upload and Storage**
- Files are uploaded and saved to `/tmp/csv_uploads/` directory
- Each file retains its original filename
- Automatically creates the directory if it doesn't exist

### 2. **File Modification**
- All operations (Create, Read, Update, Delete) modify the file on disk
- Changes are persistent
- No session or in-memory storage needed

### 3. **Error Handling**
- Clear error messages for each operation
- Validation before file operations
- Proper HTTP error responses

---

## API Endpoints and Examples

### 1. **READ - Get All Users**
```bash
curl -X POST http://localhost:8080/users/getAll \
  -F "file=@users.csv"
```

**Response (Success):**
```json
{
    "message": "Users retrieved successfully",
    "count": 3,
    "users": [
        {
            "id": 1,
            "email": "john@example.com",
            "name": "John"
        },
        {
            "id": 2,
            "email": "jane@example.com",
            "name": "Jane"
        },
        {
            "id": 3,
            "email": "bob@example.com",
            "name": "Bob"
        }
    ]
}
```

**Response (Error):**
```json
{
    "error": "Error reading CSV file: [error details]"
}
```

---

### 2. **CREATE - Add New User**
```bash
curl -X POST http://localhost:8080/users/create \
  -F "file=@users.csv" \
  -F "id=4" \
  -F "email=alice@example.com" \
  -F "name=Alice"
```

**Response (Success):**
```json
{
    "message": "User created successfully. File saved at: /tmp/csv_uploads/users.csv",
    "userId": 4,
    "userEmail": "alice@example.com",
    "userName": "Alice"
}
```

**Response (Error):**
```json
{
    "error": "User with ID 4 already exists"
}
```

**What Happens:**
- File is saved to `/tmp/csv_uploads/users.csv`
- New user is added to the file
- **File is updated on disk** with the new user

---

### 3. **UPDATE - Complete User Update**
```bash
curl -X POST http://localhost:8080/users/1/update \
  -F "file=@users.csv" \
  -F "email=newemail@example.com" \
  -F "name=NewName"
```

**Response (Success):**
```json
{
    "message": "User with ID 1 updated successfully. File saved at: /tmp/csv_uploads/users.csv",
    "userId": 1,
    "updatedEmail": "newemail@example.com",
    "updatedName": "NewName"
}
```

**Response (Error):**
```json
{
    "error": "User with ID 1 not found"
}
```

**What Happens:**
- File is saved to `/tmp/csv_uploads/users.csv`
- User with ID 1 is updated with new email and name
- **File is updated on disk** with the modified user data

---

### 4. **PATCH - Partial User Update**
```bash
curl -X POST http://localhost:8080/users/2/patch \
  -F "file=@users.csv" \
  -F "email=newemail@example.com"
```

**Response (Success):**
```json
{
    "message": "User with ID 2 partially updated successfully. File saved at: /tmp/csv_uploads/users.csv",
    "userId": 2,
    "updatedEmail": "newemail@example.com"
}
```

**What Happens:**
- File is saved to `/tmp/csv_uploads/users.csv`
- Only the provided fields (email in this case) are updated
- Name field remains unchanged
- **File is updated on disk** with the modified user data

---

### 5. **DELETE - Remove User**
```bash
curl -X POST http://localhost:8080/users/1/delete \
  -F "file=@users.csv"
```

**Response (Success):**
```json
{
    "message": "User with ID 1 deleted successfully. File saved at: /tmp/csv_uploads/users.csv",
    "deletedUserId": 1
}
```

**Response (Error):**
```json
{
    "error": "User with ID 1 not found"
}
```

**What Happens:**
- File is saved to `/tmp/csv_uploads/users.csv`
- User with ID 1 is removed from the file
- **File is updated on disk** without the deleted user

---

## CSV File Format

### Expected Format:
```
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
id=3,email=bob@example.com,name=Bob
```

### Format Rules:
- Each line represents one user record
- Fields are comma-separated
- Format: `id=VALUE,email=VALUE,name=VALUE`
- Empty lines are automatically skipped
- No header row needed

### Example files:

**users.csv:**
```
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
```

---

## Complete Code Overview

### UserService.java - Key Methods:

```java
// Save uploaded file to disk
private String saveUploadedFile(MultipartFile file)
    - Takes uploaded file and saves to /tmp/csv_uploads/
    - Returns the file path

// Parse CSV file into User objects
public List<User> parseCSVFile(String filePath)
    - Reads the file line by line
    - Converts each line to User object
    - Returns list of users

// Write User objects back to CSV file
private void writeToCSVFile(String filePath, List<User> users)
    - Takes the list of users
    - Writes them back to the file in CSV format
    - This is what makes the changes persistent

// CRUD Operations
public List<User> getAllUsers(MultipartFile file)
public String createUser(MultipartFile file, int id, String email, String name)
public String updateUser(MultipartFile file, int id, String email, String name)
public String patchUser(MultipartFile file, int id, String email, String name)
public String deleteUser(MultipartFile file, int id)
    - All accept the file as parameter
    - Save the file to disk
    - Perform operation
    - Write changes back to file
```

### UserController.java - Key Endpoints:

```java
@PostMapping("/getAll")          // Read all users
@PostMapping("/create")          // Create new user
@PostMapping("/{id}/update")     // Update complete user
@PostMapping("/{id}/patch")      // Partially update user
@PostMapping("/{id}/delete")     // Delete user
```

---

## Testing Workflow

### Step 1: Create Sample CSV File
```bash
cat > users.csv << 'EOF'
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
EOF
```

### Step 2: Run the Application
```bash
mvn spring-boot:run
```

### Step 3: Test GET All Users
```bash
curl -X POST http://localhost:8080/users/getAll \
  -F "file=@users.csv"
```
**Expected:** Returns 2 users

### Step 4: Create New User
```bash
curl -X POST http://localhost:8080/users/create \
  -F "file=@users.csv" \
  -F "id=3" \
  -F "email=bob@example.com" \
  -F "name=Bob"
```
**Expected:** File is updated with new user

### Step 5: Verify File Was Updated
```bash
cat /tmp/csv_uploads/users.csv
```
**Expected Output:**
```
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
id=3,email=bob@example.com,name=Bob
```

### Step 6: Update Existing User
```bash
curl -X POST http://localhost:8080/users/1/update \
  -F "file=@users.csv" \
  -F "email=john.new@example.com" \
  -F "name=John Updated"
```
**Expected:** User with ID 1 is updated in the file

### Step 7: Delete User
```bash
curl -X POST http://localhost:8080/users/2/delete \
  -F "file=@users.csv"
```
**Expected:** User with ID 2 is removed from the file

---

## File Locations

### Upload Directory:
```
/tmp/csv_uploads/
```

### Files Created:
```
/tmp/csv_uploads/users.csv          (if you upload users.csv)
/tmp/csv_uploads/data.csv           (if you upload data.csv)
/tmp/csv_uploads/[any-filename]     (based on original filename)
```

---

## Important Notes

✅ **Files are Persistent**
- Changes are written to disk immediately
- The file persists even after the request completes

✅ **Same Filename**
- When you upload a file with the same name, it overwrites the previous version
- This is useful for continuous operations

✅ **Error Safety**
- If an error occurs, the file is saved before the operation
- If the operation fails, the file remains intact
- You'll see an error message

✅ **No Session Required**
- Each request is independent
- No session management needed
- Stateless architecture

---

## Production Considerations

### 1. **Security**
```properties
# Add to application.properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 2. **File Cleanup**
- Implement a scheduled task to delete old files
- Add timestamp to filename to avoid overwrites
- Use user-specific directories

### 3. **Database Integration**
- Instead of files, consider storing in database
- Add transaction support
- Implement backup/recovery

### 4. **Logging**
- Add proper logging for all file operations
- Log all CRUD operations
- Track file modifications

### 5. **Authentication**
- Add user authentication
- Implement user-specific file directories
- Add authorization checks

---

## Summary

The application now:

1. ✅ Accepts CSV files with each request
2. ✅ Saves the file to `/tmp/csv_uploads/`
3. ✅ Performs CRUD operations on the file
4. ✅ **Writes changes back to the file on disk**
5. ✅ Returns success messages with file paths
6. ✅ Handles errors gracefully
7. ✅ Supports all CRUD operations (Create, Read, Update/Patch, Delete)

The key difference from the previous implementation is that **changes are now saved to the file**, not just returned as strings!

