package com.example.demo.controller;
import com.example.demo.service.FileProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.plaf.multi.MultiPanelUI;


@RestController
@RequestMapping("/api")
public class UploadController {

    private final FileProcessingService fileProcessingService;

    public UploadController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }


    @PostMapping("/fetchFile")
    public ResponseEntity<String> processFile(
            @RequestParam("file") MultipartFile file
    ) {
        String result = fileProcessingService.process(file);
        return ResponseEntity.ok(result);
    }

}
