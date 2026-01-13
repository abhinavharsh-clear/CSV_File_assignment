package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB Document to store CSV file data
 * Stores the entire CSV content along with metadata
 */
@Document(collection = "csv_files")
public class CsvFile {

    @Id
    private String id;  // MongoDB ObjectId

    private String filename;  // Original filename (e.g., "users.csv")

    private List<User> users;  // List of users parsed from CSV

    private String csvContent;  // Original CSV content as string

    private LocalDateTime uploadedAt;  // When file was uploaded

    private LocalDateTime lastModified;  // When file was last updated

    // Constructors
    public CsvFile() {
    }

    public CsvFile(String filename, List<User> users, String csvContent) {
        this.filename = filename;
        this.users = users;
        this.csvContent = csvContent;
        this.uploadedAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        this.lastModified = LocalDateTime.now();
    }

    public String getCsvContent() {
        return csvContent;
    }

    public void setCsvContent(String csvContent) {
        this.csvContent = csvContent;
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "CsvFile{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", usersCount=" + (users != null ? users.size() : 0) +
                ", uploadedAt=" + uploadedAt +
                ", lastModified=" + lastModified +
                '}';
    }
}

