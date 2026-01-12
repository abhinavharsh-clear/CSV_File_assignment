package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class UserService {

    // Configurable upload directory - can be set via application.properties
    @Value("${app.upload.dir:/Users/abhinav.harsh/Downloads/Testing}")
    private String uploadDir;

    /**
     * Save uploaded file to the configured directory
     * @param file the uploaded CSV file
     * @return the full file path where it was saved
     */
    public String saveUploadedFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String filePath = uploadDir + File.separator + originalFilename;

            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(uploadDir));

            // Save the file
            Files.write(Paths.get(filePath), file.getBytes());
            System.out.println("✅ File saved to: " + filePath);

            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Error saving uploaded file: " + e.getMessage());
        }
    }

    /**
     * Parse and load users from CSV file
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
     * Write users to CSV file at specified path
     * @param filePath the path where the file exists
     * @param users the list of users
     */
    public void writeToCSVFile(String filePath, List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (User u : users) {
                writer.println("id=" + u.getId()
                    + ",email=" + u.getEmail()
                    + ",name=" + u.getName());
            }
            writer.flush();
            System.out.println("✅ File updated at: " + filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to CSV file: " + e.getMessage());
        }
    }

    /* ---------- READ ---------- */
    /**
     * Get all users from the uploaded CSV file
     * The file is automatically saved to the configured directory
     * @param file the uploaded CSV file
     * @return list of users and the file path where it was saved
     */
    public Map<String, Object> getAllUsers(MultipartFile file) {
        String filePath = saveUploadedFile(file);
        List<User> users = parseCSVFile(file);

        return Map.of(
            "filePath", filePath,
            "users", users
        );
    }

    /* ---------- CREATE ---------- */
    /**
     * Create a new user - file is automatically saved to configured directory
     * @param file the uploaded CSV file
     * @param id the new user ID
     * @param email the new user email
     * @param name the new user name
     * @return success message with file path
     */
    public String createUser(MultipartFile file, int id, String email, String name) {
        String filePath = saveUploadedFile(file);
        List<User> users = parseCSVFile(file);

        // Check if user with this ID already exists
        for (User u : users) {
            if (u.getId() == id) {
                throw new RuntimeException("User with ID " + id + " already exists");
            }
        }

        // Add new user
        User newUser = new User(id, email, name);
        users.add(newUser);

        // Write updated data back to the file
        writeToCSVFile(filePath, users);

        return "User created successfully. File saved at: " + filePath;
    }

    /* ---------- UPDATE ---------- */
    /**
     * Update a user completely (PUT)
     * File is automatically saved to configured directory
     * @param file the uploaded CSV file
     * @param id the user ID to update
     * @param email new email
     * @param name new name
     * @return success message with file path
     */
    public String updateUser(MultipartFile file, int id, String email, String name) {
        String filePath = saveUploadedFile(file);
        List<User> users = parseCSVFile(file);

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

        // Write updated data back to the file
        writeToCSVFile(filePath, users);

        return "User with ID " + id + " updated successfully. File saved at: " + filePath;
    }

    /**
     * Partially update a user (PATCH)
     * File is automatically saved to configured directory
     * @param file the uploaded CSV file
     * @param id the user ID to update
     * @param email new email (optional)
     * @param name new name (optional)
     * @return success message with file path
     */
    public String patchUser(MultipartFile file, int id, String email, String name) {
        String filePath = saveUploadedFile(file);
        List<User> users = parseCSVFile(file);

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

        // Write updated data back to the file
        writeToCSVFile(filePath, users);

        return "User with ID " + id + " partially updated successfully. File saved at: " + filePath;
    }

    /* ---------- DELETE ---------- */
    /**
     * Delete a user by ID
     * File is automatically saved to configured directory
     * @param file the uploaded CSV file
     * @param id the user ID to delete
     * @return success message with file path
     */
    public String deleteUser(MultipartFile file, int id) {
        String filePath = saveUploadedFile(file);
        List<User> users = parseCSVFile(file);

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

        // Write updated data back to the file
        writeToCSVFile(filePath, users);

        return "User with ID " + id + " deleted successfully. File saved at: " + filePath;
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
     * Get the configured upload directory
     */
    public String getUploadDirectory() {
        return uploadDir;
    }
}

