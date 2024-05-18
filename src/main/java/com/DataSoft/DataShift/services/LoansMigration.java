package com.DataSoft.DataShift.services;

import com.DataSoft.DataShift.config.SeleniumConfig;
import com.DataSoft.DataShift.models.AutomationRequest;
import com.DataSoft.DataShift.utils.XLUtility;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class LoansMigration {
    @Autowired
    SeleniumConfig config;
    XLUtility xlutil = new XLUtility();
    String storeMemberCode, memberSamity, status = "";
    int successCount = 0, failureCount = 0, rowCount = 0, threadCount = 1;
    private ExtentReports extent;
    private ExtentTest test;

    public String loansMigration(AutomationRequest request) {
        WebDriver driver = config.getDriver(request.getBrowser()); // Obtain WebDriver instance
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request); // Use WebDriver instance
            return "Loan and savings migration successful";
        } catch (Exception e) {
            return "Loan and savings migration failed: " + e.getMessage();
        } finally {
            driver.quit(); // Close WebDriver instance
        }
    }

    private String startLoansMigration(WebDriver driver, AutomationRequest request) throws IOException {
        ExtentTest childTest = test.createNode("Loans Migration Data Entry");
        String[][] data = request.getCellData();
        for (String[] rowData : data) {
            try {
                WebElement samitySearch = driver.findElement(By.xpath("//select[@name='cbo_samity_id']"));
                Select samity = new Select(samitySearch);
                List<WebElement> allSamity = samity.getOptions();
                for (WebElement samityWise : allSamity) {
                    if (samityWise.getText().contains(rowData[5])) {
                        samityWise.click();
                    }
                }
                driver.findElement(By.xpath("//button[@class='btn ml-4 btn-primary btn-sm']")).click();
                Thread.sleep(2000);

                WebElement memberCode = driver.findElement(By.id("cus_member_id"));

                WebElement userName = driver.findElement(By.id("txt_member_name"));
                userName.clear();
                userName.sendKeys(rowData[0]);
                childTest.log(Status.INFO, "Enter member name: " + rowData[0]);

                WebElement saveMemberInformation = driver.findElement(By.xpath("//*[ contains (text(),'Save') ]"));
                saveMemberInformation.click();
                Thread.sleep(3000);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(4));
                WebElement toastMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='toast-title']")));

                WebElement getSamity = driver.findElement(By.id("cbo_samities_option"));
                memberSamity = getSamity.getAttribute("value");
                if (toastMessage.getText().equalsIgnoreCase("Success")) {

                    String memberInformation = rowData[0] + " " + storeMemberCode;

                    System.out.println("Member Information: " + memberInformation);

                    xlutil.setCellData("Sheet1", rowCount, 0, memberSamity);
                    xlutil.setCellData("Sheet1", rowCount, 1, rowData[8]);
                    xlutil.setCellData("Sheet1", rowCount, 2, memberInformation);
                    xlutil.setCellData("Sheet1", rowCount, 3, "Member is Migrated");
                    childTest.log(Status.PASS, rowData[0]+" is Migrated ");
                    Assert.assertTrue(true);
                    rowCount++;
                } else {
                    status = rowData[0] + " is not Migrated";
                }

            } catch (Exception e) {
                childTest.log(Status.FAIL, "Failed with error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return status;
    }
}
