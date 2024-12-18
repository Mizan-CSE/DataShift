package com.DataSoft.DataShift.services;

import com.DataSoft.DataShift.config.SeleniumConfig;
import com.DataSoft.DataShift.models.AutomationRequest;
import com.DataSoft.DataShift.utils.XLUtility;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

@Service
public class SamityMigration {
    @Autowired
    SeleniumConfig config;
    @Autowired
    XLUtility xlutil;
    String systemGeneratedSamityCode;
    int rowCount ;
    private ExtentReports extent;
    private ExtentTest test;
    public void initializeRowCount(String countPath) throws IOException {
        rowCount = xlutil.getLastRowNum(countPath) + 1;
    }
    public String samityMigration(AutomationRequest request) throws IOException {
        String filePath = ".\\dataset\\Migrated Information\\Migrated Samity.xlsx";
        xlutil.setPath(filePath);
        xlutil.setCellData("Sheet1", 0, 0, "Branch Code");
        xlutil.setCellData("Sheet1", 0, 1, "Samity Code");
        xlutil.setCellData("Sheet1", 0, 2, "System Generated Samity Information");
        xlutil.setCellData("Sheet1", 0, 3, "Status");
        initializeRowCount(filePath);

        extent = new ExtentReports();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/report/Samity Migration.html");
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
                sleep(1500);

                WebElement samityName = driver.findElement(By.xpath("//input[@name='txt_name']"));
                samityName.clear();
                samityName.sendKeys(rowData[2].strip());
                childTest.log(Status.INFO, "Enter samity name: " + rowData[2]);

                //  Division select dropdown
//                WebElement samityDivision = driver.findElement(By.xpath("//select[@name='cbo_division_id']"));
//                Select division = new Select(samityDivision);
                //  division.selectByVisibleText("SUCHONA [1]");
                //  Product select dropdown
//                WebElement samityProduct = driver.findElement(By.xpath("//select[@name='cbo_product_id']"));
//                Select product = new Select(samityProduct);
                //  division.selectByVisibleText("SUCHONA [1]");

//                List<WebElement> alldiv = division.getOptions();
//                for (WebElement div : alldiv) {
//                    if (div.getText().contains(rowData[3].strip())) {
//                        div.click();
//                        childTest.log(Status.INFO, "Enter division: " + rowData[3]);
//                    }
//                }

                WebElement samityCode = driver.findElement(By.xpath("//input[@name='txt_code']"));
                systemGeneratedSamityCode = samityCode.getAttribute("value");


                // Working Area

//                WebElement workingArea = driver.findElement(By.xpath("//input[@placeholder='Type min 3 char of name or code...']"));
//                workingArea.clear();
//                workingArea.sendKeys(rowData[4].strip().substring(0, 3));
//                sleep(1000);
//                List<WebElement> list = driver.findElements(By.xpath("//*[@class='list-group shadow vbt-autcomplete-list']"));
//
//                try {
//                    list.get(0).click();
//                    sleep(1000);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }

                //-------------------------------------

                // Working Area of samity create

                Pattern pattern = Pattern.compile("^[^ ,]*");
                Matcher matcher = pattern.matcher(rowData[4]);

                String extractedText = "";
                if (matcher.find()) {
                    extractedText = matcher.group(0);
                }

                WebElement workingArea = driver.findElement(By.xpath("//input[@placeholder='Type min 3 char of name or code...']"));
                workingArea.clear();
                workingArea.sendKeys(extractedText);
                sleep(1000);

                List<WebElement> list = driver.findElements(By.xpath("//*[@class='list-group shadow vbt-autcomplete-list']"));

                try {
                    list.get(0).click();
                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                childTest.log(Status.INFO, "Enter working area: " + rowData[4]);

//-------------------------------------------------------------
                WebElement fieldOfficer = driver.findElement(By.xpath("//select[@name='cbo_field_officer_id']"));
                Select officer = new Select(fieldOfficer);

                List<WebElement> allFO = officer.getOptions();
                for (WebElement FO : allFO) {
                    if (FO.getText().contains(rowData[5].strip())) {
                        FO.click();
                        childTest.log(Status.INFO, "Enter field officer: " + rowData[5]);
                    }
                }

                WebElement samityDay = driver.findElement(By.xpath("//select[@name='cbo_samity_day']"));
                Select day = new Select(samityDay);
                if (rowData[6].strip().equalsIgnoreCase("Sat") || rowData[6].strip().equalsIgnoreCase("Saturday")) {
                    day.selectByValue("Sat");
                } else if (rowData[6].strip().equalsIgnoreCase("Sun") || rowData[6].strip().equalsIgnoreCase("Sunday")) {
                    day.selectByValue("Sun");
                } else if (rowData[6].strip().equalsIgnoreCase("Mon") || rowData[6].strip().equalsIgnoreCase("Monday")) {
                    day.selectByValue("Mon");
                } else if (rowData[6].strip().equalsIgnoreCase("Tue") || rowData[6].strip().equalsIgnoreCase("Tuesday")) {
                    day.selectByValue("Tue");
                } else if (rowData[6].strip().equalsIgnoreCase("Wed") || rowData[6].strip().equalsIgnoreCase("Wednesday")) {
                    day.selectByValue("Wed");
                } else if (rowData[6].strip().equalsIgnoreCase("Thu") || rowData[6].strip().equalsIgnoreCase("Thursday")) {
                    day.selectByValue("Thu");
                }
                childTest.log(Status.INFO, "Enter field officer: " + rowData[6]);

                WebElement samityType = driver.findElement(By.xpath("//select[@name='cbo_samity_type']"));
                Select type = new Select(samityType);
                if (rowData[7].strip().equalsIgnoreCase("Male") || rowData[7].strip().equalsIgnoreCase("M")) {
                    type.selectByValue("M");
                } else if (rowData[7].strip().equalsIgnoreCase("Female") || rowData[7].strip().equalsIgnoreCase("F")) {
                    type.selectByValue("F");
                } else if (rowData[7].strip().equalsIgnoreCase("Both") || rowData[7].strip().equalsIgnoreCase("B")) {
                    type.selectByValue("B");
                }
                childTest.log(Status.INFO, "Enter samity type: " + rowData[7]);

                WebElement samityOpeningDate = driver.findElement(By.xpath("//input[@data-vv-as='Opening Date ']"));
                samityOpeningDate.clear();
                samityOpeningDate.sendKeys(rowData[8].strip());
                childTest.log(Status.INFO, "Enter samity opening date: " + rowData[8]);

                WebElement maxMemberOfSamity = driver.findElement(By.xpath("//input[@name='txt_max_member']"));
                maxMemberOfSamity.click();
                maxMemberOfSamity.clear();
                if(!rowData[9].isEmpty()){
                    maxMemberOfSamity.sendKeys(rowData[9].strip());
                    childTest.log(Status.INFO, "Maximum number of the samity: " + rowData[9]);
                }else{
                    maxMemberOfSamity.sendKeys("200");
                    childTest.log(Status.INFO, "Maximum number of the samity: 200");
                }


                WebElement saveSamity = driver.findElement(By.xpath("//button[@type='submit']"));
                saveSamity.click();
                sleep(2000);

                boolean isToastMessageDisplayed = false;
                WebElement toastMessage = null;
                try {
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(4));
                    toastMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='toast-title']")));
                    isToastMessageDisplayed = true;
                } catch (TimeoutException e) {
                    isToastMessageDisplayed = false;
                }

                if (isToastMessageDisplayed && toastMessage.getText().equalsIgnoreCase("Success")) {
                    String samityInformation = systemGeneratedSamityCode + " - "+ rowData[2];
                    xlutil.setCellData("Sheet1", rowCount, 0, rowData[0]);
                    xlutil.setCellData("Sheet1", rowCount, 1, rowData[1].strip());
                    xlutil.setCellData("Sheet1", rowCount, 2, samityInformation);
                    xlutil.setCellData("Sheet1", rowCount, 3, "Samity is Migrated");
                    childTest.log(Status.PASS, rowData[1] + " is Migrated ");
                    Assert.assertTrue(true);
                } else {
                    driver.findElement(By.xpath("//button[@class='btn btn-danger btn-sm']")).click();
                    xlutil.setCellData("Sheet1", rowCount, 0, rowData[0]);
                    xlutil.setCellData("Sheet1", rowCount, 1, rowData[1].strip());
                    xlutil.setCellData("Sheet1", rowCount, 2, "");
                    xlutil.setCellData("Sheet1", rowCount, 3, "Samity is not Migrated");
                    childTest.log(Status.FAIL, rowData[1] + " is not Migrated");
                }
                rowCount++;

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
