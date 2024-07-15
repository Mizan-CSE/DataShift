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
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class WorkingArea {
    @Autowired
    SeleniumConfig config;
    @Autowired
    XLUtility xlutil;
    int rowCount;
    private ExtentTest childTest;
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
        childTest = test.createNode("Working Area Migration Data Entry");
        String[][] data = request.getCellData();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        for (String[] rowData : data) {
            try {
                if (search(driver, rowData[4]).isEmpty() || search(driver, rowData[4]) == null) {
                    // Union/ward Page
                    WebElement addBTN = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                    addBTN.click();
                    sleep(1500);
                    WebElement wardVillageName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12']")); // ward, village
                    wardVillageName.clear();
                    wardVillageName.sendKeys(rowData[4]);
                    Thread.sleep(1500);
                    selectDivisionToThana(driver, rowData[1], rowData[2], rowData[3]);
//                    WebElement divisionName = driver.findElement(By.id("cbo_division"));
//                    Select div = new Select(divisionName);
//                    div.selectByVisibleText(rowData[1]);
//                    Thread.sleep(2000);
//
//                    WebElement districtName = driver.findElement(By.id("cbo_district"));
//                    Select dist = new Select(districtName);
//                    dist.selectByVisibleText(rowData[2]);
//                    Thread.sleep(2000);
//
//                    WebElement thanaName = driver.findElement(By.id("cbo_thana"));
//                    Select tha = new Select(thanaName);
//                    tha.selectByVisibleText(rowData[3]);
//                    Thread.sleep(2000);

                    WebElement saveWard = driver.findElement(By.xpath("//button[@class='btn btn-success btn-sm']"));
                    saveWard.click();
                    sleep(2000);

                    // Village Page
//                    driver.switchTo().window(tabs.get(1));
//                    addBTN.click();
//                    sleep(1500);
//
//                    wardVillageName.clear();
//                    wardVillageName.sendKeys(rowData[5]);
//
//                    Thread.sleep(1500);
//
//                    selectDivisionToThana(driver, rowData[1], rowData[2], rowData[3]);
//                    selectWard(driver, rowData[4]);
//
////                    div.selectByVisibleText(rowData[1]);
////                    Thread.sleep(2000);
////
////                    dist.selectByVisibleText(rowData[2]);
////                    Thread.sleep(2000);
////
////                    tha.selectByVisibleText(rowData[3]);
////                    Thread.sleep(2000);
//
//                    WebElement unionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_union'][@class='form-control form-control-sm form-control col-md-12']"));
//                    Select union = new Select(unionNameVillageForm);
//                    union.selectByVisibleText(rowData[4]);
//                    Thread.sleep(2000);
//
//                    WebElement submitVillageWorkingArea = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-success btn-sm']"));
//                    submitVillageWorkingArea.click();
//                    sleep(2000);
//
//                    // Working Area page
//                    driver.switchTo().window(tabs.get(2));
//                    addBTN.click();
//                    sleep(1500);
//                    WebElement workingAreaName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12' and @placeholder='name']")); // working Area
//                    workingAreaName.clear();
//                    workingAreaName.sendKeys(rowData[6]);
//                    Thread.sleep(1500);
//                    selectDivisionToThana(driver, rowData[1], rowData[2], rowData[3]);
//                    selectWard(driver, rowData[4]);
//                    selectVillage(driver, rowData[5]);
//
////                    div.selectByVisibleText(rowData[1]);
////                    Thread.sleep(2000);
////
////                    dist.selectByVisibleText(rowData[2]);
////                    Thread.sleep(2000);
////
////                    tha.selectByVisibleText(rowData[3]);
////                    Thread.sleep(2000);
//
//                    union.selectByVisibleText(rowData[4]);
//                    Thread.sleep(2000);
//
//                    WebElement villageNameWorkingAreaForm = driver.findElement(By.xpath("//select[@id='cbo_village'][@class='form-control form-control-sm form-control col-md-12']"));
//                    Select vill = new Select(villageNameWorkingAreaForm);
//                    vill.selectByVisibleText(rowData[5]);
//                    Thread.sleep(2000);
//
//                    submitVillageWorkingArea.click();
//                    sleep(2000);
//                    verifyAndHandleToastMessage(driver, rowData[6]);

                } else {
                    driver.switchTo().window(tabs.get(1));
                    Thread.sleep(1500);
                    if (search(driver, rowData[5]).isEmpty() || search(driver, rowData[5]) == null) {
                        WebElement addBTN = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                        addBTN.click();
                        sleep(1500);
                        WebElement wardVillageName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12']")); // ward, village
                        wardVillageName.clear();
                        wardVillageName.sendKeys(rowData[5]);
                        Thread.sleep(1500);
                        selectDivisionToThana(driver, rowData[1], rowData[2], rowData[3]);
                        selectWard(driver, rowData[4]);
//                        WebElement divisionName = driver.findElement(By.id("cbo_division"));
//                        Select div = new Select(divisionName);
//                        div.selectByVisibleText(rowData[1]);
//                        Thread.sleep(2000);
//
//                        WebElement districtName = driver.findElement(By.id("cbo_district"));
//                        Select dist = new Select(districtName);
//                        dist.selectByVisibleText(rowData[2]);
//                        Thread.sleep(2000);
//
//                        WebElement thanaName = driver.findElement(By.id("cbo_thana"));
//                        Select tha = new Select(thanaName);
//                        tha.selectByVisibleText(rowData[3]);
//                        Thread.sleep(2000);
//
//                        WebElement unionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_union'][@class='form-control form-control-sm form-control col-md-12']"));
//                        Select union = new Select(unionNameVillageForm);
//                        union.selectByVisibleText(rowData[4]);
//                        Thread.sleep(2000);

                        WebElement submitVillageWorkingArea = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-success btn-sm']"));
                        submitVillageWorkingArea.click();
                        sleep(2000);

                        // Working Area page
//                        driver.switchTo().window(tabs.get(2));
//                        addBTN.click();
//                        sleep(1500);
//
//                        WebElement workingAreaName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12' and @placeholder='name']")); // working Area
//                        workingAreaName.clear();
//                        workingAreaName.sendKeys(rowData[6]);
//                        Thread.sleep(1500);
//                        selectDivisionToThana(driver, rowData[1], rowData[2], rowData[3]);
//                        selectWard(driver, rowData[4]);
//                        selectVillage(driver, rowData[5]);
//
////                        div.selectByVisibleText(rowData[1]);
////                        Thread.sleep(2000);
////
////                        dist.selectByVisibleText(rowData[2]);
////                        Thread.sleep(2000);
////
////                        tha.selectByVisibleText(rowData[3]);
////                        Thread.sleep(2000);
////
////                        union.selectByVisibleText(rowData[4]);
////                        Thread.sleep(2000);
////
////                        WebElement villageNameWorkingAreaForm = driver.findElement(By.xpath("//select[@id='cbo_village'][@class='form-control form-control-sm form-control col-md-12']"));
////                        Select vill = new Select(villageNameWorkingAreaForm);
////                        vill.selectByVisibleText(rowData[5]);
////                        Thread.sleep(2000);
//
//                        submitVillageWorkingArea.click();
//                        sleep(2000);
//
//                        verifyAndHandleToastMessage(driver, rowData[6]);
                    } else {
                        driver.switchTo().window(tabs.get(2));
                        Thread.sleep(1500);
                        if (search(driver, rowData[6]).isEmpty() || search(driver, rowData[6]) == null) {
                            WebElement addBTN = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                            addBTN.click();
                            sleep(1500);
                            WebElement workingAreaName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12' and @placeholder='name']")); // working Area
                            workingAreaName.clear();
                            workingAreaName.sendKeys(rowData[6]);
                            Thread.sleep(1500);
                            selectDivisionToThana(driver, rowData[1], rowData[2], rowData[3]);
                            selectWard(driver, rowData[4]);
                            selectVillage(driver, rowData[5]);
//                            WebElement divisionName = driver.findElement(By.id("cbo_division"));
//                            Select div = new Select(divisionName);
//                            div.selectByVisibleText(rowData[1]);
//                            Thread.sleep(2000);
//
//                            WebElement districtName = driver.findElement(By.id("cbo_district"));
//                            Select dist = new Select(districtName);
//                            dist.selectByVisibleText(rowData[2]);
//                            Thread.sleep(2000);
//
//                            WebElement thanaName = driver.findElement(By.id("cbo_thana"));
//                            Select tha = new Select(thanaName);
//                            tha.selectByVisibleText(rowData[3]);
//                            Thread.sleep(2000);
//
//                            WebElement unionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_union'][@class='form-control form-control-sm form-control col-md-12']"));
//                            Select union = new Select(unionNameVillageForm);
//                            union.selectByVisibleText(rowData[4]);
//                            Thread.sleep(2000);
//
//                            WebElement villageNameWorkingAreaForm = driver.findElement(By.xpath("//select[@id='cbo_village'][@class='form-control form-control-sm form-control col-md-12']"));
//                            Select vill = new Select(villageNameWorkingAreaForm);
//                            vill.selectByVisibleText(rowData[5]);
//                            Thread.sleep(2000);

                            WebElement submitVillageWorkingArea = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-success btn-sm']"));
                            submitVillageWorkingArea.click();
                            sleep(2000);

                            verifyAndHandleToastMessage(driver, rowData[6]);
                        }

                    }

                }
                rowCount++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String search(WebDriver driver, String wordVillWorking) throws InterruptedException {
        WebElement searchField = driver.findElement(By.xpath("//input[@placeholder='Search By Name' or @id='txt_name']"));
        searchField.clear();
        searchField.sendKeys(wordVillWorking);
        WebElement searchBTN = driver.findElement(By.id("custom-search-btn"));
        searchBTN.click();
        sleep(1500);
        WebElement newWardVillageWorkingArea = driver.findElement(By.xpath("//table/tbody"));
        return newWardVillageWorkingArea.getText();
    }

    public void selectDivisionToThana(WebDriver driver, String division, String district, String thana) throws InterruptedException {
        WebElement divisionName = driver.findElement(By.id("cbo_division"));
        Select div = new Select(divisionName);
//        List<WebElement> allDivision = div.getOptions();
//        for (WebElement divWise : allDivision) {
//            if (divWise.getText().contains(division)) {
//                System.out.println(divWise.getText());
//                divWise.click();
//            }
//        }
        div.selectByVisibleText(division);
        Thread.sleep(2000);

        WebElement districtName = driver.findElement(By.id("cbo_district"));
        Select dist = new Select(districtName);
//        List<WebElement> allDistrict = dist.getOptions();
//        for (WebElement distWise : allDistrict) {
//            if (distWise.getText().contains(division)) {
//                distWise.click();
//            }
//        }
        dist.selectByVisibleText(district);
        Thread.sleep(2000);

        WebElement thanaName = driver.findElement(By.id("cbo_thana"));
        Select tha = new Select(thanaName);
//        List<WebElement> allThana = tha.getOptions();
//        for (WebElement thaWise : allThana) {
//            if (thaWise.getText().contains(division)) {
//                thaWise.click();
//            }
//        }
        tha.selectByVisibleText(thana);
        Thread.sleep(2000);
    }

    public void selectWard(WebDriver driver, String ward) throws InterruptedException {
        WebElement unionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_union'][@class='form-control form-control-sm form-control col-md-12']"));
        Select union = new Select(unionNameVillageForm);
        union.selectByVisibleText(ward);
        Thread.sleep(2000);
    }

    public void selectVillage(WebDriver driver, String village) throws InterruptedException {
        WebElement villageNameWorkingAreaForm = driver.findElement(By.xpath("//select[@id='cbo_village'][@class='form-control form-control-sm form-control col-md-12']"));
        Select vill = new Select(villageNameWorkingAreaForm);
        vill.selectByVisibleText(village);
        Thread.sleep(2000);
    }

    public void verifyAndHandleToastMessage(WebDriver driver, String workingArea) throws IOException {
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
            xlutil.setCellData("Sheet1", rowCount, 0, workingArea);
            xlutil.setCellData("Sheet1", rowCount, 1, "Working Area is Migrated");
            childTest.log(Status.PASS, workingArea + " is Migrated ");
            Assert.assertTrue(true);
        } else {
            driver.findElement(By.xpath("//button[@class='btn mr-2 btn-danger btn-sm']")).click();
            xlutil.setCellData("Sheet1", rowCount, 0, workingArea);
            xlutil.setCellData("Sheet1", rowCount, 1, "Working Area is not Migrated");
            childTest.log(Status.FAIL, workingArea + " is not Migrated");
        }
    }
}


