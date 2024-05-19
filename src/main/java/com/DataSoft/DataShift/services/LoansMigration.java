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
import org.openqa.selenium.Keys;
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
public class LoansMigration {
    @Autowired
    SeleniumConfig config;
    @Autowired
    XLUtility xlutil;
    String systemGeneratedLoansCode, status;
    int rowCount = 0, threadCount = 1;
    private ExtentReports extent;
    private ExtentTest test;


    public String loansMigration(AutomationRequest request) throws IOException {
        extent = new ExtentReports();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/Loans Migration.html");
        htmlReporter.config().setTheme(Theme.DARK);
        htmlReporter.config().setDocumentTitle("Loans Migration Report");
        extent.attachReporter(htmlReporter);
        test = extent.createTest("Auto Loans Migration");
        test.log(Status.INFO, "Starting Loans migration...");

        WebDriver driver = config.getDriver(request.getBrowser());
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request); // Use WebDriver instance
            test = extent.createTest("Auto Loans Migration");
            test.log(Status.INFO, "Starting Loans migration...");
            config.loansSavingsMigrationPage(driver);
            startLoansMigration(driver, request);
            test.log(Status.PASS, "Loans migration successful");
            return "Loans migration successful";
        } catch (Exception e) {
            test.log(Status.FAIL, "Loans migration failed: " + e.getMessage());
            return "Loans migration failed: " + e.getMessage();
        } finally {
            extent.flush(); // Flush the report at the end
            driver.quit(); // Close WebDriver instance
        }
    }

    private String startLoansMigration(WebDriver driver, AutomationRequest request) throws IOException, InterruptedException {
        String filePath = ".\\dataset\\Migrated Information\\Migrated Loans.xlsx";
        xlutil.setPath(filePath);
        xlutil.setCellData("Sheet1", 0, 0, "System Generated Samity Information");
        xlutil.setCellData("Sheet1", 0, 1, "Member Code");
        xlutil.setCellData("Sheet1", 0, 2, "System Generated Loan Code");
        xlutil.setCellData("Sheet1", 0, 3, "Status");

        ExtentTest childTest = test.createNode("Loans Migration Data Entry");
        WebElement samitySearch = driver.findElement(By.xpath("//select[@name='cbo_samity_id']"));
        Select samity = new Select(samitySearch);
        String[][] data = request.getCellData();
        for (String[] rowData : data) {
            try {
                samity.selectByVisibleText(rowData[1]);
//                List<WebElement> allSamity = samity.getOptions();
//                for (WebElement samityWise : allSamity) {
//                    if (samityWise.getText().contains(rowData[1])) {
//                        samityWise.click();
//                    }
//                }
                driver.findElement(By.xpath("//button[@class='btn ml-4 btn-primary btn-sm']")).click();
                Thread.sleep(2000);

                // Loans Product Header
                String loansProductHeader = rowData[4].trim();
                String[] splitLoansProduct = loansProductHeader.split("[-\\s]");
                String firstWordOfLoansProduct = splitLoansProduct.length > 0 ? splitLoansProduct[0] : loansProductHeader;

                List<WebElement> savingsAndLoansName = driver.findElements(By.xpath("//table/thead/tr[2]/th"));  // web savings screen headers
                int selectedSavingsIndex = -1;
                for (int i = 0; i < savingsAndLoansName.size(); i++) {
                    if (savingsAndLoansName.get(i).getText().equals(firstWordOfLoansProduct)) {
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

                WebElement disbursementDate = driver.findElement(By.xpath("//input[@class='form-control form-control-sm hasDatepicker']"));
                disbursementDate.clear();
                disbursementDate.sendKeys(rowData[5]);
                childTest.log(Status.INFO, "Enter disbursement date: " + rowData[5]);

                WebElement loanProductDropdown = driver.findElement(By.xpath("//select[@name='product']"));
                Select loanProductSelectDropdown = new Select(loanProductDropdown);

                List<WebElement> allLoansProduct = loanProductSelectDropdown.getOptions();
                for (WebElement productWise : allLoansProduct) {
                    if (productWise.getText().contains(rowData[4])) {
                        productWise.click();
                        childTest.log(Status.INFO, "Select loan product: " + rowData[4]);
                    }
                }
                Thread.sleep(1000);

                WebElement frequency = driver.findElement(By.xpath("//select[@name='cbo_repayment_frequency']"));
                Select repayFrequency = new Select(frequency);
                int totalRepayFrequency = repayFrequency.getOptions().size();
                if (totalRepayFrequency == 2) {
                    repayFrequency.selectByIndex(1);
                } else if (rowData[7].equalsIgnoreCase("W") || rowData[7].equalsIgnoreCase("Weekly")) {
                    repayFrequency.selectByValue("W");
                } else if (rowData[7].equalsIgnoreCase("M") || rowData[7].equalsIgnoreCase("Monthly")) {
                    repayFrequency.selectByValue("M");
                }
                childTest.log(Status.INFO, "Select repayment frequency: " + repayFrequency.getFirstSelectedOption().getText());

                WebElement loanCycle = driver.findElement(By.xpath("//input[@name='txt_cycle']"));
                if (!rowData[10].isEmpty() && !rowData[10].equalsIgnoreCase("0")) {
                    loanCycle.clear();
                    loanCycle.sendKeys(rowData[10]);
                    childTest.log(Status.INFO, "Enter loan cycle " + rowData[10]);
                } else {
                    loanCycle.sendKeys("1");
                    childTest.log(Status.INFO, "Enter loan cycle: 1" );
                }
                Thread.sleep(1000);

                WebElement loanAmount = driver.findElement(By.xpath("//input[@name='txt_loan_amount']"));
                loanAmount.clear();
                loanAmount.sendKeys(rowData[11]);
                childTest.log(Status.INFO, "Enter loan amount: " +rowData[11]);

                WebElement numberOfRepayment = driver.findElement(By.xpath("//select[@name='txt_number_of_installment']"));
                Select installmentNumber = new Select(numberOfRepayment);
                int totalInstallment = installmentNumber.getOptions().size();
                if (totalInstallment == 2) {
                    installmentNumber.selectByIndex(0);
                    childTest.log(Status.INFO, "Enter installment number: " +installmentNumber.getFirstSelectedOption().getText());
                } else if (!rowData[12].isEmpty() && !rowData[12].equalsIgnoreCase("0")) {
                    installmentNumber.selectByVisibleText(rowData[12]);
                    childTest.log(Status.INFO, "Enter installment number: " +rowData[12]);
                }
                Thread.sleep(1000);

                WebElement insuranceAmount = driver.findElement(By.xpath("//input[@name='txt_insurance_amount']"));
                if (!rowData[13].isEmpty() && !rowData[13].equalsIgnoreCase("0")) {
                    insuranceAmount.clear();
                    insuranceAmount.sendKeys(rowData[13]);
                    childTest.log(Status.INFO, "Enter insurance amount: " +rowData[13]);
                }

                WebElement loanPurpose = driver.findElement(By.xpath("//select[@name='cbo_loan_purpose']"));
                Select loanPurposeDropdown = new Select(loanPurpose);
                loanPurposeDropdown.selectByVisibleText(rowData[14]);
                childTest.log(Status.INFO, "Enter loan Purpose: " +rowData[14]);

                WebElement folioNumber = driver.findElement(By.xpath("//input[@name='txt_folio_number']"));
                if (!rowData[15].isEmpty() && !rowData[15].equalsIgnoreCase("0")) {
//                    folioNumber.clear();
                    folioNumber.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                    folioNumber.sendKeys(rowData[15]);
                    childTest.log(Status.INFO, "Enter folio number: " +rowData[15]);
                }

                WebElement interestDiscountAmount = driver.findElement(By.xpath("//input[@name='txt_discount_interest_amount']"));
                if (!rowData[16].isEmpty() && !rowData[16].equalsIgnoreCase("0")) {
                    //interestDiscountAmount.clear();
                    interestDiscountAmount.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                    interestDiscountAmount.sendKeys(rowData[16]);
                    childTest.log(Status.INFO, "Enter interest discount amount: " +rowData[16]);
                }

                WebElement installmentAmount = driver.findElement(By.xpath("//input[@name='txt_installment_amount']"));
//                installmentAmount.clear();
                installmentAmount.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                installmentAmount.sendKeys(rowData[17]);
                childTest.log(Status.INFO, "Enter installment amount: " +rowData[17]);

                WebElement OpeningLoanOutstanding = driver.findElement(By.xpath("//input[@name='txt_opening_balance']"));
//                OpeningLoanOutstanding.clear();
                OpeningLoanOutstanding.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                OpeningLoanOutstanding.sendKeys(rowData[18]);
                childTest.log(Status.INFO, "Enter Opening loan outstanding: " +rowData[18]);

//                WebElement extraInstallment = driver.findElement(By.xpath("//input[@name='txt_extra_installment_amount']"));
//                if (!rowData[19].isEmpty() && !rowData[19].equalsIgnoreCase("0")) {
//                    extraInstallment.sendKeys(Keys.chord(Keys.CONTROL, "a"));
//                    extraInstallment.sendKeys(rowData[19]);
//                }

                WebElement loansCode = driver.findElement(By.xpath("//input[@name='loan_code']"));
                systemGeneratedLoansCode = loansCode.getAttribute("value");

                WebElement clickNxtBtn = driver.findElement(By.xpath("//button[@title='Next Step']"));
                clickNxtBtn.click();
                Thread.sleep(2000);

                if (!rowData[20].isEmpty()) {
                    WebElement guarantorName = driver.findElement(By.xpath("//input[@name='txt_guarantor_name_1']"));
                    guarantorName.clear();
                    guarantorName.sendKeys(rowData[20]);
                    childTest.log(Status.INFO, "Enter guarantor name: " +rowData[20]);
                }
                if (!rowData[21].isEmpty()) {
                    WebElement guarantorRelation = driver.findElement(By.xpath("//input[@name='txt_guarantor_relationship_1']"));
                    guarantorRelation.clear();
                    guarantorRelation.sendKeys(rowData[21]);
                    childTest.log(Status.INFO, "Enter guarantor relation: " +rowData[21]);
                }
                if (!rowData[22].isEmpty()) {
                    WebElement addressOfGuarantor = driver.findElement(By.xpath("//input[@name='txt_guarantor_address_1']"));
                    addressOfGuarantor.clear();
                    addressOfGuarantor.sendKeys(rowData[22]);
                    childTest.log(Status.INFO, "Enter guarantor address: " +rowData[22]);
                }
                if (!rowData[23].isEmpty()) {
                    WebElement contractOfGuarantor = driver.findElement(By.xpath("//input[@name='txt_guarantor_contact_1']"));
                    contractOfGuarantor.clear();
                    contractOfGuarantor.sendKeys(rowData[23]);
                    childTest.log(Status.INFO, "Enter guarantor contact number: " +rowData[23]);
                }

                WebElement saveLoansMigration = driver.findElement(By.xpath("//button[@title='Submit']"));
                saveLoansMigration.click();
                Thread.sleep(3000);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(4));
                WebElement toastMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='toast-title']")));

                if (toastMessage.getText().equalsIgnoreCase("Success")) {
                    xlutil.setCellData("Sheet1", rowCount, 0, rowData[1]);
                    xlutil.setCellData("Sheet1", rowCount, 1, rowData[2]);
                    xlutil.setCellData("Sheet1", rowCount, 2, systemGeneratedLoansCode);
                    xlutil.setCellData("Sheet1", rowCount, 3, "Loans is Migrated");
                    childTest.log(Status.PASS, "Loans of " + rowData[3] + " is Migrated ");
                    Assert.assertTrue(true);
                    rowCount++;
                    status = "Loans of " + rowData[3] + " is Migrated";
                } else {
                    status = "Loans of " + rowData[3] + " is not Migrated";
                }

            } catch (Exception e) {
                childTest.log(Status.FAIL, "Failed with error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return status;
    }
}