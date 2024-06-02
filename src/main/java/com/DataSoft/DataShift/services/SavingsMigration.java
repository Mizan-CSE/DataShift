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
public class SavingsMigration {
    @Autowired
    SeleniumConfig config;
    @Autowired
    XLUtility xlutil;
    int rowCount = 1;
    String systemGeneratedSavingsCode, status = "";
    int successCount = 0;
    int failureCount = 0;
    int threadCount = 4;
    private ExtentReports extent;
    private ExtentTest test;


    //Check the cell value is number
    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

//    public String savingsMigration(AutomationRequest request) throws IOException {
//        String filePath = ".\\dataset\\Migrated Information\\Migrated Savings.xlsx";
//        xlutil.setPath(filePath);
//        xlutil.setCellData("Sheet1", 0, 0, "Samity");
//        xlutil.setCellData("Sheet1", 0, 1, "Member Information");
//        xlutil.setCellData("Sheet1", 0, 2, "System Generated Savings Code");
//        xlutil.setCellData("Sheet1", 0, 3, "Status");
//
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        List<Future<String>> futures = new ArrayList<>();
//
//        extent = new ExtentReports();
//        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/Savings Migration.html");
//
//        htmlReporter.config().setTheme(Theme.DARK);
//        htmlReporter.config().setDocumentTitle("Savings Migration Report");
//
//        extent.attachReporter(htmlReporter);
//
//        test = extent.createTest("Savings Auto Migration");
//        test.log(Status.INFO, "Starting Savings migration...");
//
//        String[][] data = request.getCellData();
//        for (String[] rowData : data) {
//            Callable<String> task = () -> {
//                WebDriver driver = config.getDriver(request.getBrowser());
//                if (driver == null) {
//                    return "Unsupported browser: " + request.getBrowser();
//                }
//
//                try {
//                    config.login(driver, request);
//                    config.loansSavingsMigrationPage(driver);
//                    selectSamityForSavingsMigration(driver, rowData);
//                    startSavingsMigration(driver, rowData);
//
//                    if (Objects.equals(status, "Savings Migrated")) {
//                        String memberInformation = rowData[0] + " " + storeMemberCode;
//                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[5]);
//                        xlutil.setCellData("Sheet1", rowCount, 1, rowData[8]);
//                        xlutil.setCellData("Sheet1", rowCount, 2, memberInformation);
//                        xlutil.setCellData("Sheet1", rowCount, 3, status);
//                        test.log(Status.PASS, "Savings migration successful");
//                        rowCount++;
//                    }
//                    return rowData[5] + status;
//                } catch (Exception e) {
//                    test.log(Status.FAIL, "Savings migration failed: " + e.getMessage());
//                    return status + e.getMessage();
//                } finally {
//                    extent.flush(); // Flush the report at the end
//                    driver.quit(); // Close WebDriver instance
//                }
//            };
//            futures.add(executorService.submit(task));
//        }
//
//        for (Future<String> future : futures) {
//            try {
//                String result = future.get();
//                if (result.startsWith("Savings migration failed:")) {
//                    failureCount++;
//                    System.out.println(result);
//                } else {
//                    successCount++;
//                }
//            } catch (InterruptedException | ExecutionException e) {
//                failureCount++;
//                System.out.println("Savings migration failed: " + e.getMessage());
//            }
//        }
//
//        executorService.shutdown();
//
//        System.out.println("Savings migration successful: " + successCount);
//        System.out.println("Savings migration failed: " + failureCount);
//        return status;
//    }

    public String savingsMigration(AutomationRequest request) throws IOException {
        extent = new ExtentReports();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/Savings Migration.html");
        htmlReporter.config().setTheme(Theme.DARK);
        htmlReporter.config().setDocumentTitle("Savings Migration Report");
        extent.attachReporter(htmlReporter);

        WebDriver driver = config.getDriver(request.getBrowser());
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request);
            test = extent.createTest("Auto Savings Migration");
            test.log(Status.INFO, "Starting Savings migration...");
            config.loansSavingsMigrationPage(driver);
            startSavingsMigration(driver, request);
            test.log(Status.PASS, "Savings migration successful");
            return "Savings migration successful";
        } catch (Exception e) {
            test.log(Status.FAIL, "Savings migration failed: " + e.getMessage());
            return "Savings migration failed: " + e.getMessage();
        } finally {
            extent.flush(); // Flush the report at the end
            driver.quit(); // Close WebDriver instance
        }
    }

    private String startSavingsMigration(WebDriver driver, AutomationRequest request) throws IOException {
        String filePath = ".\\dataset\\Migrated Information\\Migrated Savings.xlsx";
        xlutil.setPath(filePath);
        xlutil.setCellData("Sheet1", 0, 0, "System Generated Samity Information");
        xlutil.setCellData("Sheet1", 0, 1, "Member Code");
        xlutil.setCellData("Sheet1", 0, 2, "System Generated Savings Code");
        xlutil.setCellData("Sheet1", 0, 3, "Status");

        ExtentTest childTest = test.createNode("Savings Migration Data Entry");
        WebElement samitySearch = driver.findElement(By.xpath("//select[@name='cbo_samity_id']"));
        Select samity = new Select(samitySearch);
        String[][] data = request.getCellData();
        for (String[] rowData : data) {
            try {
                samity.selectByVisibleText(rowData[2].strip());
                driver.findElement(By.xpath("//button[@class='btn ml-4 btn-primary btn-sm']")).click();
                Thread.sleep(2000);

                List<WebElement> savingsAndLoansName = driver.findElements(By.xpath("//table/thead/tr[2]/th"));
                int selectedSavingsIndex = -1;
                for (int i = 0; i < savingsAndLoansName.size(); i++) {
                    if (savingsAndLoansName.get(i).getText().equals(rowData[4])) {
                        selectedSavingsIndex = i + 1;
                        break;
                    }
                }
                List<WebElement> memberInformation = driver.findElements(By.xpath("//table[@class='table table-sm table-bordered']/tbody/tr/td[2]"));

                int selectedMemberIndex = -1;
                for (int i = 0; i < memberInformation.size(); i++) {
                    if (memberInformation.get(i).getText().contains(rowData[3])) {
                        selectedMemberIndex = i + 1;
                        break;
                    }
                }

                String plusIcon = "//table[@class='table table-sm table-bordered']/tbody[" + selectedMemberIndex + "]/tr/td[" + (selectedSavingsIndex + 2) + "]";
                WebElement PlusButton = driver.findElement(By.xpath(plusIcon));
                PlusButton.click();
                Thread.sleep(2000);

                WebElement savingsCycle = driver.findElement(By.xpath("(//input[@name='Transaction Date'])[3]"));
                savingsCycle.clear();
                savingsCycle.sendKeys(rowData[6]);

                WebElement savingsCode = driver.findElement(By.xpath("(//input[@name='Transaction Date'])[4]"));
                systemGeneratedSavingsCode = savingsCode.getText();

                List<WebElement> savingsOpeningBalanceLabel = driver.findElements(By.xpath("//label[contains(text(),'Opening Balance')]"));
                int isOpeningBalanceLabelPresent = savingsOpeningBalanceLabel.size();
                if (isOpeningBalanceLabelPresent > 0) {
                    if (!rowData[7].isEmpty()) {
                        WebElement savingsOpeningBalance = driver.findElement(By.xpath("(//input[@name='Transaction Date'])[5]"));
                        savingsOpeningBalance.clear();
                        savingsOpeningBalance.sendKeys(rowData[7]);
                    }
                }

                WebElement savingsOpeningDate = driver.findElement(By.xpath("//input[@placeholder='Date From']"));
                savingsOpeningDate.clear();
                savingsOpeningDate.sendKeys(rowData[8]);
                savingsCycle.click();

                List<WebElement> autoProcessAmountLabel = driver.findElements(By.xpath("//label[contains(text(),'auto_process_amount:')]"));
                int isAutoprocessAmountLabelPresent = autoProcessAmountLabel.size();
                if (isAutoprocessAmountLabelPresent > 0) {
                    if (!rowData[9].isEmpty()) {
                        WebElement autoProcessAmount = driver.findElement(By.xpath("(//input[@name='Transaction Date'])[6]"));
                        autoProcessAmount.clear();
                        autoProcessAmount.sendKeys(rowData[9]);
                    }

                    WebElement saveSavingsInformation = driver.findElement(By.xpath("//button[@class='btn add btn-success btn-sm']"));
                    saveSavingsInformation.click();
                    Thread.sleep(3000);

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
                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[2]);
                        xlutil.setCellData("Sheet1", rowCount, 1, rowData[3]);
                        xlutil.setCellData("Sheet1", rowCount, 2, systemGeneratedSavingsCode);
                        xlutil.setCellData("Sheet1", rowCount, 3, "Savings is Migrated");
                        childTest.log(Status.PASS, "Savings of " + rowData[3] + " is Migrated ");
                        Assert.assertTrue(true);

                    } else {
                        WebElement cancelButton = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-warning btn-sm']"));
                        cancelButton.click();
                        Thread.sleep(2000);
                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[2]);
                        xlutil.setCellData("Sheet1", rowCount, 1, rowData[3]);
                        xlutil.setCellData("Sheet1", rowCount, 2, "");
                        xlutil.setCellData("Sheet1", rowCount, 3, "Savings is not Migrated");
                        childTest.log(Status.FAIL, "Savings of " + rowData[3] + " is not Migrated ");
                    }

                } else {

                    String periodTypeConversion;
                    if (rowData[10].contains(".")) {
                        periodTypeConversion = String.valueOf(Double.parseDouble(rowData[10]));
                    } else {
                        periodTypeConversion = String.valueOf(Integer.parseInt(rowData[10]));
                    }
                    WebElement savingsPeriod = driver.findElement(By.xpath("//select[@name='cbo_period']"));
                    Select selectPeriod = new Select(savingsPeriod);
                    List<WebElement> allPeriod = selectPeriod.getOptions();
                    for (WebElement period : allPeriod) {
                        if (period.getText().contains(periodTypeConversion)) {
                            period.click();
                        }
                    }
//                    selectPeriod.selectByVisibleText(rowData[10]);
                    Thread.sleep(1000);

                    WebElement selectMonthlyDepositAmount = driver.findElement(By.xpath("//select[@name='cbo_installment_amount']"));
                    Select monthlyDepositAmount = new Select(selectMonthlyDepositAmount);
                    double monthlyDeposit = Double.parseDouble(rowData[9]);
                    monthlyDepositAmount.selectByVisibleText(String.format("%.2f", monthlyDeposit));
                    Thread.sleep(1000);

                    WebElement payableAmount = driver.findElement(By.xpath("//input[@name='txt_repayment_amount']"));
                    payableAmount.clear();
                    payableAmount.sendKeys(rowData[13]);
                    Thread.sleep(1000);

                    List<WebElement> isInterestCalculationPeriodPresent = driver.findElements(By.xpath("//label[@for='cbo_interest_calculation_period']"));
                    if (!isInterestCalculationPeriodPresent.isEmpty()) {
                        WebElement calculationPeriod = driver.findElement(By.xpath("//select[@name='cbo_interest_calculation_period']"));
                        Select interestCalculationPeriod = new Select(calculationPeriod);
                        if (!rowData[12].isEmpty()) {
                            if (rowData[12].strip().equalsIgnoreCase("M") || rowData[12].strip().equalsIgnoreCase("Monthly")) {
                                interestCalculationPeriod.selectByVisibleText("Monthly");
                            } else if (rowData[12].strip().equalsIgnoreCase("Q") || rowData[12].strip().equalsIgnoreCase("Quaterly")) {
                                interestCalculationPeriod.selectByVisibleText("Quaterly");
                            } else if (rowData[12].strip().equalsIgnoreCase("HY") || rowData[12].strip().equalsIgnoreCase("Half-Yearly")) {
                                interestCalculationPeriod.selectByVisibleText("Half-Yearly");
                            } else {
                                interestCalculationPeriod.selectByVisibleText("Yearly");
                            }
                        }
                    }

                    List<WebElement> nomineeInformation = driver.findElements(By.xpath("//*[contains(text(),'Share (%)')]"));
                    int isNomineeInformationPresent = nomineeInformation.size();
                    if (isNomineeInformationPresent > 0) {
                        WebElement nomineeName = driver.findElement(By.xpath("//input[@name='yearundefined']"));
                        nomineeName.clear();
                        nomineeName.sendKeys(rowData[17]);

                        WebElement nomineeRelation = driver.findElement(By.xpath("//input[@name='collectionundefined']"));
                        nomineeRelation.clear();
                        nomineeRelation.sendKeys(rowData[18]);

                        WebElement nomineeShare = driver.findElement(By.xpath("//input[@name='maturedundefined']"));
                        nomineeShare.clear();
                        nomineeShare.sendKeys("100");
                    }

                    WebElement saveSavingsInformation = driver.findElement(By.xpath("//button[@class='btn add btn-success btn-sm']"));
                    saveSavingsInformation.click();
                    Thread.sleep(3000);

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
                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[2]);
                        xlutil.setCellData("Sheet1", rowCount, 1, rowData[3]);
                        xlutil.setCellData("Sheet1", rowCount, 2, systemGeneratedSavingsCode);
                        xlutil.setCellData("Sheet1", rowCount, 3, "Savings is Migrated");
                        childTest.log(Status.PASS, "Savings of " + rowData[3] + " is Migrated ");
                        Assert.assertTrue(true);

                    } else {
                        WebElement cancelButton = driver.findElement(By.xpath("//button[@class='btn mr-2 btn-warning btn-sm']"));
                        cancelButton.click();
                        Thread.sleep(2000);

                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[2]);
                        xlutil.setCellData("Sheet1", rowCount, 1, rowData[3]);
                        xlutil.setCellData("Sheet1", rowCount, 2, "");
                        xlutil.setCellData("Sheet1", rowCount, 3, "Savings is not Migrated");
                        childTest.log(Status.FAIL, "Savings of " + rowData[3] + " is not Migrated ");
                    }
                }
                rowCount++;


            } catch (Exception e) {
                childTest.log(Status.FAIL, "Failed with error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}
