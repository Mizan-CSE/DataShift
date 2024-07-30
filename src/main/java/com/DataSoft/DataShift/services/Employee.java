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
import java.util.List;

@Service
public class Employee {
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

    public String employeeMigration(AutomationRequest request) throws IOException {
        String filePath = ".\\dataset\\Migrated Information\\Migrated Employee.xlsx";
        xlutil.setPath(filePath);
        xlutil.setCellData("Sheet1", 0, 0, "Employee Name");
        xlutil.setCellData("Sheet1", 0, 1, "Employee Code");
        xlutil.setCellData("Sheet1", 0, 2, "Status");
        initializeRowCount(filePath);

        extent = new ExtentReports();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/report/Employee Migration.html");
        htmlReporter.config().setTheme(Theme.DARK);
        htmlReporter.config().setDocumentTitle("Employee Migration Report");
        extent.attachReporter(htmlReporter);

        WebDriver driver = config.getDriver(request.getBrowser());
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request); // Use WebDriver instance
            test = extent.createTest("Auto Employee Migration");
            test.log(Status.INFO, "Starting Employee migration...");
            config.employeePage(driver);
            startEmployeeMigration(driver, request);
            test.log(Status.PASS, "Employee migration successful");
            return "Employee migration successful";
        } catch (Exception e) {
            test.log(Status.FAIL, "Employee migration failed: " + e.getMessage());
            return "Employee migration failed: " + e.getMessage();
        } finally {
            extent.flush(); // Flush the report at the end
            driver.quit(); // Close WebDriver instance
        }
    }

    private String startEmployeeMigration(WebDriver driver, AutomationRequest request) throws IOException, InterruptedException {
        childTest = test.createNode("Employee Migration Data Entry");
        String[][] data = request.getCellData();
        for (String[] rowData : data) {
            try {
                WebElement empAddButton = driver.findElement(By.xpath("//button[@class='btn add btn-info btn-sm']"));
                empAddButton.click();
                Thread.sleep(2500);

                WebElement empName = driver.findElement(By.name("txt_name"));
                empName.clear();
                empName.sendKeys(rowData[0]);
                childTest.log(Status.INFO, "Enter employee name: " + rowData[0]);

                WebElement empCode = driver.findElement(By.name("txt_code"));
                empCode.clear();
                empCode.sendKeys(rowData[1]);
                childTest.log(Status.INFO, "Enter employee code: " + rowData[1]);

//                WebElement selectBranch = driver.findElement(By.xpath("//select[@name='cbo_branch']"));
//                List<WebElement> selectAllBranch = driver.findElements(By.xpath("//select[@name='cbo_branch']"));
//                Select branch = new Select(selectBranch);
//                List<WebElement> selectAllBranch = branch.getOptions();
//                for (WebElement branchWise : selectAllBranch) {
//                    if (branchWise.getText().contains(rowData[2].strip())) {
//                        branchWise.click();
//                    }
//                }

                WebElement empDesignation = driver.findElement(By.xpath("//select[@name='cbo_employee_designation']"));
                Select designation = new Select(empDesignation);
                List<WebElement> allEmpDesignation = designation.getOptions();
                for (WebElement empDesignate : allEmpDesignation) {
                    if (empDesignate.getText().contains(rowData[3].strip())) {
                        empDesignate.click();
                        childTest.log(Status.INFO, "Select employee designation: " + rowData[3]);
                        break;
                    }
                }


                WebElement empFather = driver.findElement(By.name("txt_fathers_name"));
                empFather.clear();
                if (!rowData[4].isEmpty()) {
                    if(!(rowData[4].equalsIgnoreCase("-") || rowData[4].equalsIgnoreCase("0"))){
                        empFather.sendKeys(rowData[4]);
                        childTest.log(Status.INFO, "Enter employee father name: " + rowData[4]);
                    }
                }
//                else {
//                    empFather.sendKeys("Null");
//                    childTest.log(Status.INFO, "Enter employee father name: Null ");
//                }

                WebElement empMother = driver.findElement(By.name("txt_mothers_name"));
                empMother.clear();
                if (!rowData[5].isEmpty()) {
                    if(!(rowData[5].equalsIgnoreCase("-") || rowData[5].equalsIgnoreCase("0"))) {
                        empMother.sendKeys(rowData[5]);
                        childTest.log(Status.INFO, "Enter employee mother name: " + rowData[5]);
                    }
                }
//                else {
//                    empMother.sendKeys("Null");
//                    childTest.log(Status.INFO, "Enter employee mother name: Null ");
//                }

                WebElement empSpouse = driver.findElement(By.name("txt_spouse_name"));
                empSpouse.clear();
                if (!rowData[6].isEmpty()) {
                    empSpouse.sendKeys(rowData[6]);
                    childTest.log(Status.INFO, "Enter employee spouse name: " + rowData[6]);
                }

                WebElement empPermanentAddress = driver.findElement(By.name("txt_permanent_address"));
                empPermanentAddress.clear();
                empPermanentAddress.sendKeys(rowData[7]);
                childTest.log(Status.INFO, "Enter employee permanent address: " + rowData[7]);

                WebElement empPresentAddress = driver.findElement(By.name("txt_present_address"));
                empPresentAddress.clear();
                empPresentAddress.sendKeys(rowData[8]);
                childTest.log(Status.INFO, "Enter employee present address: " + rowData[8]);

                WebElement empGender = driver.findElement(By.name("cbo_gender"));
                Select gen = new Select(empGender);
                if (rowData[9].strip().trim().equalsIgnoreCase("F") || rowData[9].strip().trim().equalsIgnoreCase("Female")) {
                    gen.selectByVisibleText("Female");
                    childTest.log(Status.INFO, "Select employee gender as female");
                } else if (rowData[9].strip().trim().equalsIgnoreCase("M") || rowData[9].strip().trim().equalsIgnoreCase("Male")) {
                    gen.selectByVisibleText("Male");
                    childTest.log(Status.INFO, "Select employee gender as male");
                }


                WebElement empMobileNo = driver.findElement(By.name("txt_mobile_no"));
                empMobileNo.clear();
                if (!rowData[10].isEmpty()) {
                    if (!rowData[10].strip().startsWith("0")) {
                        empMobileNo.sendKeys("0" + rowData[10].strip());
                    } else {
                        empMobileNo.sendKeys(rowData[10].strip());
                    }
                    childTest.log(Status.INFO, "Enter employee phone number: " + rowData[10]);
                }

                WebElement empEmail = driver.findElement(By.name("txt_email"));
                if (!rowData[11].isEmpty()) {
                    empEmail.clear();
                    empEmail.sendKeys(rowData[11]);
                    childTest.log(Status.INFO, "Enter employee email: " + rowData[11]);
                }

                WebElement selectEmpDegree = driver.findElement(By.xpath("//select[@name='cbo_last_achieved_degree']"));
                Select empDegree = new Select(selectEmpDegree);
                List<WebElement> selectAllEmpDegree = empDegree.getOptions();
                for (WebElement degreeWise : selectAllEmpDegree) {
                    if (degreeWise.getText().contains(rowData[12].strip())) {
                        degreeWise.click();
                        childTest.log(Status.INFO, "Enter employee education qualification: " + rowData[12]);
                        break;
                    }
                }

                WebElement empBirthDate = driver.findElement(By.xpath("//input[@name='txt_date_of_birth']"));
                empBirthDate.clear();
                empBirthDate.sendKeys(rowData[13].strip());
                childTest.log(Status.INFO, "Enter employee date of birth: " + rowData[13]);

                WebElement empJoiningDate = driver.findElement(By.xpath("//input[@name='txt_date_of_joining']"));
                empJoiningDate.clear();
                empJoiningDate.sendKeys(rowData[14].strip());
                childTest.log(Status.INFO, "Enter employee date of birth: " + rowData[14]);

                WebElement selectEmpCanManageLoan = driver.findElement(By.xpath("//select[@name='cbo_is_field_officer']"));
                Select manageLoan = new Select(selectEmpCanManageLoan);
                if (rowData[15].strip().trim().equalsIgnoreCase("1") || rowData[15].strip().trim().equalsIgnoreCase("Yes") || rowData[15].strip().trim().equalsIgnoreCase("Y")) {
                    manageLoan.selectByVisibleText("Yes");
                } else if (rowData[15].strip().trim().equalsIgnoreCase("0") || rowData[15].strip().trim().equalsIgnoreCase("No") || rowData[15].strip().trim().equalsIgnoreCase("N")) {
                    manageLoan.selectByVisibleText("No");
                }
                childTest.log(Status.INFO, "Can employee manage loan: " + rowData[15]);

                WebElement empSecurityMoney = driver.findElement(By.name("txt_secuirity_money"));
                if (!rowData[16].isEmpty()) {
                    empSecurityMoney.clear();
                    empSecurityMoney.sendKeys(rowData[16]);
                    childTest.log(Status.INFO, "Enter employee security money: " + rowData[16]);
                }

                WebElement empStartingSalary = driver.findElement(By.name("txt_starting_salary"));
                if (!rowData[17].isEmpty()) {
                    empStartingSalary.clear();
                    empStartingSalary.sendKeys(rowData[17]);
                    childTest.log(Status.INFO, "Enter employee starting salary: " + rowData[17]);
                }

                WebElement empCurrentSalary = driver.findElement(By.name("txt_current_salary"));
                if (!rowData[18].isEmpty()) {
                    empCurrentSalary.clear();
                    empCurrentSalary.sendKeys(rowData[18]);
                    childTest.log(Status.INFO, "Enter employee current salary: " + rowData[17]);
                }

                if (!rowData[19].isEmpty() && !rowData[19].strip().equals("0")) {
                    WebElement nationalIdentification = driver.findElement(By.xpath("//input[@name='txt_national_id']"));
                    nationalIdentification.clear();
                    nationalIdentification.sendKeys(rowData[19].strip());
                    childTest.log(Status.INFO, "Enter employee NID: " + rowData[19]);
                }


                WebElement smartIdentification = driver.findElement(By.xpath("//input[@name='txt_smart_id']"));
                smartIdentification.clear();
                smartIdentification.sendKeys(rowData[20].strip());
                childTest.log(Status.INFO, "Enter employee smart ID: " + rowData[20]);

                if (!rowData[21].isEmpty()) {
                    WebElement selectBloodGroup = driver.findElement(By.xpath("//select[@name='cbo_blood_group']"));
                    Select bloodGroup = new Select(selectBloodGroup);
                    List<WebElement> selectAllBloodGroup = bloodGroup.getOptions();
                    for (WebElement groupWise : selectAllBloodGroup) {
                        if (groupWise.getText().contains(rowData[21].strip())) {
                            groupWise.click();
                            break;
                        }
                    }
                }

                if (!rowData[22].isEmpty()) {
                    WebElement empStatus = driver.findElement(By.name("cbo_status"));
                    Select stat = new Select(empStatus);
                    if (rowData[22].strip().trim().equalsIgnoreCase("I") || rowData[22].strip().trim().equalsIgnoreCase("Inactive") || rowData[22].strip().trim().equalsIgnoreCase("0")) {
                        stat.selectByVisibleText("Inactive");
                    } else if (rowData[22].strip().trim().equalsIgnoreCase("A") || rowData[22].strip().trim().equalsIgnoreCase("Active") || rowData[22].strip().trim().equalsIgnoreCase("1")) {
                        stat.selectByVisibleText("Active");
                    } else if (rowData[22].strip().trim().equalsIgnoreCase("T") || rowData[22].strip().trim().equalsIgnoreCase("Terminated") || rowData[22].strip().trim().equalsIgnoreCase("2")) {
                        stat.selectByVisibleText("Terminated");
                    }
                }

                WebElement empSave = driver.findElement(By.xpath("//button[@class='btn btn-success btn-sm']"));
                empSave.click();
                Thread.sleep(3000);
                verifyAndHandleEmployeeToastMessage(driver, rowData[0], rowData[1]);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void verifyAndHandleEmployeeToastMessage(WebDriver driver, String employee, String code) throws IOException {
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
            xlutil.setCellData("Sheet1", rowCount, 0, employee);
            xlutil.setCellData("Sheet1", rowCount, 1, "Employee is Migrated");
            xlutil.setCellData("Sheet1", rowCount, 2, code);
            childTest.log(Status.PASS, employee +"("+code +")-" + " is Migrated ");
            Assert.assertTrue(true);
        } else {
            driver.findElement(By.xpath("//button[@class='btn btn-danger btn-sm']")).click();
            xlutil.setCellData("Sheet1", rowCount, 0, employee);
            xlutil.setCellData("Sheet1", rowCount, 1, "Employee is Migrated");
            xlutil.setCellData("Sheet1", rowCount, 2, code);
            childTest.log(Status.FAIL, employee +"("+code +")-" + "not is Migrated ");
        }
    }
}

