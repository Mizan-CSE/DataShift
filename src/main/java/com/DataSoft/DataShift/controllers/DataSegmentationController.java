package com.DataSoft.DataShift.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class DataSegmentationController {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.segment-dir}")
    private String downloadDir;

    @PostMapping(value = "/upload")
    public ResponseEntity<List<String>> dataSegmentation(@RequestParam("file") MultipartFile file) {
        try {
            String absoluteDirectory = Paths.get(System.getProperty("user.dir"), uploadDir).toString();
            String filePath = absoluteDirectory + File.separator + file.getOriginalFilename(); // Absolute path
            file.transferTo(new File(filePath));

            List<File> segmentedFiles = processFile(new File(filePath));

            List<String> fileUrls = segmentedFiles.stream()
                    .map(segmentedFile -> MvcUriComponentsBuilder.fromMethodName(
                            DataSegmentationController.class, "serveFile", segmentedFile.getName()).build().toString())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(fileUrls);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public List<File> processFile(File file) throws IOException, InterruptedException {
        String outputDir = downloadDir;
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python", ".\\PreprocessingModel\\Script\\Data Segmentation.py", file.getAbsolutePath(), outputDir);

        Process process = processBuilder.start();
        process.waitFor();

        File outputDirectory = new File(outputDir);
        List<File> segmentedFiles = new ArrayList<>();
        for (File outputFile : outputDirectory.listFiles()) {
            if (outputFile.isFile() && outputFile.getName().startsWith(file.getName().split("\\.")[0])) {
                segmentedFiles.add(outputFile);
            }
        }

        return segmentedFiles;
    }

    @GetMapping("/download1")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());

            File file = new File(downloadDir + File.separator + decodedFileName);
            FileSystemResource resource = new FileSystemResource(file);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}