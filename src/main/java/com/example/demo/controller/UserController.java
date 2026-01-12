package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    /* ---------- READ ---------- */
    /**
     * Get all users from the uploaded CSV file
     * File is automatically saved to Downloads directory
     * @param file the CSV file uploaded with the request
     * @return list of all users and the file path
     */
    @PostMapping("/getAll")
    public ResponseEntity<?> getAllUsers(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Map<String, Object> result = service.getAllUsers(file);
            String filePath = (String) result.get("filePath");
            List<User> users = (List<User>) result.get("users");

            return ResponseEntity.ok(Map.of(
                    "message", "Users retrieved successfully",
                    "filePath", filePath,
                    "count", users.size(),
                    "users", users
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /* ---------- CREATE ---------- */
    /**
     * Create a new user in the CSV file
     * File is automatically saved to Downloads directory
     * @param file the CSV file uploaded with the request
     * @param id the new user ID
     * @param email the new user email
     * @param name the new user name
     * @return success message with file path
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestParam("file") MultipartFile file,
            @RequestParam int id,
            @RequestParam String email,
            @RequestParam String name
    ) {
        try {
            String message = service.createUser(file, id, email, name);
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "userId", id,
                    "userEmail", email,
                    "userName", name,
                    "operation", "CREATE"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /* ---------- UPDATE ---------- */
    /**
     * Update a user completely (PUT)
     * File is automatically saved to Downloads directory
     * @param file the CSV file uploaded with the request
     * @param id the user ID to update
     * @param email new email
     * @param name new name
     * @return success message with file path
     */
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateUser(
            @RequestParam("file") MultipartFile file,
            @PathVariable int id,
            @RequestParam String email,
            @RequestParam String name
    ) {
        try {
            String message = service.updateUser(file, id, email, name);
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "userId", id,
                    "updatedEmail", email,
                    "updatedName", name,
                    "operation", "UPDATE"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Partially update a user (PATCH)
     * File is automatically saved to Downloads directory
     * @param file the CSV file uploaded with the request
     * @param id the user ID to update
     * @param email new email (optional)
     * @param name new name (optional)
     * @return success message with file path
     */
    @PostMapping("/{id}/patch")
    public ResponseEntity<?> patchUser(
            @RequestParam("file") MultipartFile file,
            @PathVariable int id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name
    ) {
        try {
            String message = service.patchUser(file, id, email, name);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", message);
            response.put("userId", id);
            response.put("operation", "PATCH");
            if (email != null && !email.isEmpty()) {
                response.put("updatedEmail", email);
            }
            if (name != null && !name.isEmpty()) {
                response.put("updatedName", name);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /* ---------- DELETE ---------- */
    /**
     * Delete a user by ID
     * File is automatically saved to Downloads directory
     * @param file the CSV file uploaded with the request
     * @param id the user ID to delete
     * @return success message with file path
     */
    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteUser(
            @RequestParam("file") MultipartFile file,
            @PathVariable int id
    ) {
        try {
            String message = service.deleteUser(file, id);
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "deletedUserId", id,
                    "operation", "DELETE"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}

