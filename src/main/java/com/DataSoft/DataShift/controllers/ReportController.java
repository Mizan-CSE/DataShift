package com.DataSoft.DataShift.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Controller

public class ReportController {
    private static final Map<String, String> migrationMap = new HashMap<>();
    static {
        migrationMap.put("employee-migration", "Employee Migration");
        migrationMap.put("member-migration", "Member Migration");
        migrationMap.put("samity-migration", "Samity Migration");
        migrationMap.put("loans-migration", "Loans Migration");
        migrationMap.put("savings-migration", "Savings Migration");
    }

    @GetMapping(value = "/report/{migrationKey}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getReport(@PathVariable String migrationKey){
        String migrationName = migrationMap.get(migrationKey.toLowerCase());
        if (migrationName == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String targetDirectoryPath = "target/report/";
        String fileName = migrationName + ".html";
        File file = Paths.get(targetDirectoryPath, fileName).toFile();

        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + fileName);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}