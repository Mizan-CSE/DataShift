package com.DataSoft.DataShift.controllers;

import com.DataSoft.DataShift.models.AutomationRequest;
import com.DataSoft.DataShift.services.*;
import com.DataSoft.DataShift.utils.XLUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//@RestController
////@RequestMapping("/datashift")
//public class AutomationController {
//    @Autowired
//    private WorkingArea workingArea;
//    @Autowired
//    private Employee employee;
//    @Autowired
//    private Samity samity;
//    @Autowired
//    private MemberMigration memberMigration;
//    @Autowired
//    private Loan_Savings loan_savingsMigration;
//    @Autowired
//    private XLUtility excelReaderService;
//    private Map<String, String[][]> uploadedFiles = new HashMap<>();
//    private String lastUploadedFileId = null; // Track the fileId of the last uploaded file



//    @PostMapping("/preprocessDataset")
//    public ResponseEntity<String> preprocessDataset(@RequestParam("file") MultipartFile file, AutomationRequest request) {
//        if (file.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
//        }
//
//        try {
//            String[][] data = excelReaderService.getData(file.getInputStream());
//
//            String fileId = UUID.randomUUID().toString();// Generate a unique fileId
//            uploadedFiles.put(fileId, data);
//            lastUploadedFileId = fileId; // Update lastUploadedFileId to the newly uploaded file
//
//            return ResponseEntity.ok().body("{\"message\": \"Dataset preprocessing completed.\"}");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to preprocess dataset.");
//        }
//    }

//    @PostMapping("/runAutomation")
//    public String processMigration(@RequestBody AutomationRequest request) {
//        String[][] data = uploadedFiles.get(lastUploadedFileId);
//        String result=null;
//        request.setCellData(data);
//        String migrationName = request.getTestcase();
//        switch (migrationName) {
//            case "Working Area" -> result = workingArea.areaMigration(request);
//            case "Employee" -> result = employee.employeeMigration(request);
//            case "Samity" -> result = samity.samityMigration(request);
//            case "Member Migration" -> result = memberMigration.memberMigration(request);
//            case "Loan & Savings Migration" -> result = loan_savingsMigration.loansSavingsMigration(request);
//        }
//        return "{\"status\": \"" + result + "\"}";
//    }
//}
