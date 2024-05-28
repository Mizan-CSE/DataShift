package com.DataSoft.DataShift.services;

import com.DataSoft.DataShift.config.SeleniumConfig;
import com.DataSoft.DataShift.models.AutomationRequest;
import com.DataSoft.DataShift.utils.XLUtility;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SamityMigration {
    @Autowired
    SeleniumConfig config;
    @Autowired
    XLUtility xlutil;
    private ExtentReports extent;
    private ExtentTest test;

    public String samityMigration(AutomationRequest request) throws IOException {
        String filePath = ".\\dataset\\Migrated Information\\Migrated Samity.xlsx";
        xlutil.setPath(filePath);
        xlutil.setCellData("Sheet1", 0, 0, "Branch Code");
        xlutil.setCellData("Sheet1", 0, 1, "Samity Code");
        xlutil.setCellData("Sheet1", 0, 2, "System Generated Samity Information");
        xlutil.setCellData("Sheet1", 0, 3, "Status");

        extent = new ExtentReports();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/Samity Migration.html");
        htmlReporter.config().setTheme(Theme.DARK);
        htmlReporter.config().setDocumentTitle("Samity Migration Report");
        extent.attachReporter(htmlReporter);

        WebDriver driver = config.getDriver(request.getBrowser());
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request); // Use WebDriver instance
            test = extent.createTest("Auto Samity Migration");
            test.log(Status.INFO, "Starting samity migration...");
            config.samityMigrationPage(driver);
            startSamityMigration(driver, request);
            test.log(Status.PASS, "Samity migration successful");
            return "Samity migration successful";
        } catch (Exception e) {
            test.log(Status.FAIL, "Samity migration failed: " + e.getMessage());
            return "Samity migration failed: " + e.getMessage();
        } finally {
            extent.flush(); // Flush the report at the end
            driver.quit(); // Close WebDriver instance
        }
    }

    private String startSamityMigration(WebDriver driver, AutomationRequest request) throws IOException, InterruptedException {
        ExtentTest childTest = test.createNode("Samity Migration Data Entry");
        String[][] data = request.getCellData();
        for (String[] rowData : data) {
            try {
                WebElement samityAddButton = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                samityAddButton.click();
                Thread.sleep(1500);



                driver.findElement(By.xpath("//button[@class='btn ml-4 btn-primary btn-sm']")).click();
                Thread.sleep(2000);
                return null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
