package com.DataSoft.DataShift.controllers;

import com.DataSoft.DataShift.models.AutomationRequest;
import com.DataSoft.DataShift.services.*;
import com.DataSoft.DataShift.utils.XLUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

@RestController
public class MigrationController {
    @Autowired
    private MemberMigration memberMigration;
    @Autowired
    private LoansMigration loansMigration;
    @Autowired
    private SavingsMigration savingsMigration;
    @Autowired
    private SamityMigration samityMigration;
    @Autowired
    private Employee employeeMigration;
    @Autowired
    private WorkingArea workingArea;
    @Autowired
    private XLUtility excelReaderService;
    private Map<String, String[][]> processFile = new HashMap<>();
    private String lastProcessedFileId = null; // Track the fileId of the last uploaded file
    @Autowired
    private AutomationRequest automationRequest;

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.download-dir}")
    private String downloadDir;

    @PostMapping(value = "/preprocessDataset")
    public ResponseEntity<Map<String, String>> preprocessDataset(@RequestParam("file") MultipartFile file, @RequestParam("testcase") String testcase) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "No file uploaded."));
        }
        File fileDelete = null;
        try {
            String absoluteDirectory = Paths.get(System.getProperty("user.dir"), uploadDir).toString();
            // Save the uploaded file to a specified location
            String filePath = absoluteDirectory + File.separator + file.getOriginalFilename(); // Absolute path

            file.transferTo(new File(filePath));
            fileDelete = new File(uploadDir + File.separator + file.getOriginalFilename());
            System.out.println("Uploaded file path: " + filePath);

            // Call your Python script to process the uploaded file
            List<String> filePaths = executeMigrationScript(filePath, testcase);
            String cleanFilePath = filePaths.get(0);
            String ignoreFilePath = filePaths.get(1);

            automationRequest.setCleanedFilePath(cleanFilePath);
            automationRequest.setProcessedFilePath(filePaths);

            int totalCleanedRows = excelReaderService.getLastRowNum(cleanFilePath);
            int totalIgnoredRows = excelReaderService.getLastRowNum(ignoreFilePath);
            int totalRowsInUploadedExcel = totalCleanedRows + totalIgnoredRows;

            Map<String, String> response = new HashMap<>();
            response.put("message", "Dataset preprocessing completed.");
            response.put("cleanedFilePath", URLEncoder.encode(cleanFilePath, StandardCharsets.UTF_8));
            response.put("ignoredFilePath", URLEncoder.encode(ignoreFilePath, StandardCharsets.UTF_8));
            response.put("totalUploadedRows", String.valueOf(totalRowsInUploadedExcel));
            response.put("totalCleanedRows", String.valueOf(totalCleanedRows));
            response.put("totalIgnoredRows", String.valueOf(totalIgnoredRows));
            boolean deleted = fileDelete.delete();
            if (!deleted) {
                System.err.println("Failed to delete the file: " + fileDelete.getAbsolutePath());
            }
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            if (fileDelete != null && fileDelete.exists()) {
                boolean deleted = fileDelete.delete();
                if (!deleted) {
                    System.err.println("Failed to delete the file: " + file.getOriginalFilename());
                }
            }
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to preprocess dataset."));
        }
    }

    @PostMapping("/runAutomation")
    public ResponseEntity<Map<String, String>> processMigration(@RequestBody AutomationRequest request) throws IOException, InterruptedException {
        try {
            String[][] data = processFile.get(lastProcessedFileId);
            request.setCellData(data);
            System.out.println("Processed Data:" + Arrays.deepToString(request.getCellData()));

            String migrationName = request.getTestcase();
            String result;

            switch (migrationName.toLowerCase()) {
                case "working area":
                    result = workingArea.workingAreaMigration(request);
                    break;
                case "employee migration":
                    result = employeeMigration.employeeMigration(request);
                    break;
                case "samity migration":
                    result = samityMigration.samityMigration(request);
                    break;
                case "member migration":
                    result = memberMigration.memberMigration(request);
                    break;
                case "loans migration":
                    result = loansMigration.loansMigration(request);
                    break;
                case "savings migration":
                    result = savingsMigration.savingsMigration(request);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Invalid migration type"));
            }
            String migrationKey = migrationName.toLowerCase().replace(" ", "-");
            String reportUrl = "/datashift/report/" + migrationKey;
            Map<String, String> response = new HashMap<>();
            response.put("status", result);
            response.put("reportUrl", reportUrl);

            List<String> processFile = automationRequest.getProcessedFilePath();
            for (String path : processFile) {
                System.out.println("processPath: "+path);
                System.out.println();
                String processFileAbsolutePath = Paths.get(System.getProperty("user.dir"), path).toString();
                File fileToDelete = new File(processFileAbsolutePath);
                if (fileToDelete.exists()) {
                    boolean deletedAdditional = fileToDelete.delete();
                    if (!deletedAdditional) {
                        System.err.println("Failed to delete the file: " + fileToDelete.getAbsolutePath());
                    }
                }
            }

//            List<String> test = request.getProcessedFilePath();
            // Delete additional files if needed
//            for (String path : test) {
//                System.out.println("Path: "+path);
//                System.out.println();
//                File fileToDelete = new File(path);
//                if (fileToDelete.exists()) {
//                    boolean deletedAdditional = fileToDelete.delete();
//                    if (!deletedAdditional) {
//                        System.err.println("Failed to delete the file: " + fileToDelete.getAbsolutePath());
//                    }
//                }
//            }


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error(500)");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private List<String> executeMigrationScript(String inputFilePath, String migrationScreen) {
        String pythonScriptPath = ".\\PreprocessingModel\\Script\\DataMigration.py";
        List<String> resultList = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("python",pythonScriptPath, inputFilePath, migrationScreen);
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
            // Read output file path generated by the Python script
            try {
                String cleanFilePath = Paths.get(System.getProperty("user.dir"), resultList.get(0)).toString();
                String ignoreFilePath = Paths.get(System.getProperty("user.dir"), resultList.get(1)).toString();
                String[][] data = excelReaderService.getData(cleanFilePath);
                System.out.println("Processed Data:" + Arrays.deepToString(data));
                String fileId = UUID.randomUUID().toString();// Generate a unique fileId
                processFile.put(fileId, data);
                lastProcessedFileId = fileId; // Update lastUploadedFileId to the newly uploaded file
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
