package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.CsvFile;
import com.example.demo.repository.CsvFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final CsvFileRepository csvFileRepository;

    public UserService(CsvFileRepository csvFileRepository) {
        this.csvFileRepository = csvFileRepository;
    }

    /**
     * Parse and load users from uploaded CSV file
     * @param file the uploaded CSV file
     * @return list of users parsed from the file
     */
    public List<User> parseCSVFile(MultipartFile file) {
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    users.add(parseLineToUser(line));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage());
        }

        return users;
    }

    /**
     * Convert CSV content to string for storage
     * @param file the uploaded CSV file
     * @return CSV content as string
     */
    private String getCsvContent(MultipartFile file) {
        try {
            return new String(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file content: " + e.getMessage());
        }
    }

    /**
     * Write users to CSV string format
     * @param users the list of users
     * @return CSV formatted string
     */
    private String convertUsersToCSV(List<User> users) {
        StringBuilder csv = new StringBuilder();
        for (User u : users) {
            csv.append("id=").append(u.getId())
                    .append(",email=").append(u.getEmail())
                    .append(",name=").append(u.getName())
                    .append("\n");
        }
        return csv.toString();
    }

    /* ---------- READ ---------- */
    /**
     * Get all users from the uploaded CSV file
     * Uploads file to MongoDB
     * @param file the uploaded CSV file
     * @return map with filename, users, and MongoDB ID
     */
    public Map<String, Object> getAllUsers(MultipartFile file) {
        String filename = file.getOriginalFilename();
        List<User> users = parseCSVFile(file);
        String csvContent = getCsvContent(file);

        // Check if file already exists in DB
        Optional<CsvFile> existingFile = csvFileRepository.findByFilename(filename);

        CsvFile csvFile;
        if (existingFile.isPresent()) {
            // Update existing file with new content
            csvFile = existingFile.get();
            csvFile.setUsers(users);
            csvFile.setCsvContent(csvContent);
            System.out.println("✅ File already exists in DB, updating: " + filename);
        } else {
            // Create new file in DB
            csvFile = new CsvFile(filename, users, csvContent);
            System.out.println("✅ New file saved to MongoDB: " + filename);
        }

        csvFileRepository.save(csvFile);

        return Map.of(
            "filename", filename,
            "fileId", csvFile.getId(),
            "users", users
        );
    }

    /* ---------- CREATE ---------- */
    /**
     * Create a new user
     * @param filename the filename to fetch from DB
     * @param id the new user ID
     * @param email the new user email
     * @param name the new user name
     * @return success message
     */
    public String createUser(String filename, int id, String email, String name) {
        // Fetch from MongoDB
        Optional<CsvFile> csvFileOpt = csvFileRepository.findByFilename(filename);
        if (csvFileOpt.isEmpty()) {
            throw new RuntimeException("File not found in database: " + filename);
        }

        CsvFile csvFile = csvFileOpt.get();
        List<User> users = csvFile.getUsers();

        // Check if user with this ID already exists
        for (User u : users) {
            if (u.getId() == id) {
                throw new RuntimeException("User with ID " + id + " already exists");
            }
        }

        // Add new user
        User newUser = new User(id, email, name);
        users.add(newUser);

        // Update in MongoDB
        csvFile.setUsers(users);
        csvFile.setCsvContent(convertUsersToCSV(users));
        csvFileRepository.save(csvFile);

        return "User created successfully. Stored in MongoDB: " + filename;
    }

    /* ---------- UPDATE ---------- */
    /**
     * Update a user completely (PUT)
     * @param filename the filename to fetch from DB
     * @param id the user ID to update
     * @param email new email
     * @param name new name
     * @return success message
     */
    public String updateUser(String filename, int id, String email, String name) {
        // Fetch from MongoDB
        Optional<CsvFile> csvFileOpt = csvFileRepository.findByFilename(filename);
        if (csvFileOpt.isEmpty()) {
            throw new RuntimeException("File not found in database: " + filename);
        }

        CsvFile csvFile = csvFileOpt.get();
        List<User> users = csvFile.getUsers();

        boolean found = false;
        for (User u : users) {
            if (u.getId() == id) {
                u.setEmail(email);
                u.setName(name);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("User with ID " + id + " not found");
        }

        // Update in MongoDB
        csvFile.setUsers(users);
        csvFile.setCsvContent(convertUsersToCSV(users));
        csvFileRepository.save(csvFile);

        return "User with ID " + id + " updated successfully. Updated in MongoDB: " + filename;
    }

    /**
     * Partially update a user (PATCH)
     * @param filename the filename to fetch from DB
     * @param id the user ID to update
     * @param email new email (optional)
     * @param name new name (optional)
     * @return success message
     */
    public String patchUser(String filename, int id, String email, String name) {
        // Fetch from MongoDB
        Optional<CsvFile> csvFileOpt = csvFileRepository.findByFilename(filename);
        if (csvFileOpt.isEmpty()) {
            throw new RuntimeException("File not found in database: " + filename);
        }

        CsvFile csvFile = csvFileOpt.get();
        List<User> users = csvFile.getUsers();

        boolean found = false;
        for (User u : users) {
            if (u.getId() == id) {
                if (email != null && !email.isEmpty()) {
                    u.setEmail(email);
                }
                if (name != null && !name.isEmpty()) {
                    u.setName(name);
                }
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("User with ID " + id + " not found");
        }

        // Update in MongoDB
        csvFile.setUsers(users);
        csvFile.setCsvContent(convertUsersToCSV(users));
        csvFileRepository.save(csvFile);

        return "User with ID " + id + " partially updated successfully. Updated in MongoDB: " + filename;
    }

    /* ---------- DELETE ---------- */
    /**
     * Delete a user by ID
     * @param filename the filename to fetch from DB
     * @param id the user ID to delete
     * @return success message
     */
    public String deleteUser(String filename, int id) {
        // Fetch from MongoDB
        Optional<CsvFile> csvFileOpt = csvFileRepository.findByFilename(filename);
        if (csvFileOpt.isEmpty()) {
            throw new RuntimeException("File not found in database: " + filename);
        }

        CsvFile csvFile = csvFileOpt.get();
        List<User> users = csvFile.getUsers();

        boolean found = false;
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User u = iterator.next();
            if (u.getId() == id) {
                iterator.remove();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("User with ID " + id + " not found");
        }

        // Update in MongoDB
        csvFile.setUsers(users);
        csvFile.setCsvContent(convertUsersToCSV(users));
        csvFileRepository.save(csvFile);

        return "User with ID " + id + " deleted successfully. Updated in MongoDB: " + filename;
    }

    /* ---------- HELPERS ---------- */

    /**
     * Parse a CSV line to a User object
     * Expected format: id=1,email=test@example.com,name=TestName
     */
    private User parseLineToUser(String line) {
        Map<String, String> map = new HashMap<>();

        String[] pairs = line.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }

        try {
            return new User(
                    Integer.parseInt(map.get("id")),
                    map.get("email"),
                    map.get("name")
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid CSV format in line: " + line);
        }
    }

    /**
     * Get file info from MongoDB
     * @param filename the filename to search
     * @return map with file details
     */
    public Map<String, Object> getFileInfo(String filename) {
        Optional<CsvFile> csvFileOpt = csvFileRepository.findByFilename(filename);
        if (csvFileOpt.isEmpty()) {
            throw new RuntimeException("File not found in database: " + filename);
        }

        CsvFile csvFile = csvFileOpt.get();
        return Map.of(
            "id", csvFile.getId(),
            "filename", csvFile.getFilename(),
            "userCount", csvFile.getUsers().size(),
            "uploadedAt", csvFile.getUploadedAt(),
            "lastModified", csvFile.getLastModified()
        );
    }
}

