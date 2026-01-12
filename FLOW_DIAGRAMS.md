# Visual Flow Diagrams - CSV CRUD Implementation

## 1. Overall Architecture Flow

```
┌─────────────────────────────────────────────────────────────┐
│                     User/Client                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      │ Upload CSV + Operation
                      ↓
┌─────────────────────────────────────────────────────────────┐
│                   UserController                             │
│  - Receives file parameter                                   │
│  - Receives operation parameters (id, email, name)           │
│  - Handles error responses                                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      │ Calls service method
                      ↓
┌─────────────────────────────────────────────────────────────┐
│                   UserService                                │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ getOrSaveUploadedFile()                              │   │
│  │  - Check if file exists on disk                      │   │
│  │  - If NO: Save uploaded file                         │   │
│  │  - If YES: Use existing file (preserve changes)      │   │
│  │  - Return file path                                  │   │
│  └──────────────────┬───────────────────────────────────┘   │
│                     │                                         │
│  ┌──────────────────▼───────────────────────────────────┐   │
│  │ parseCSVFile(filePath)                               │   │
│  │  - Read CSV from disk                                │   │
│  │  - Parse each line into User objects                 │   │
│  │  - Return List<User>                                 │   │
│  └──────────────────┬───────────────────────────────────┘   │
│                     │                                         │
│  ┌──────────────────▼───────────────────────────────────┐   │
│  │ CRUD Operation (create/read/update/delete)           │   │
│  │  - Perform business logic on User list               │   │
│  │  - Modify User objects                               │   │
│  └──────────────────┬───────────────────────────────────┘   │
│                     │                                         │
│  ┌──────────────────▼───────────────────────────────────┐   │
│  │ writeToCSVFile(filePath, users)                      │   │
│  │  - Open file for writing                             │   │
│  │  - Convert User objects to CSV format                │   │
│  │  - Write to disk ✅ (PERSISTENCE!)                   │   │
│  │  - Flush to ensure saved                             │   │
│  └──────────────────┬───────────────────────────────────┘   │
│                     │                                         │
│                     │ Return success message + file path      │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ↓
┌─────────────────────────────────────────────────────────────┐
│              File System: /tmp/csv_uploads/                  │
│                                                               │
│  users.csv ← UPDATED WITH CHANGES ✅                         │
│  employees.csv                                               │
│  data.csv                                                    │
└─────────────────────────────────────────────────────────────┘
                      │
                      │ Response sent to user
                      ↓
┌─────────────────────────────────────────────────────────────┐
│            Response JSON                                      │
│  {                                                            │
│    "message": "Operation successful",                        │
│    "filePath": "/tmp/csv_uploads/users.csv"                 │
│  }                                                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. File Persistence Flow (The Key Fix!)

### ❌ WRONG Way (Previous Implementation)

```
Upload 1: users.csv (id=1,2)
    ↓
Save to /tmp/csv_uploads/users.csv → File has 2 users
    ↓
Create user 3
    ↓
Write to file → File has 3 users
    ↓
Upload 2: users.csv (id=1,2) ← Original file again
    ↓
OVERWRITE /tmp/csv_uploads/users.csv → File now has ONLY 2 users ❌
    ↓
Update user 1
    ↓
Write to file → File has 2 users (LOST USER 3!) ❌
```

### ✅ RIGHT Way (Fixed Implementation)

```
Upload 1: users.csv (id=1,2)
    ↓
Check: Does /tmp/csv_uploads/users.csv exist? NO
    ↓
Save to /tmp/csv_uploads/users.csv → File has 2 users
    ↓
Create user 3
    ↓
Write to file → File has 3 users ✅
    ↓
Upload 2: users.csv (id=1,2) ← Original file again
    ↓
Check: Does /tmp/csv_uploads/users.csv exist? YES
    ↓
DON'T OVERWRITE, USE EXISTING FILE ✅
    ↓
Update user 1
    ↓
Write to file → File has 3 users with user 1 updated ✅
    ↓
File still has user 3 (created earlier) ✅
```

---

## 3. Sequential Operations

```
Operation 1: Create User 3
┌──────────────────────────────────────┐
│ Uploaded file: id=1,2                │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ getOrSaveUploadedFile()               │
│ File exists? NO → Save it             │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ Create: Add user 3                    │
│ List now: [1, 2, 3]                  │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ writeToCSVFile()                      │
│ File on disk: id=1,2,3 ✅             │
└──────────────────────────────────────┘

Operation 2: Update User 1
┌──────────────────────────────────────┐
│ Uploaded file: id=1,2 (original)      │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ getOrSaveUploadedFile()               │
│ File exists? YES → Use it             │
│ Reads from disk: id=1,2,3 ✅          │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ Update: Modify user 1's email         │
│ List now: [1(updated), 2, 3]          │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ writeToCSVFile()                      │
│ File on disk: id=1(new),2,3 ✅        │
│ User 3 is still there! ✅             │
└──────────────────────────────────────┘

Operation 3: Delete User 2
┌──────────────────────────────────────┐
│ Uploaded file: id=1,2 (original)      │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ getOrSaveUploadedFile()               │
│ File exists? YES → Use it             │
│ Reads from disk: id=1(updated),2,3    │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ Delete: Remove user 2                 │
│ List now: [1(updated), 3]             │
└──────────────────────────────────────┘
              ↓
┌──────────────────────────────────────┐
│ writeToCSVFile()                      │
│ File on disk: id=1(updated),3 ✅      │
│ User 3 is still there! ✅             │
└──────────────────────────────────────┘
```

---

## 4. File System State

```
Operation 1: Create User 3
Before:                         After:
/tmp/csv_uploads/               /tmp/csv_uploads/
(empty)                         └── users.csv
                                    ├── id=1,...
                                    ├── id=2,...
                                    └── id=3,... ✅

Operation 2: Update User 1
Before:                         After:
/tmp/csv_uploads/               /tmp/csv_uploads/
└── users.csv                   └── users.csv
    ├── id=1,...                    ├── id=1(new),...
    ├── id=2,...                    ├── id=2,...
    └── id=3,...                    └── id=3,... ✅

Operation 3: Delete User 2
Before:                         After:
/tmp/csv_uploads/               /tmp/csv_uploads/
└── users.csv                   └── users.csv
    ├── id=1(new),...               ├── id=1(new),...
    ├── id=2,...                    └── id=3,... ✅
    └── id=3,...
```

---

## 5. Code Flow Diagram

```
                        ┌─────────────────────────┐
                        │ Incoming API Request    │
                        │ POST /users/{operation} │
                        │ + CSV file              │
                        └────────────┬────────────┘
                                     │
                    ┌────────────────▼────────────────┐
                    │ UserController.operationMethod()│
                    │ - Validates file               │
                    │ - Extracts parameters          │
                    └────────────────┬────────────────┘
                                     │
                    ┌────────────────▼────────────────────────┐
                    │ UserService.operationMethod()           │
                    │ (create/read/update/delete)             │
                    └────────────────┬───────────────────────┘
                                     │
        ┌────────────────────────────┼────────────────────────────┐
        │                            │                            │
        ▼                            ▼                            ▼
┌──────────────────┐       ┌──────────────────┐        ┌─────────────────┐
│getOrSaveFile()   │       │parseCSVFile()    │        │CRUD Operations  │
│                  │       │                  │        │                 │
│Check if file     │──────▶│Read from disk    │──────▶ │Modify User      │
│exists:           │       │Parse each line   │        │objects in list  │
│ YES → Use it     │       │Convert to User   │        │based on operation│
│ NO → Save it     │       │objects           │        │                 │
└──────────────────┘       │Return List       │        └────────┬────────┘
                           └──────────────────┘                  │
                                                                  │
                                    ┌─────────────────────────────┘
                                    │
                                    ▼
                        ┌───────────────────────────────┐
                        │ writeToCSVFile()              │
                        │                               │
                        │ - Open file for writing       │
                        │ - Iterate through User list   │
                        │ - Write each user to CSV      │
                        │ - Flush data (PERSISTS!)      │
                        └───────────────┬───────────────┘
                                        │
                                        ▼
                        ┌───────────────────────────────┐
                        │ /tmp/csv_uploads/users.csv    │
                        │ File updated on disk! ✅      │
                        └───────────────┬───────────────┘
                                        │
                                        ▼
                        ┌───────────────────────────────┐
                        │ Return Success Response       │
                        │ with file path                │
                        └───────────────────────────────┘
```

---

## 6. Memory vs Disk Operations

```
In-Memory Operations:
┌──────────────────────────────────────┐
│ RAM                                  │
│                                      │
│ List<User> users = [                │
│   User(1, john@..., John),           │
│   User(2, jane@..., Jane),           │
│   User(3, bob@..., Bob)  ← Added     │
│ ]                                    │
│                                      │
│ Operations happen here               │
│ Very fast, but temporary             │
└──────────────────────────────────────┘

Disk Operations (PERSISTENCE!):
┌──────────────────────────────────────┐
│ /tmp/csv_uploads/users.csv           │
│                                      │
│ id=1,email=john@...,name=John        │
│ id=2,email=jane@...,name=Jane        │
│ id=3,email=bob@...,name=Bob ← Added  │
│                                      │
│ Changes written here                 │
│ Slow but permanent                   │
└──────────────────────────────────────┘

Key: writeToCSVFile() bridges both!
     Reads from memory ──▶ Writes to disk
```

---

## 7. Error Handling Flow

```
┌──────────────────────┐
│ Operation Request    │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────────────────────┐
│ Try to getOrSaveUploadedFile()       │
└──────────┬──────────────────┬────────┘
           │ Success          │ IOException
           ▼                  ▼
       Continue...      ┌─────────────────┐
                        │ Throw Exception │
                        │ "Error handling │
                        │  uploaded file" │
                        └─────────────────┘
                              │
                              ▼
                        ┌─────────────────────────┐
                        │ Catch in Controller     │
                        │ Return 400 Bad Request  │
                        │ with error message      │
                        └─────────────────────────┘

Try to parseCSVFile()
           │
           ├─ FileNotFound ─▶ "CSV file not found"
           ├─ IOException ──▶ "Error reading CSV"
           └─ Success ──────▶ Continue

Perform CRUD Operation
           │
           ├─ User not found ──────▶ "User with ID X not found"
           ├─ Duplicate user ──────▶ "User with ID X already exists"
           └─ Success ─────────────▶ Continue

Try to writeToCSVFile()
           │
           ├─ IOException ──▶ "Error writing to CSV"
           └─ Success ──────▶ Return Success Response
```

---

## 8. State Transition Diagram

```
                    ┌───────────────────┐
                    │ No File Uploaded  │
                    │ /tmp/csv_uploads/ │
                    │ (empty)           │
                    └─────────┬─────────┘
                              │
                    Upload CSV file
                              │
                              ▼
                    ┌───────────────────┐
                    │ File Exists       │
                    │ /tmp/csv_uploads/ │
                    │ users.csv(2 users)│
                    └─────────┬─────────┘
                              │
                    Operation: CREATE
                              │
                              ▼
                    ┌───────────────────┐
                    │ File Updated      │
                    │ /tmp/csv_uploads/ │
                    │ users.csv(3 users)│
                    └─────────┬─────────┘
                              │
                    Upload same file
                              │
                              ▼
                    ┌───────────────────┐
                    │ File Exists       │
                    │ Use it (don't     │
                    │ overwrite!)       │
                    │ Read: 3 users ✅  │
                    └─────────┬─────────┘
                              │
                    Operation: UPDATE
                              │
                              ▼
                    ┌───────────────────┐
                    │ File Updated      │
                    │ 3 users (1 changed)│
                    │ User 3 still there!│
                    └─────────┬─────────┘
                              │
                    Upload same file
                              │
                              ▼
                    ┌───────────────────┐
                    │ File Exists       │
                    │ Use it            │
                    │ Read: 3 users ✅  │
                    └─────────┬─────────┘
                              │
                    Operation: DELETE
                              │
                              ▼
                    ┌───────────────────┐
                    │ File Updated      │
                    │ 2 users remain    │
                    │ (User 3 here!)    │
                    └───────────────────┘
```

---

## Key Insight

```
The Critical Method: getOrSaveUploadedFile()

Does file exist on disk?
    │
    ├─ NO  ──▶ Save uploaded file (First time)
    │
    └─ YES ──▶ Use existing file (Preserve changes!)
    
This single check makes all the difference!

Without it: Every upload overwrites previous changes ❌
With it:    Modifications are preserved ✅
```

This is the **heart of the implementation**!

