package com.example.demo.repository;

import com.example.demo.model.CsvFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB Repository for CsvFile documents
 * Provides CRUD operations and custom queries
 */
@Repository
public interface CsvFileRepository extends MongoRepository<CsvFile, String> {

    /**
     * Find a CSV file by its filename
     * @param filename the filename to search for
     * @return Optional containing the CsvFile if found
     */
    Optional<CsvFile> findByFilename(String filename);

    /**
     * Delete a CSV file by filename
     * @param filename the filename to delete
     */
    void deleteByFilename(String filename);
}

