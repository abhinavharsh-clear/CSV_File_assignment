package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileProcessingService {
    String process(MultipartFile file);
}
