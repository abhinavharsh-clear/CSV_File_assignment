# Quick Reference - CSV CRUD with File Modification

## What Changed

### Original Problem:
- File path was hardcoded
- No file upload support
- Changes were not saved to file

### Solution:
- Files are uploaded with each request
- Saved to `/tmp/csv_uploads/` directory
- **Changes are written to disk** ✅

---

## Key Fix: `getOrSaveUploadedFile()` Method

```java
// BEFORE (Original - overwrites every time)
private String saveUploadedFile(MultipartFile file) {
    Files.write(Paths.get(filePath), file.getBytes()); // OVERWRITES!
    return filePath;
}

// AFTER (Fixed - preserves modifications)
private String getOrSaveUploadedFile(MultipartFile file) {
    if (!existingFile.exists()) {
        Files.write(Paths.get(filePath), file.getBytes()); // Save once
    } else {
        System.out.println("Using existing file"); // Use saved version
    }
    return filePath;
}
```

---

## Files Modified

| File | Change |
|------|--------|
| `UserService.java` | Changed to file-based storage, write changes to disk |
| `UserController.java` | Added file upload to all endpoints |
| `User.java` | Added `setId()` method |
| `DemoApplication.java` | No changes |

---

## API Usage

### 1. Create Sample File
```bash
cat > users.csv << 'EOF'
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
EOF
```

### 2. Get All Users
```bash
curl -X POST http://localhost:8080/users/getAll -F "file=@users.csv"
```

### 3. Create User
```bash
curl -X POST http://localhost:8080/users/create \
  -F "file=@users.csv" \
  -F "id=3" -F "email=bob@example.com" -F "name=Bob"
```
**File updated!** Bob is now in `/tmp/csv_uploads/users.csv`

### 4. Update User
```bash
curl -X POST http://localhost:8080/users/1/update \
  -F "file=@users.csv" \
  -F "email=john.new@example.com" -F "name=John Updated"
```
**File updated!** User 1's email and name changed

### 5. Partial Update
```bash
curl -X POST http://localhost:8080/users/2/patch \
  -F "file=@users.csv" \
  -F "email=jane.new@example.com"
```
**File updated!** Only Jane's email changed, name stays same

### 6. Delete User
```bash
curl -X POST http://localhost:8080/users/1/delete -F "file=@users.csv"
```
**File updated!** User 1 is removed

---

## Verify Changes

After any operation, check the saved file:
```bash
cat /tmp/csv_uploads/users.csv
```

You'll see all modifications applied!

---

## How It Works Step-by-Step

### First Operation:
```
1. User uploads users.csv
2. System checks: Does /tmp/csv_uploads/users.csv exist? NO
3. System: Save the uploaded file
4. Perform operation (e.g., create user)
5. Write all users to /tmp/csv_uploads/users.csv
6. File now has changes
```

### Second Operation (Upload same original file):
```
1. User uploads users.csv (original, unmodified)
2. System checks: Does /tmp/csv_uploads/users.csv exist? YES
3. System: Don't overwrite, use existing file (has previous changes!)
4. Perform operation (e.g., update user)
5. Write all users to /tmp/csv_uploads/users.csv
6. File updated with new changes + previous changes
```

---

## Understanding File Locations

```
Your Desktop/Laptop:
├── users.csv           ← Original file, never modified by system

System's Upload Directory:
├── /tmp/csv_uploads/
│   └── users.csv      ← Modified by system, grows/changes with operations
```

**Important:** System modifies its copy, not your original! You can reuse the original file repeatedly.

---

## Expected Behavior

| Operation | File Before | File After |
|-----------|------------|-----------|
| Create user 3 | 2 users | 3 users |
| Update user 1 | User 1 data old | User 1 data new |
| Delete user 2 | User 2 exists | User 2 gone |
| Get all users | - | Returns all users |

---

## Common Questions

### Q: Why upload original file each time?
**A:** So you can perform multiple operations. The system uses the saved modified version, not the upload.

### Q: Where are files saved?
**A:** `/tmp/csv_uploads/` (temp directory)

### Q: Are changes permanent?
**A:** Yes! Until you manually delete `/tmp/csv_uploads/users.csv`

### Q: Can I download the modified file?
**A:** Yes, from `/tmp/csv_uploads/users.csv`

### Q: What if I upload a different file name?
**A:** It's treated as a new file: `/tmp/csv_uploads/data.csv`

---

## Test Checklist

- [ ] Start the application
- [ ] Create users.csv with 2 users
- [ ] Create user 3 (verify file has 3 users)
- [ ] Update user 1 (verify changes in file)
- [ ] Delete user 2 (verify removed from file)
- [ ] Check `/tmp/csv_uploads/users.csv` matches expectations

---

## Files in Project

```
demo/
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java        (No changes)
│   ├── controller/
│   │   └── UserController.java     (✏️ Modified)
│   ├── model/
│   │   └── User.java               (✏️ Modified - added setId)
│   └── service/
│       └── UserService.java        (✏️ Modified - file I/O)
├── IMPLEMENTATION_SUMMARY.md       (Detailed explanation)
├── TESTING_GUIDE.md               (Step-by-step tests)
└── pom.xml                         (No changes)
```

---

## The Fix in One Sentence

Changed from `saveUploadedFile()` which overwrites every time, to `getOrSaveUploadedFile()` which saves once and then modifies the same file on disk! 

✅ **Now changes are actually written to the CSV file!**

