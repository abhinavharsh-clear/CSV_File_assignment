# CSV CRUD Operations with File Upload - Implementation Guide

## Overview
The application now supports uploading a CSV file with each API request to perform CRUD operations. Users no longer need hardcoded file paths; instead, they upload the CSV file each time they want to perform an operation.

---

## Project Structure

### Files Modified

1. **UserService.java** - Service layer for handling CSV operations
2. **UserController.java** - REST API endpoints
3. **User.java** - Model class (added setId method)
4. **DemoApplication.java** - Main Spring Boot application (no changes needed)

---

## Architecture & Flow

### Current Flow:
```
User Uploads CSV File 
    ↓
UserController receives file + operation parameters
    ↓
UserService parses CSV file → converts to User objects
    ↓
Performs CRUD operation (Create/Read/Update/Delete)
    ↓
Converts updated User list back to CSV string
    ↓
Returns updated CSV content to user
```

---

## API Endpoints

### 1. **READ - Get All Users**
```
POST /users/getAll
Content-Type: multipart/form-data

Parameters:
- file: <CSV file>

Response (Success):
[
    {
        "id": 1,
        "email": "john@example.com",
        "name": "John"
    },
    {
        "id": 2,
        "email": "jane@example.com",
        "name": "Jane"
    }
]

Response (Error):
{
    "error": "File is empty" or "Error reading CSV file: ..."
}
```

### 2. **CREATE - Add New User**
```
POST /users/create
Content-Type: multipart/form-data

Parameters:
- file: <CSV file>
- id: 3
- email: "bob@example.com"
- name: "Bob"

Response (Success):
{
    "message": "User created successfully",
    "updatedCSV": "id=1,email=john@example.com,name=John\nid=2,email=jane@example.com,name=Jane\nid=3,email=bob@example.com,name=Bob\n"
}

Response (Error):
{
    "error": "User with ID 3 already exists" or "Invalid CSV format in line: ..."
}
```

### 3. **UPDATE - Update Existing User (Complete)**
```
POST /users/{id}/update
Content-Type: multipart/form-data

Parameters:
- file: <CSV file>
- id: 1 (path parameter)
- email: "newemail@example.com"
- name: "NewName"

Response (Success):
{
    "message": "User with ID 1 updated successfully",
    "updatedCSV": "id=1,email=newemail@example.com,name=NewName\nid=2,email=jane@example.com,name=Jane\n"
}

Response (Error):
{
    "error": "User with ID 1 not found"
}
```

### 4. **PATCH - Partial Update (Optional Fields)**
```
POST /users/{id}/patch
Content-Type: multipart/form-data

Parameters:
- file: <CSV file>
- id: 1 (path parameter)
- email: "newemail@example.com" (optional)
- name: "NewName" (optional)

Response (Success):
{
    "message": "User with ID 1 partially updated successfully",
    "updatedCSV": "id=1,email=newemail@example.com,name=John\nid=2,email=jane@example.com,name=Jane\n"
}

Response (Error):
{
    "error": "User with ID 1 not found"
}
```

### 5. **DELETE - Remove User**
```
POST /users/{id}/delete
Content-Type: multipart/form-data

Parameters:
- file: <CSV file>
- id: 1 (path parameter)

Response (Success):
{
    "message": "User with ID 1 deleted successfully",
    "updatedCSV": "id=2,email=jane@example.com,name=Jane\n"
}

Response (Error):
{
    "error": "User with ID 1 not found"
}
```

---

## CSV File Format

**Expected CSV Format:**
```
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
id=3,email=bob@example.com,name=Bob
```

**Format Rules:**
- Each line represents one user
- Fields are comma-separated
- Format: `id=VALUE,email=VALUE,name=VALUE`
- No spaces allowed around `=` unless they're part of the value
- Empty lines are ignored

---

## Code Flow - UserService.java

### Key Methods:

#### 1. `parseCSVFile(MultipartFile file)`
- Reads the uploaded file
- Parses each line into User objects
- Returns a List<User>

#### 2. `getAllUsers(MultipartFile file)`
- Calls parseCSVFile to get all users
- Returns the list

#### 3. `createUser(MultipartFile file, int id, String email, String name)`
- Parses CSV file
- Checks if user with same ID exists
- Adds new user to list
- Converts back to CSV string and returns

#### 4. `updateUser(MultipartFile file, int id, String email, String name)`
- Parses CSV file
- Finds user by ID
- Updates email and name
- Converts back to CSV string and returns

#### 5. `patchUser(MultipartFile file, int id, String email, String name)`
- Similar to updateUser
- But only updates fields that are provided (not null/empty)

#### 6. `deleteUser(MultipartFile file, int id)`
- Parses CSV file
- Finds and removes user by ID
- Converts back to CSV string and returns

#### 7. `convertUsersToCSV(List<User> users)`
- Helper method
- Converts User objects back to CSV format string

---

## Code Flow - UserController.java

### Key Features:

1. **All endpoints accept multipart/form-data**
   - File is uploaded with each request
   - No session or fileId needed

2. **Error Handling**
   - Try-catch blocks wrap service calls
   - Returns proper HTTP error responses
   - Error messages explain what went wrong

3. **Response Format**
   - Success responses include message and updatedCSV
   - GET endpoint returns the list of users
   - Error responses include "error" field

---

## Testing the Implementation

### Using cURL:

#### 1. Create Sample CSV File
```bash
cat > users.csv << 'EOF'
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
EOF
```

#### 2. Get All Users
```bash
curl -X POST http://localhost:8080/users/getAll \
  -F "file=@users.csv"
```

#### 3. Create New User
```bash
curl -X POST http://localhost:8080/users/create \
  -F "file=@users.csv" \
  -F "id=3" \
  -F "email=bob@example.com" \
  -F "name=Bob"
```

#### 4. Update User
```bash
curl -X POST http://localhost:8080/users/1/update \
  -F "file=@users.csv" \
  -F "email=newemail@example.com" \
  -F "name=NewName"
```

#### 5. Partially Update User
```bash
curl -X POST http://localhost:8080/users/2/patch \
  -F "file=@users.csv" \
  -F "email=newemail@example.com"
```

#### 6. Delete User
```bash
curl -X POST http://localhost:8080/users/1/delete \
  -F "file=@users.csv"
```

---

## Key Advantages

✅ **No Hardcoded File Paths**
- File is uploaded each time
- No dependency on fixed file location

✅ **Stateless Operations**
- Each request is independent
- No session management needed
- No file registry or storage required

✅ **Easy to Use**
- Simple file upload mechanism
- Clear API endpoints
- Returns updated CSV content

✅ **Multi-User Support**
- Each user works with their own file
- No conflicts between users
- No cleanup needed

✅ **Data Consistency**
- Operations performed in-memory
- Reduces disk I/O
- No race conditions

---

## Error Scenarios

### 1. Empty File
```
Error: "File is empty"
```

### 2. Invalid CSV Format
```
Error: "Invalid CSV format in line: id=abc,email=test,name=Test"
```

### 3. User Not Found
```
Error: "User with ID 99 not found"
```

### 4. Duplicate User Creation
```
Error: "User with ID 1 already exists"
```

### 5. File Read Error
```
Error: "Error reading CSV file: [detailed error message]"
```

---

## Production Considerations

For production deployment, consider:

1. **Input Validation**
   - Validate email format
   - Validate user ID uniqueness
   - Sanitize inputs

2. **File Size Limits**
   - Set max file upload size in application.properties
   ```properties
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   ```

3. **Logging**
   - Add proper logging instead of System.err
   - Log all operations for audit trail

4. **Security**
   - Add authentication/authorization
   - Implement rate limiting
   - Validate file type/content

5. **Database Integration**
   - Consider moving from CSV to database
   - Implement proper data persistence
   - Add transaction support

6. **API Documentation**
   - Add Swagger/OpenAPI annotations
   - Document all endpoints
   - Provide example requests/responses

---

## Summary of Changes

| File | Changes | Impact |
|------|---------|--------|
| **UserService.java** | Removed hardcoded file path, added file parsing logic, changed methods to accept MultipartFile parameter | Service now operates on uploaded files instead of fixed path |
| **UserController.java** | All endpoints now accept file parameter, removed fileId requirement, added new endpoints for CREATE operation | Users upload file with each request |
| **User.java** | Added setId() method | Minor enhancement for future flexibility |
| **DemoApplication.java** | No changes | Works as-is |

---

## Workflow Example

### Scenario: User wants to create a new user and then update it

**Step 1: User has a CSV file:**
```
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
```

**Step 2: Create new user (Bob)**
```bash
curl -X POST http://localhost:8080/users/create \
  -F "file=@users.csv" \
  -F "id=3" \
  -F "email=bob@example.com" \
  -F "name=Bob"
```

**Response:**
```json
{
    "message": "User created successfully",
    "updatedCSV": "id=1,email=john@example.com,name=John\nid=2,email=jane@example.com,name=Jane\nid=3,email=bob@example.com,name=Bob\n"
}
```

**Step 3: Save the updatedCSV as new file and update Bob's email**
```bash
# Save updatedCSV content to new file: users_updated.csv

curl -X POST http://localhost:8080/users/3/update \
  -F "file=@users_updated.csv" \
  -F "email=bob.new@example.com" \
  -F "name=Bob"
```

**Response:**
```json
{
    "message": "User with ID 3 updated successfully",
    "updatedCSV": "id=1,email=john@example.com,name=John\nid=2,email=jane@example.com,name=Jane\nid=3,email=bob.new@example.com,name=Bob\n"
}
```

---

## Conclusion

The application now successfully supports dynamic CSV file uploads for each API request, eliminating the need for hardcoded file paths. Users can perform all CRUD operations while working with their own uploaded files, making the system flexible and multi-user friendly.

