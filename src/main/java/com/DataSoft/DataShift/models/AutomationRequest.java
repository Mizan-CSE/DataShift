package com.DataSoft.DataShift.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
@Component
public class AutomationRequest {
    private MultipartFile file;
    private String testcase;
    private String browser;
    private String mfi;
    private String username;
    private String password;
    private String[][] cellData;
    private String cleanedFilePath;
    private List<String> processedFilePath;
}
