package com.DataSoft.DataShift.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DataSegmentationController {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.segment-dir}")
    private String downloadDir;

    @PostMapping(value = "/upload/file")
    public ResponseEntity<List<String>> dataSegmentation(@RequestParam("file") MultipartFile file) {
        File fileDelete = null;
        try {
            String absoluteDirectory = Paths.get(System.getProperty("user.dir"), uploadDir).toString();
            String filePath = absoluteDirectory + File.separator + file.getOriginalFilename(); // Absolute path
            file.transferTo(new File(filePath));
            fileDelete = new File(uploadDir + File.separator + file.getOriginalFilename());

            List<String> segmentedFiles = processFile(filePath);
            for (String line : segmentedFiles) {
                System.out.println("Path from python: " + line);
                System.out.println();
            }
            boolean deleted = fileDelete.delete();
            if (!deleted) {
                System.err.println("Failed to delete the file: " + fileDelete.getAbsolutePath());
            }
            return ResponseEntity.ok(segmentedFiles);
        } catch (IOException e) {
            if (fileDelete != null && fileDelete.exists()) {
                boolean deleted = fileDelete.delete();
                if (!deleted) {
                    System.err.println("Failed to delete the file: " + file.getOriginalFilename());
                }
            }
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    public List<String> processFile(String inputFilePath) {
        String segmentationScript = ".\\PreprocessingModel\\Script\\DataSegmentation.py";
        List<String> resultList = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", segmentationScript, inputFilePath);
            pb.redirectErrorStream(true); // Merge stderr with stdout for easier debugging
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                resultList.add(line);
            }
            reader.close();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script exited with code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    @GetMapping("/download/segment/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
        File file = null;
        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());

            file = new File(downloadDir + File.separator + decodedFileName);
            FileSystemResource resource = new FileSystemResource(file);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

            ResponseEntity<Resource> response = ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
            return response;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}