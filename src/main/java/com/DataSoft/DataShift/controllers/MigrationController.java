package com.DataSoft.DataShift.controllers;

import com.DataSoft.DataShift.models.AutomationRequest;
import com.DataSoft.DataShift.services.LoansMigration;
import com.DataSoft.DataShift.services.MemberMigration;
import com.DataSoft.DataShift.services.SamityMigration;
import com.DataSoft.DataShift.services.SavingsMigration;
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
    private XLUtility excelReaderService;
    private Map<String, String[][]> processFile = new HashMap<>();
    private String lastProcessedFileId = null; // Track the fileId of the last uploaded file
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.download-dir}")
    private String downloadDir;

    @PostMapping(value = "/preprocessDataset")
    public ResponseEntity<String> preprocessDataset(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("testcase") String testcase) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
        }
        try {
            String absoluteDirectory = Paths.get(System.getProperty("user.dir"), uploadDir).toString();
            // Save the uploaded file to a specified location
            String filePath = absoluteDirectory + File.separator + file.getOriginalFilename(); // Absolute path


            file.transferTo(new File(filePath));
            System.out.println("Uploaded file path: " + filePath);
            // Call your Python script to process the uploaded file
            executeMigrationScript(filePath, testcase); // Return the path of the processed file
            return ResponseEntity.ok().body("{\"message\": \"Dataset preprocessing completed.\"}");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to preprocess dataset.");
        }
    }


    @PostMapping("/runAutomation")
    public ResponseEntity<Map<String, String>> processMigration(@RequestBody AutomationRequest request) throws IOException {
        String[][] data = processFile.get(lastProcessedFileId);
        String result = null;
        request.setCellData(data);
        System.out.println("Processed Data:" + Arrays.deepToString(request.getCellData()));
        String migrationName = request.getTestcase();
        if (migrationName.equalsIgnoreCase("Member Migration")) {
            result = memberMigration.memberMigration(request);
        }
        else if (migrationName.equalsIgnoreCase("Loans Migration")) {
            result = loansMigration.loansMigration(request);
        }
        else if (migrationName.equalsIgnoreCase("Savings Migration")) {
            result = savingsMigration.savingsMigration(request);
        }
        else if (migrationName.equalsIgnoreCase("Samity Migration")) {
            result = samityMigration.samityMigration(request);
        }

        String migrationKey = migrationName.toLowerCase().replace(" ", "-");
        String reportUrl = "/datashift/report/" + migrationKey;

        Map<String, String> response = new HashMap<>();
        response.put("status", result);
        response.put("reportUrl", reportUrl);

        return ResponseEntity.ok(response);
//        return "{\"status\": \"" + result + "\"}";
    }

    private String executeMigrationScript(String inputFilePath, String migrationScreen) {
        try {
            // Path to your Python script
            String pythonScriptPath = ".\\PreprocessingModel\\Script\\DataMigration.py";
            String samityDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Samity Data.xlsx";
            String memberDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Member Data.xlsx";
            String loansDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Loans Data.xlsx";
            String savingsDataPath = ".\\dataset\\processed\\cleaned\\Cleaned Savings Data.xlsx";
            String outputFilePath = null;
            // Create ProcessBuilder instance with Python command, script path, and input file path
            ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, inputFilePath, migrationScreen);
            // Start the process
            Process process = pb.start();
            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script exited with code " + exitCode);
            if (migrationScreen.equalsIgnoreCase("Samity Migration")){
                outputFilePath= Paths.get(System.getProperty("user.dir"), samityDataPath).toString();
            }
            else if (migrationScreen.equalsIgnoreCase("Member Migration")){
                outputFilePath= Paths.get(System.getProperty("user.dir"), memberDataPath).toString();
            }
            else if (migrationScreen.equalsIgnoreCase("Loans Migration")){
                outputFilePath= Paths.get(System.getProperty("user.dir"), loansDataPath).toString();
            }
            else if (migrationScreen.equalsIgnoreCase("Savings Migration")){
                outputFilePath= Paths.get(System.getProperty("user.dir"), savingsDataPath).toString();
            }
            // Read output file path generated by the Python script
            try {
//                String outputFilePath = reader.readLine();
                String[][] data = excelReaderService.getData(outputFilePath);
                System.out.println("Processed Data:"+ Arrays.deepToString(data));
                String fileId = UUID.randomUUID().toString();// Generate a unique fileId
                processFile.put(fileId, data);
                lastProcessedFileId = fileId; // Update lastUploadedFileId to the newly uploaded file
                System.out.println("Output file path from Python: " + outputFilePath);
                return outputFilePath;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
