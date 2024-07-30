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

import java.io.File;
import java.io.IOException;
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
            Map<String, String> filePaths = executeMigrationScript(filePath, testcase);
            String outputFilePath = filePaths.get("outputFilePath");
            String ignoredFilePath = filePaths.get("ignoredFilePath");
            System.out.println("Output file path from Python: " + outputFilePath);
            System.out.println("Ignored file path from Python: " + ignoredFilePath);

            automationRequest.setProcessedFilePath(outputFilePath);
            int totalCleanedRows = excelReaderService.getLastRowNum(outputFilePath);
            int totalIgnoredRows = excelReaderService.getLastRowNum(ignoredFilePath);
            int totalRowsInUploadedExcel = totalCleanedRows + totalIgnoredRows;

            Map<String, String> response = new HashMap<>();
            response.put("message", "Dataset preprocessing completed.");
            response.put("cleanedFilePath", URLEncoder.encode(outputFilePath, StandardCharsets.UTF_8));
            response.put("ignoredFilePath", URLEncoder.encode(ignoredFilePath, StandardCharsets.UTF_8));
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
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error(500)");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Map<String, String> executeMigrationScript(String inputFilePath, String migrationScreen) {
        try {
            // Path of Python script
            String pythonScriptPath = ".\\PreprocessingModel\\Script\\DataMigration.py";

            String workingAreaCleanedDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Working Area Data.xlsx";
            String workingAreaIgnoredDataPath = ".\\dataset\\processed\\Ignored\\Ignore Working Area Data.xlsx";

            String employeeCleanedDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Employee Data.xlsx";
            String employeeIgnoredDataPath = ".\\dataset\\processed\\Ignored\\Ignore Employee Data.xlsx";

            String samityCleanedDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Samity Data.xlsx";
            String samityIgnoredDataPath = ".\\dataset\\processed\\Ignored\\Ignore Samity Data.xlsx";

            String memberCleanedDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Member Data.xlsx";
            String memberIgnoredDataPath = ".\\dataset\\processed\\Ignored\\Ignore Member Data.xlsx";

            String loansCleanedDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Loans Data.xlsx";
            String loansIgnoredDataPath = ".\\dataset\\processed\\Ignored\\Ignore Loans Data.xlsx";

            String savingsCleanedDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Savings Data.xlsx";
            String savingsIgnoredDataPath = ".\\dataset\\processed\\Ignored\\Ignore Savings Data.xlsx";

            String outputFilePath = null;
            String ignoredFilePath = null;
            // Create ProcessBuilder instance with Python command, script path, and input file path
            ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, inputFilePath, migrationScreen);
            // Start the process
            Process process = pb.start();
            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script exited with code " + exitCode);
            if (migrationScreen.equalsIgnoreCase("Working Area")) {
                outputFilePath = Paths.get(System.getProperty("user.dir"), workingAreaCleanedDataPath).toString();
                ignoredFilePath = Paths.get(System.getProperty("user.dir"), workingAreaIgnoredDataPath).toString();
            } else if (migrationScreen.equalsIgnoreCase("Employee Migration")) {
                outputFilePath = Paths.get(System.getProperty("user.dir"), employeeCleanedDataPath).toString();
                ignoredFilePath = Paths.get(System.getProperty("user.dir"), employeeIgnoredDataPath).toString();
            } else if (migrationScreen.equalsIgnoreCase("Samity Migration")) {
                outputFilePath = Paths.get(System.getProperty("user.dir"), samityCleanedDataPath).toString();
                ignoredFilePath = Paths.get(System.getProperty("user.dir"), samityIgnoredDataPath).toString();
            } else if (migrationScreen.equalsIgnoreCase("Member Migration")) {
                outputFilePath = Paths.get(System.getProperty("user.dir"), memberCleanedDataPath).toString();
                ignoredFilePath = Paths.get(System.getProperty("user.dir"), memberIgnoredDataPath).toString();
            } else if (migrationScreen.equalsIgnoreCase("Loans Migration")) {
                outputFilePath = Paths.get(System.getProperty("user.dir"), loansCleanedDataPath).toString();
                ignoredFilePath = Paths.get(System.getProperty("user.dir"), loansIgnoredDataPath).toString();
            } else if (migrationScreen.equalsIgnoreCase("Savings Migration")) {
                outputFilePath = Paths.get(System.getProperty("user.dir"), savingsCleanedDataPath).toString();
                ignoredFilePath = Paths.get(System.getProperty("user.dir"), savingsIgnoredDataPath).toString();
            }
            // Read output file path generated by the Python script
            try {
//                String outputFilePath = reader.readLine();
                String[][] data = excelReaderService.getData(outputFilePath);
                System.out.println("Processed Data:" + Arrays.deepToString(data));
                String fileId = UUID.randomUUID().toString();// Generate a unique fileId
                processFile.put(fileId, data);
                lastProcessedFileId = fileId; // Update lastUploadedFileId to the newly uploaded file

                Map<String, String> filePaths = new HashMap<>();
                filePaths.put("outputFilePath", outputFilePath);
                filePaths.put("ignoredFilePath", ignoredFilePath);
                return filePaths;
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyMap();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

}
