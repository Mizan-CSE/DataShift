package com.DataSoft.DataShift.controllers;

import com.DataSoft.DataShift.models.AutomationRequest;
import com.DataSoft.DataShift.utils.XLUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class ExcelDataController {

    @Autowired
    private AutomationRequest automationRequest;
    @Autowired
    private XLUtility excelService;

    @GetMapping("/process-data")
    public List<Map<String, String>> processData() {
        String excelFilePath = automationRequest.getProcessedFilePath();
        if (excelFilePath == null || excelFilePath.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is not set.");
        }
        try {
            System.out.println(excelService.readExcelData(excelFilePath));
            return excelService.readExcelData(excelFilePath);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing data", e);
        }
    }
}