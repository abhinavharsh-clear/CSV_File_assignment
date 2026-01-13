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
     * File is automatically stored in MongoDB
     * @param file the CSV file uploaded with the request
     * @return list of all users and MongoDB file ID
     */
    @PostMapping("/getAll")
    public ResponseEntity<?> getAllUsers(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Map<String, Object> result = service.getAllUsers(file);
            String filename = (String) result.get("filename");
            String fileId = (String) result.get("fileId");
            List<User> users = (List<User>) result.get("users");

            return ResponseEntity.ok(Map.of(
                    "message", "Users retrieved successfully",
                    "filename", filename,
                    "mongoDbId", fileId,
                    "count", users.size(),
                    "users", users,
                    "note", "File stored in MongoDB. Use filename in subsequent requests."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /* ---------- CREATE ---------- */
    /**
     * Create a new user in MongoDB
     * @param filename the CSV filename (must be previously uploaded)
     * @param id the new user ID
     * @param email the new user email
     * @param name the new user name
     * @return success message
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestParam String filename,
            @RequestParam int id,
            @RequestParam String email,
            @RequestParam String name
    ) {
        try {
            String message = service.createUser(filename, id, email, name);
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "filename", filename,
                    "userId", id,
                    "userEmail", email,
                    "userName", name,
                    "operation", "CREATE",
                    "storage", "MongoDB"
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
     * @param filename the CSV filename stored in MongoDB
     * @param id the user ID to update
     * @param email new email
     * @param name new name
     * @return success message
     */
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateUser(
            @RequestParam String filename,
            @PathVariable int id,
            @RequestParam String email,
            @RequestParam String name
    ) {
        try {
            String message = service.updateUser(filename, id, email, name);
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "filename", filename,
                    "userId", id,
                    "updatedEmail", email,
                    "updatedName", name,
                    "operation", "UPDATE",
                    "storage", "MongoDB"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Partially update a user (PATCH)
     * @param filename the CSV filename stored in MongoDB
     * @param id the user ID to update
     * @param email new email (optional)
     * @param name new name (optional)
     * @return success message
     */
    @PatchMapping("/{id}/patch")
    public ResponseEntity<?> patchUser(
            @RequestParam String filename,
            @PathVariable int id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name
    ) {
        try {
            String message = service.patchUser(filename, id, email, name);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", message);
            response.put("filename", filename);
            response.put("userId", id);
            response.put("operation", "PATCH");
            response.put("storage", "MongoDB");
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
     * @param filename the CSV filename stored in MongoDB
     * @param id the user ID to delete
     * @return success message
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteUser(
            @RequestParam String filename,
            @PathVariable int id
    ) {
        try {
            String message = service.deleteUser(filename, id);
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "filename", filename,
                    "deletedUserId", id,
                    "operation", "DELETE",
                    "storage", "MongoDB"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /* ---------- FILE INFO ---------- */
    /**
     * Get information about a stored file in MongoDB
     * @param filename the filename to query
     * @return file details
     */
    @GetMapping("/info/{filename}")
    public ResponseEntity<?> getFileInfo(
            @PathVariable String filename
    ) {
        try {
            Map<String, Object> fileInfo = service.getFileInfo(filename);
            return ResponseEntity.ok(Map.of(
                    "message", "File information retrieved",
                    "fileInfo", fileInfo
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}

