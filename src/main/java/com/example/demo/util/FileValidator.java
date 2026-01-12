package com.example.demo.util;

import com.example.demo.exception.FileProcessingException;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    private FileValidator() {}

    public static void validate(MultipartFile file) {

        if (file.isEmpty()) {
            throw new FileProcessingException("File is empty");
        }

        if (!"text/plain".equals(file.getContentType())) {
            throw new FileProcessingException("Only text files are allowed");
        }
    }
}
