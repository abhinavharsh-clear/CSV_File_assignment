package com.example.demo.service.impl;

import com.example.demo.exception.FileProcessingException;
import com.example.demo.service.FileProcessingService;
import com.example.demo.util.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {

    @Override
    public String process(MultipartFile file) {

        FileValidator.validate(file);

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream())
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(processLine(line)).append("\n");
            }

        } catch (IOException e) {
            throw new FileProcessingException("Error while reading file");
        }

        return output.toString();
    }

    private String processLine(String line) {
        // Business logic
        return line.toUpperCase();
    }
}
