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
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class WorkingArea {
    @Autowired
    SeleniumConfig config;
    @Autowired
    XLUtility xlutil;
    int rowCount ;
    private ExtentReports extent;
    private ExtentTest test;
    public void initializeRowCount(String countPath) throws IOException {
        rowCount = xlutil.getLastRowNum(countPath) + 1;
    }
    public String workingAreaMigration(AutomationRequest request) throws IOException {
        String filePath = ".\\dataset\\Migrated Information\\Migrated Working Area.xlsx";
        xlutil.setPath(filePath);
        xlutil.setCellData("Sheet1", 0, 0, "Migrated Working Area");
        xlutil.setCellData("Sheet1", 0, 1, "Status");
        initializeRowCount(filePath);

        extent = new ExtentReports();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/report/Working Area Migration.html");
        htmlReporter.config().setTheme(Theme.DARK);
        htmlReporter.config().setDocumentTitle("Working Area Migration Report");
        extent.attachReporter(htmlReporter);

        WebDriver driver = config.getDriver(request.getBrowser());
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request); // Use WebDriver instance
            test = extent.createTest("Auto Working Area Migration");
            test.log(Status.INFO, "Starting Working Area migration...");
            config.addressConfigurationPage(driver);
            startWorkingAreaMigration(driver, request);
            test.log(Status.PASS, "Working area migration successful");
            return "Working area migration successful";
        } catch (Exception e) {
            test.log(Status.FAIL, "Working area migration failed: " + e.getMessage());
            return "Working area migration failed: " + e.getMessage();
        } finally {
            extent.flush(); // Flush the report at the end
            driver.quit(); // Close WebDriver instance
        }
    }

    private String startWorkingAreaMigration(WebDriver driver, AutomationRequest request) throws IOException, InterruptedException {
        ExtentTest childTest = test.createNode("Working Area Migration Data Entry");
        String[][] data = request.getCellData();
        sleep(2000);
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
        for (String[] rowData : data) {
            try {

                WebElement addBTN = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                WebElement wardVillageName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12']")); // ward, village
                WebElement workingAreaName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12' and @placeholder='Name']")); // working Area

                WebElement saveWard = driver.findElement(By.xpath("//button[@class='btn btn-success btn-sm']"));
                WebElement submitVillageWorkingArea = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-success btn-sm']"));

                boolean wardIsNotPresent  = search(driver, rowData[4]);
                boolean villageIsNotPresent  = search(driver, rowData[5]);
                if (wardIsNotPresent){
                    // Union/ward Page
                    addBTN.click();
                    sleep(1500);

                    wardVillageName.clear();
                    wardVillageName.sendKeys(data[4]);
                    selectDivisionToThana(driver,rowData[1], rowData[2], rowData[3]);
                    saveWard.click();
                    sleep(2000);

                    // Village Page
                    driver.switchTo().window(tabs.get(2));
                    addBTN.click();
                    sleep(1500);

                    wardVillageName.clear();
                    wardVillageName.sendKeys(data[5]);
                    selectDivisionToThana(driver,rowData[1], rowData[2], rowData[3]);
                    selectWard(driver, rowData[4]);
                    submitVillageWorkingArea.click();
                    sleep(2000);

                    // Working Area page
                    driver.switchTo().window(tabs.get(3));
                    addBTN.click();
                    sleep(1500);
                    workingAreaName.clear();
                    workingAreaName.sendKeys(rowData[6]);
                    selectDivisionToThana(driver,rowData[1], rowData[2], rowData[3]);
                    selectWard(driver, rowData[4]);
                    selectVillage(driver, rowData[5]);
                    submitVillageWorkingArea.click();
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

                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[6]);
                        xlutil.setCellData("Sheet1", rowCount, 1, "Working Area is Migrated");
                        childTest.log(Status.PASS, rowData[6] + " is Migrated ");
                        Assert.assertTrue(true);
                    }
                    else{
                        driver.findElement(By.xpath("//button[@class='btn mr-2 btn-danger btn-sm']")).click();
                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[6]);
                        xlutil.setCellData("Sheet1", rowCount, 1, "Working Area is Migrated");
                        childTest.log(Status.FAIL, rowData[6] + " is not Migrated");
                    }
                }
                else if(villageIsNotPresent){

                }
                rowCount++;

            }catch (Exception e){
                e.printStackTrace();
            }
        }









        WebElement divisionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_division'][@class='form-control form-control-sm form-control col-md-12']"));
        WebElement districtNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_district'][@class='form-control form-control-sm form-control col-md-12']"));
        WebElement thanaNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_thana'][@class='form-control form-control-sm form-control col-md-12']"));

// Working Area Configuration Locators


        return null;
    }


    public boolean search(WebDriver driver, String wordVillWorking) throws InterruptedException {
        WebElement searchField = driver.findElement(By.xpath("//input[@placeholder='Search By Name' or @id='txt_name']"));
        searchField.sendKeys(wordVillWorking);
        WebElement searchBTN = driver.findElement(By.id("custom-search-btn"));
        searchBTN.click();
        sleep(1500);
        WebElement newWardVillageWorkingArea = driver.findElement(By.xpath("//table/tbody/tr/td[2]"));
        return !newWardVillageWorkingArea.getText().contains(wordVillWorking);
    }

    public void selectDivisionToThana(WebDriver driver, String division, String district, String thana){
        WebElement divisionName = driver.findElement(By.id("cbo_division"));
        Select div = new Select(divisionName);
        div.selectByVisibleText(division);

        WebElement districtName = driver.findElement(By.id("cbo_district"));
        Select dist = new Select(districtName);
        dist.selectByVisibleText(district);

        WebElement thanaName = driver.findElement(By.id("cbo_thana"));
        Select tha = new Select(thanaName);
        tha.selectByVisibleText(thana);
    }
    public void selectWard(WebDriver driver, String ward){
        WebElement unionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_union'][@class='form-control form-control-sm form-control col-md-12']"));
        Select union = new Select(unionNameVillageForm);
        union.selectByVisibleText(ward);
    }
    public void selectVillage(WebDriver driver, String village){
        WebElement villageNameWorkingAreaForm = driver.findElement(By.xpath("//select[@id='cbo_village'][@class='form-control form-control-sm form-control col-md-12']"));
        Select vill = new Select(villageNameWorkingAreaForm);
        vill.selectByVisibleText(village);
    }


}


