# CSV File Modification - Testing Guide

## How the Fixed Implementation Works

### Key Change:
The code now uses **`getOrSaveUploadedFile()`** instead of `saveUploadedFile()`.

**What this means:**
1. **First time you upload a file**: The file is saved to `/tmp/csv_uploads/`
2. **Second time onwards**: Instead of overwriting the saved file with the uploaded content, it **preserves the modified file** on disk and uses that
3. **Changes are persistent**: All modifications made to the file stay on disk permanently

### Algorithm:
```
If file with same name doesn't exist on disk:
    → Save the uploaded file
    
If file with same name already exists on disk:
    → Use the existing file (which has all previous modifications)
    → Don't overwrite it with the new upload
    
Then perform the operation and save changes
```

---

## Step-by-Step Testing

### Step 1: Create a Sample CSV File
```bash
cat > users.csv << 'EOF'
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
EOF
```

### Step 2: Run the Application
```bash
cd /Users/abhinav.harsh/Downloads/demo
mvn spring-boot:run
```

### Step 3: First Upload - Create New User
Upload the file and create a new user:
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
    "message": "User created successfully. File saved at: /tmp/csv_uploads/users.csv",
    "userId": 3,
    "userEmail": "bob@example.com",
    "userName": "Bob"
}
```

**Console Output:**
```
New file created at: /tmp/csv_uploads/users.csv
```

### Step 4: Verify the File Was Modified
Open the saved file:
```bash
cat /tmp/csv_uploads/users.csv
```

**Expected Output (3 users):**
```
id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane
id=3,email=bob@example.com,name=Bob
```

✅ **File has been modified!** The new user is there.

---

### Step 5: Second Operation - Update Existing User
Now upload the **SAME ORIGINAL FILE** again (not the modified one) and update user 1:
```bash
curl -X POST http://localhost:8080/users/1/update \
  -F "file=@users.csv" \
  -F "email=john.newemail@example.com" \
  -F "name=John Updated"
```

**Console Output:**
```
Using existing file at: /tmp/csv_uploads/users.csv
```

Notice it says **"Using existing file"** - it's NOT overwriting it with the uploaded file!

**Response:**
```json
{
    "message": "User with ID 1 updated successfully. File saved at: /tmp/csv_uploads/users.csv",
    "userId": 1,
    "updatedEmail": "john.newemail@example.com",
    "updatedName": "John Updated"
}
```

### Step 6: Verify Changes Persist
Check the file again:
```bash
cat /tmp/csv_uploads/users.csv
```

**Expected Output (Bob is still there, John is updated):**
```
id=1,email=john.newemail@example.com,name=John Updated
id=2,email=jane@example.com,name=Jane
id=3,email=bob@example.com,name=Bob
```

✅ **All changes are persisted!** 
- Bob (user 3) created in step 3 is still there
- John's data is updated with new email and name

---

### Step 7: Third Operation - Delete User
Delete user 2:
```bash
curl -X POST http://localhost:8080/users/2/delete \
  -F "file=@users.csv"
```

**Console Output:**
```
Using existing file at: /tmp/csv_uploads/users.csv
```

**Response:**
```json
{
    "message": "User with ID 2 deleted successfully. File saved at: /tmp/csv_uploads/users.csv",
    "deletedUserId": 2
}
```

### Step 8: Final Verification
```bash
cat /tmp/csv_uploads/users.csv
```

**Expected Output (Only John and Bob remain):**
```
id=1,email=john.newemail@example.com,name=John Updated
id=3,email=bob@example.com,name=Bob
```

✅ **Jane is gone!** All modifications are persistent!

---

## Complete Example Workflow

```bash
# Initial file
echo "id=1,email=john@example.com,name=John
id=2,email=jane@example.com,name=Jane" > users.csv

# Operation 1: Create user 3
curl -X POST http://localhost:8080/users/create \
  -F "file=@users.csv" \
  -F "id=3" -F "email=bob@example.com" -F "name=Bob"

# Now file has 3 users
cat /tmp/csv_uploads/users.csv
# Output: 3 users

# Operation 2: Update user 1 (upload original file again)
curl -X POST http://localhost:8080/users/1/update \
  -F "file=@users.csv" \
  -F "email=john.new@example.com" -F "name=John Updated"

# File still has 3 users, but user 1 is updated
cat /tmp/csv_uploads/users.csv
# Output: 3 users with updated John

# Operation 3: Delete user 2
curl -X POST http://localhost:8080/users/2/delete \
  -F "file=@users.csv"

# File now has 2 users (Bob still there!)
cat /tmp/csv_uploads/users.csv
# Output: 2 users (John and Bob, Jane is deleted)
```

---

## Key Points

### ✅ What's Fixed:
1. **First upload**: File is saved to disk
2. **Subsequent operations**: File is NOT overwritten, modifications are applied to the saved version
3. **Changes persist**: All CRUD operations modify the file on disk permanently

### ✅ Why It Works:
- `getOrSaveUploadedFile()` checks if file already exists
- If it exists, it uses the existing file (with all modifications)
- If it doesn't exist, it saves the uploaded file
- After each operation, `writeToCSVFile()` updates the saved file

### ✅ You Can Verify:
- Always check `/tmp/csv_uploads/users.csv` after each operation
- File grows when you create users
- File contents change when you update users
- Users are removed when you delete
- Previous operations' results are preserved

---

## Console Output Explanation

**First upload:**
```
New file created at: /tmp/csv_uploads/users.csv
```
→ File created on disk

**Subsequent operations:**
```
Using existing file at: /tmp/csv_uploads/users.csv
```
→ Using saved file, preserving previous changes

---

## Important Notes

1. **File Path**: `/tmp/csv_uploads/users.csv`
   - On macOS/Linux: Usually `/var/folders/...` (temp directory)
   - Files are saved with original filename

2. **Multiple Files**: 
   - If you upload `data.csv`, it creates `/tmp/csv_uploads/data.csv`
   - If you upload `users.csv`, it creates `/tmp/csv_uploads/users.csv`
   - Each file is tracked independently

3. **Modifications Are Permanent**:
   - Once created, changes stay in the file
   - Even after server restart, files remain on disk
   - You can copy the file and reuse it

4. **Upload the Original File Each Time**:
   - Always upload the original file, not the modified one
   - The system will use the saved modified version
   - This is intentional behavior - allows continuous operations

---

## Summary

The key difference is that the method now:
- **Does NOT** overwrite saved files with new uploads
- **Preserves** all previous modifications
- **Persists** changes to disk after every operation

This is exactly what you requested - the CSV file is **actually modified** on disk, not just returned as a string!

