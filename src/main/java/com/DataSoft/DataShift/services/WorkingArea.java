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
import java.util.HashSet;
import java.util.Set;

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
            startUnionMigration(driver, request);
            startVillageMigration(driver, request);
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

    // Union Path findout work
    private String startUnionMigration(WebDriver driver, AutomationRequest request) throws IOException, InterruptedException {
        childTest = test.createNode("Union Migration Data Entry");
        String[][] data = request.getCellData();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        Set<String> processedUnions = new HashSet<>();

        for (String[] rowData : data) {
            String union = rowData[4];
            if (processedUnions.contains(union)) {
                continue;
            }

            try {
                if (search(driver, union, rowData[3])) {
                    // Union/ward Page
                    WebElement addBTN = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                    addBTN.click();
                    sleep(1500);
                    WebElement wardVillageName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12']")); // ward, village
                    wardVillageName.clear();
                    wardVillageName.sendKeys(union);
                    childTest.log(Status.INFO, "Enter Union/ward name: " + union);
                    Thread.sleep(1500);

                    WebElement divisionName = driver.findElement(By.xpath("//select[@id='cbo_division']"));
                    Select div = new Select(divisionName);
                    div.selectByVisibleText(rowData[1]);
                    childTest.log(Status.INFO, "Select division name: " + rowData[1]);
                    Thread.sleep(2000);

                    WebElement districtName = driver.findElement(By.xpath("//select[@id='cbo_district']"));
                    Select dist = new Select(districtName);
                    dist.selectByVisibleText(rowData[2]);
                    childTest.log(Status.INFO, "Select district name: " + rowData[2]);
                    Thread.sleep(2000);

                    WebElement thanaName = driver.findElement(By.xpath("//select[@id='cbo_thana']"));
                    Select tha = new Select(thanaName);
                    tha.selectByVisibleText(rowData[3]);
                    childTest.log(Status.INFO, "Select thana name: " + rowData[3]);
                    Thread.sleep(2000);

                    WebElement saveWard = driver.findElement(By.xpath("//button[@class='btn btn-success btn-sm']"));
                    saveWard.click();
                    sleep(2000);
                    verifyAndHandleUnionMessage(driver, union);
                }
                // Add the processed union to the set
                processedUnions.add(union);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    // This is village migration Path find out code-----------------------------------------------
    private String startVillageMigration(WebDriver driver, AutomationRequest request) throws IOException, InterruptedException {
        childTest = test.createNode("Village Migration Data Entry");
        String[][] data = request.getCellData();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
        Set<String> processedVillage = new HashSet<>();

        for (String[] rowData : data) {
            String village = rowData[5];
            if (processedVillage.contains(village)) {
                continue;
            }
            try {
                if (search(driver, village, rowData[4])) {
                    WebElement addBTN = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                    addBTN.click();
                    sleep(1500);

                    WebElement wardVillageName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12']")); // ward, village
                    wardVillageName.clear();
                    wardVillageName.sendKeys(village);
                    childTest.log(Status.INFO, "Enter village name: " + village);

                    WebElement divisionName = driver.findElement(By.xpath("//select[@id='cbo_division' and @class='form-control form-control-sm form-control col-md-12']"));
                    Select div = new Select(divisionName);
                    div.selectByVisibleText(rowData[1]);
                    childTest.log(Status.INFO, "Select division name: " + rowData[1]);
                    Thread.sleep(2000);

                    WebElement districtName = driver.findElement(By.xpath("//select[@id='cbo_district' and @class='form-control form-control-sm form-control col-md-12']"));
                    Select dist = new Select(districtName);
                    dist.selectByVisibleText(rowData[2]);
                    childTest.log(Status.INFO, "Select district name: " + rowData[2]);
                    Thread.sleep(2000);

                    WebElement thanaName = driver.findElement(By.xpath("//select[@id='cbo_thana' and @class='form-control form-control-sm form-control col-md-12']"));
                    Select tha = new Select(thanaName);
                    tha.selectByVisibleText(rowData[3]);
                    childTest.log(Status.INFO, "Select thana name: " + rowData[3]);
                    Thread.sleep(2000);

                    WebElement unionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_union'][@class='form-control form-control-sm form-control col-md-12']"));
                    Select union = new Select(unionNameVillageForm);
                    union.selectByVisibleText(rowData[4]);
                    childTest.log(Status.INFO, "Select union name: " + rowData[4]);
                    Thread.sleep(2000);

                    WebElement submitVillageWorkingArea = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-success btn-sm']"));
                    submitVillageWorkingArea.click();
                    sleep(2000);
                    verifyAndHandleVillageMessage(driver, village);
//                    driver.switchTo().window(tabs.get(2));
                }
                processedVillage.add(village);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    // Here is Working area Path find out work--------------------------------------------------------

    private String startWorkingAreaMigration(WebDriver driver, AutomationRequest request) throws IOException, InterruptedException {
        childTest = test.createNode("Working Area Migration Data Entry");
        String[][] data = request.getCellData();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(2));
        Set<String> processedWorkingArea = new HashSet<>();

        for (String[] rowData : data) {
            String workingArea = rowData[6];

            if (processedWorkingArea.contains(workingArea)) {
                continue;
            }
            try {
                if (searchWorkingArea(driver, workingArea, rowData[5])) {
                    WebElement addBTN = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));  // Working Area Add button path
                    addBTN.click();
                    sleep(2000);

                    // Working Area Name-------------------
                    WebElement workingAreaName = driver.findElement(By.xpath("//input[@class='form-control form-control-sm col-md-12' and @id='txt_name']")); // name
                    workingAreaName.clear();
                    workingAreaName.sendKeys(workingArea);
                    childTest.log(Status.INFO, "Enter working area name: " + workingArea);

                    // Division select field------------------
                    WebElement WorkingDivisionName = driver.findElement(By.xpath("//select[@id='cbo_division']"));
                    Select division = new Select(WorkingDivisionName);
                    division.selectByVisibleText(rowData[1].strip());
                    childTest.log(Status.INFO, "Select division name: " + rowData[1]);
                    Thread.sleep(2000);

                    // Dist select field from working area-----------------
                    WebElement workingAreaDistrictName = driver.findElement(By.xpath("//select[@id='cbo_district' and @class='form-control form-control-sm form-control col-md-12']"));
                    Select distOfWorkingArea = new Select(workingAreaDistrictName);
                    distOfWorkingArea.selectByVisibleText(rowData[2].strip());
                    childTest.log(Status.INFO, "Select district name: " + rowData[2]);
                    Thread.sleep(2000);

                    //Thana name select from working Area------------------
                    WebElement workingAreaThanaName = driver.findElement(By.xpath("//select[@id='cbo_thana' and @class='form-control form-control-sm form-control col-md-12']"));
                    Select tha = new Select(workingAreaThanaName);
                    tha.selectByVisibleText(rowData[3].strip());
                    childTest.log(Status.INFO, "Select thana name: " + rowData[3]);
                    Thread.sleep(2000);

                    // Union name select from Working Area----------
                    WebElement workingAreaUnionNameVillageForm = driver.findElement(By.xpath("//select[@id='cbo_union'][@class='form-control form-control-sm form-control col-md-12']"));
                    Select union = new Select(workingAreaUnionNameVillageForm);
                    union.selectByVisibleText(rowData[4].strip());
                    childTest.log(Status.INFO, "Select union name: " + rowData[4]);
                    Thread.sleep(2000);

                    // Village name select from Working Area--------------------

                    WebElement workingAreaVillageName = driver.findElement(By.xpath("//select[@id='cbo_village'][@class='form-control form-control-sm form-control col-md-12']"));
                    Select village = new Select(workingAreaVillageName);
                    village.selectByVisibleText(rowData[5].strip());
                    childTest.log(Status.INFO, "Select village name: " + rowData[5]);
                    Thread.sleep(2000);


                    // Send code from Working Area------------------
                    WebElement workingAreaCode = driver.findElement(By.xpath("//input[@id='txt_code'][@class='form-control form-control-sm col-md-12']"));
                    if (!rowData[7].isEmpty()) {
                        workingAreaCode.clear();
                        workingAreaCode.sendKeys(rowData[7]);
                        childTest.log(Status.INFO, "Enter working area code: " + rowData[7]);
                    }

                    // Click Working Area Submit Form----------

                    WebElement submitWorkingArea = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-success btn-sm']"));
                    submitWorkingArea.click();
                    sleep(2000);
                    verifyAndHandleWorkingAreaMessage(driver, rowData[6]);
                }
                rowCount++;
                processedWorkingArea.add(workingArea);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    // Here is working Area name Searching code---------------------------------------
    public boolean searchWorkingArea(WebDriver driver, String workingAreaPath, String villageAreaPath) throws InterruptedException {
        WebElement searchFieldOfWorkingArea = driver.findElement(By.xpath("//input[@id='txt_name'][@class='mr-sm-2 form-control form-control-sm col-sm-1']"));
        searchFieldOfWorkingArea.clear();
        searchFieldOfWorkingArea.sendKeys(workingAreaPath);
        WebElement searchBTN = driver.findElement(By.id("custom-search-btn"));
        searchBTN.click();
        sleep(1500);
        WebElement tableDataOfWorkingArea = driver.findElement(By.xpath("//table/tbody"));
        String allData = tableDataOfWorkingArea.getText();
        return !(allData.contains(workingAreaPath) && allData.contains(villageAreaPath));
    }


    //Here is union and village searching code------------------------------------------------------
    public boolean search(WebDriver driver, String wordVillWorking, String union) throws InterruptedException {
        WebElement searchField = driver.findElement(By.xpath("//input[@placeholder='name' or @placeholder='Search By Name']"));
        searchField.clear();
        searchField.sendKeys(wordVillWorking);
        WebElement searchBTN = driver.findElement(By.id("custom-search-btn"));
        searchBTN.click();
        sleep(1500);
        WebElement newWardVillageWorkingArea = driver.findElement(By.xpath("//table/tbody"));
        String allData = newWardVillageWorkingArea.getText();
        return !(allData.contains(wordVillWorking) && allData.contains(union));
    }


    public void verifyAndHandleUnionMessage(WebDriver driver, String union) throws IOException {
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
            childTest.log(Status.PASS, union + " is Migrated ");
            Assert.assertTrue(true);
        } else {
            driver.findElement(By.xpath("//button[@class='btn mr-2 btn-danger btn-sm']")).click();
            childTest.log(Status.FAIL, union + " is not Migrated");
        }
    }

    public void verifyAndHandleVillageMessage(WebDriver driver, String village) throws IOException {
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
            childTest.log(Status.PASS, village + " is Migrated ");
            Assert.assertTrue(true);
        } else {
            driver.findElement(By.xpath("//button[@class='btn mr-2 btn-danger btn-sm']")).click();
            childTest.log(Status.FAIL, village + " is not Migrated");
        }
    }

    public void verifyAndHandleWorkingAreaMessage(WebDriver driver, String workingArea) throws IOException {
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


