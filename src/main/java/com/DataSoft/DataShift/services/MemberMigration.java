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
public class MemberMigration {
    @Autowired
    SeleniumConfig config;
    XLUtility xlutil = new XLUtility();

    String storeMemberCode, memberSamity, status = "";
    int successCount = 0, failureCount = 0, rowCount = 0, threadCount = 1;
    private ExtentReports extent;
    private ExtentTest test;

    //Check the cell value is number
    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    //     Migration with single thread
    public String memberMigration(AutomationRequest request) throws IOException {
        extent = new ExtentReports();
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/Member Migration.html");
        htmlReporter.config().setTheme(Theme.DARK);
        htmlReporter.config().setDocumentTitle("Member Migration Report");
        extent.attachReporter(htmlReporter);
        test = extent.createTest("Auto Member Migration");
        test.log(Status.INFO, "Starting member migration...");

        WebDriver driver = config.getDriver(request.getBrowser());
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request); // Use WebDriver instance
            test = extent.createTest("Auto Member Migration");
            test.log(Status.INFO, "Starting member migration...");
            config.memberMigrationPage(driver);
            startMemberMigration(driver, request);
            test.log(Status.PASS, "Member migration successful");
            return "Member migration successful";
        } catch (Exception e) {
            test.log(Status.FAIL, "Member migration failed: " + e.getMessage());
            return "Member migration failed: " + e.getMessage();
        } finally {
            extent.flush(); // Flush the report at the end
            driver.quit(); // Close WebDriver instance
        }
    }

//    public String memberMigration(AutomationRequest request) throws IOException {
//        xlutil.setCellData("Sheet1", 0, 0, "Samity");
//        xlutil.setCellData("Sheet1", 0, 1, "Member Code");
//        xlutil.setCellData("Sheet1", 0, 2, "System Generated Member Information");
//        xlutil.setCellData("Sheet1", 0, 3, "Status");
//
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        List<Future<String>> futures = new ArrayList<>();
//
//        extent = new ExtentReports();
//        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/Migration.html");
//
//        htmlReporter.config().setTheme(Theme.DARK);
//        htmlReporter.config().setDocumentTitle("Member Migration Report");
//
//        extent.attachReporter(htmlReporter);
//
//        test = extent.createTest("Auto Member Migration");
//        test.log(Status.INFO, "Starting member migration...");
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
//                    config.migrationPage(driver);
//                    selectSamityForMemberMigration(driver, rowData);
//                    startMemberMigration(driver, rowData);
//                    String memberInformation = rowData[0] + " " + storeMemberCode;
//
//                    if (Objects.equals(status, "Member Migrated")) {
//                        xlutil.setCellData("Sheet1", rowCount, 0, rowData[5]);
//                        xlutil.setCellData("Sheet1", rowCount, 1, rowData[8]);
//                        xlutil.setCellData("Sheet1", rowCount, 2, memberInformation);
//                        xlutil.setCellData("Sheet1", rowCount, 3, status);
//                        test.log(Status.PASS, "Member migration successful");
//                        rowCount++;
//                    }
//                    return rowData[5] + status;
//                } catch (Exception e) {
//                    test.log(Status.FAIL, "Member migration failed: " + e.getMessage());
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
//                if (result.startsWith("Member migration failed:")) {
//                    failureCount++;
//                    System.out.println(result);
//                } else {
//                    successCount++;
//                }
//            } catch (InterruptedException | ExecutionException e) {
//                failureCount++;
//                System.out.println("Member migration failed: " + e.getMessage());
//            }
//        }
//
//        executorService.shutdown();
//
//        System.out.println("Member migration successful: " + successCount);
//        System.out.println("Member migration failed: " + failureCount);
//        return status;
//    }

    private String startMemberMigration(WebDriver driver, AutomationRequest request) throws IOException {

        xlutil.setCellData("Sheet1", 0, 0, "Samity");
        xlutil.setCellData("Sheet1", 0, 1, "Member Code");
        xlutil.setCellData("Sheet1", 0, 2, "System Generated Member Information");
        xlutil.setCellData("Sheet1", 0, 3, "Status");

        ExtentTest childTest = test.createNode("Member Migration Data Entry");
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

                WebElement admissionDate = driver.findElement(By.xpath("//input[@name='form_field_data.txt_registration_date']"));
                admissionDate.clear();
                admissionDate.sendKeys(rowData[2]);
                memberCode.click();
                childTest.log(Status.INFO, "Enter member admission date: " + rowData[2]);

                WebElement primaryProduct = driver.findElement(By.xpath("//select[@name='form_field_data.cbo_product']"));
                Select primaryProd = new Select(primaryProduct);
//            if (isNumeric(rowData[3])) {
//                primaryProd.selectByValue(rowData[3]);
//            } else {
//                primaryProd.selectByVisibleText(rowData[3]);
//            }
                List<WebElement> allPrimaryProduct = primaryProd.getOptions();
                for (WebElement product : allPrimaryProduct) {
                    if (product.getText().contains(rowData[3])) {
                        product.click();
                        childTest.log(Status.INFO, "Select member primary product: " + rowData[3]);
                        break;
                    }
                }
                Thread.sleep(1000);

                WebElement memberDOB = driver.findElement(By.xpath("//input[@name='form_field_data.txt_date_of_birth']"));
                memberDOB.clear();
                memberDOB.sendKeys(rowData[7]);

                memberCode.click();
                storeMemberCode = memberCode.getAttribute("value");
                childTest.log(Status.INFO, "Enter member birth date: " + rowData[7]);

                WebElement memberVillage = driver.findElement(By.xpath("//select[@name='form_field_data.txt_village_ward']"));
                Select vil = new Select(memberVillage);
                vil.selectByIndex(0);
                childTest.log(Status.INFO, "Enter member village: " + memberVillage.getText());

                WebElement postOffice = driver.findElement(By.xpath("//select[@name='form_field_data.post_office']"));
                Select post = new Select(postOffice);
                post.selectByIndex(0);
                childTest.log(Status.INFO, "Enter member post office: " + postOffice.getText());

                WebElement gender = driver.findElement(By.xpath("//select[@name='form_field_data.cbo_gender']"));
                Select gen = new Select(gender);
                int totalGenderContains = gen.getOptions().size();
                if (totalGenderContains == 1) {
                    gen.selectByIndex(0);
                } else {
                    if (rowData[11].trim().equalsIgnoreCase("F") || rowData[11].trim().equalsIgnoreCase("Female")) {
                        gen.selectByVisibleText("Female");
                    } else if (rowData[11].trim().equalsIgnoreCase("M") || rowData[11].trim().equalsIgnoreCase("Male")) {
                        gen.selectByVisibleText("Male");
                    }
                }
                childTest.log(Status.INFO, "Enter member gender: " + rowData[11]);

                WebElement fatherName = driver.findElement(By.id("txt_father_name"));
                fatherName.clear();
                fatherName.sendKeys(rowData[12]);
                childTest.log(Status.INFO, "Enter member father name: " + rowData[12]);

                WebElement motherName = driver.findElement(By.id("txt_mother_name"));
                motherName.clear();
                motherName.sendKeys(rowData[13]);
                childTest.log(Status.INFO, "Enter member mother name: " + rowData[13]);

                WebElement maritalStatus = driver.findElement(By.xpath("//select[@name='form_field_data.txt_marital_status']"));
                Select mStatus = new Select(maritalStatus);
                if (rowData[14].equalsIgnoreCase("M") || rowData[14].equalsIgnoreCase("Married")) {
                    mStatus.selectByValue("M");
                } else if (rowData[14].equalsIgnoreCase("S") || rowData[14].equalsIgnoreCase("Unmarried") || rowData[14].equalsIgnoreCase("U")) {
                    mStatus.selectByValue("S");
                } else if (rowData[14].equalsIgnoreCase("Widower") || rowData[14].equalsIgnoreCase("Wr")) {
                    mStatus.selectByValue("Wr");
                } else if (rowData[14].equalsIgnoreCase("Widow") || rowData[14].equalsIgnoreCase("W")) {
                    mStatus.selectByValue("W");
                } else
                    mStatus.selectByValue("D");
                childTest.log(Status.INFO, "Enter member marital status: " + rowData[14]);
                Thread.sleep(1000);

                if (Objects.equals(mStatus.getFirstSelectedOption().getText(), "Married") || Objects.equals(mStatus.getFirstSelectedOption().getText(), "Widower") || Objects.equals(mStatus.getFirstSelectedOption().getText(), "Widow")) {
                    WebElement spouseName = driver.findElement(By.id("txt_spouse_name"));
                    spouseName.clear();
                    spouseName.sendKeys(rowData[15]);
                    childTest.log(Status.INFO, "Enter spouse name of the member: " + rowData[15]);
                }

                if (!rowData[16].isEmpty() && !rowData[16].equals("0")) {
                    WebElement education = driver.findElement(By.xpath("//select[@name='form_field_data.educational_qualification']"));
                    Select edu = new Select(education);
                    List<WebElement> allEduLevel = edu.getOptions();
                    for (WebElement eduLevel : allEduLevel) {
                        if (eduLevel.getText().contains(rowData[16])) {
                            eduLevel.click();
                            childTest.log(Status.INFO, "Enter member education: " + rowData[16]);
                            break;
                        }
                    }
                }

                if (!rowData[17].isEmpty() && !rowData[17].equals("0")) {
                    WebElement NID = driver.findElement(By.id("txt_national_id"));
                    NID.clear();
                    NID.sendKeys(rowData[17]);
                    childTest.log(Status.INFO, "Enter member national ID Number: " + rowData[17]);
                }

                if (!rowData[18].isEmpty() && !rowData[18].equals("0")) {
                    WebElement smartID = driver.findElement(By.id("txt_smart_id"));
                    smartID.clear();
                    smartID.sendKeys(rowData[18]);
                    childTest.log(Status.INFO, "Enter member smart ID Number: " + rowData[18]);
                }

                if (!rowData[19].isEmpty() && !rowData[19].equals("0")) {
                    WebElement birthID = driver.findElement(By.id("txt_birth_registration_no"));
                    birthID.clear();
                    birthID.sendKeys(rowData[19]);
                    childTest.log(Status.INFO, "Enter member birth registration number: " + rowData[19]);
                }

                WebElement otherCard = driver.findElement(By.xpath("//select[@name='form_field_data.cbo_card_type']"));
                if (!rowData[20].isEmpty() && !rowData[20].equals("0")) {
                    Select card = new Select(otherCard);
                    if (Objects.equals(rowData[20], "Pass") || Objects.equals(rowData[20], "Passport") || Objects.equals(rowData[20], "P")) {
                        card.selectByIndex(1);
                    } else
                        card.selectByIndex(2);
                    childTest.log(Status.INFO, "Select member other card: " + rowData[20]);
                }

                if (Objects.equals(otherCard.getText(), "Passport") || Objects.equals(otherCard.getText(), "Driving License")) {
                    WebElement otherCardNo = driver.findElement(By.id("txt_other_id"));
                    otherCardNo.clear();
                    otherCardNo.sendKeys(rowData[21]);
                    childTest.log(Status.INFO, "Enter member other card number: " + rowData[21]);

                    WebElement cardIssuingCountry = driver.findElement(By.xpath("//select[@name='form_field_data.cbo_card_country']"));
                    Select issueCountry = new Select(cardIssuingCountry);
                    issueCountry.selectByVisibleText(rowData[22]);
                    childTest.log(Status.INFO, "Select member other card issuing country: " + rowData[22]);

                    WebElement cardExpiryDate = driver.findElement(By.xpath("//select[@name='form_field_data.cbo_card_country']"));
                    cardExpiryDate.clear();
                    cardExpiryDate.sendKeys(rowData[23]);
                    childTest.log(Status.INFO, "Select member other card expiry date: " + rowData[23]);

                }

                if (!rowData[24].isEmpty() && !rowData[24].equals("0")) {
                    WebElement formNumber = driver.findElement(By.id("txt_admission_no"));
                    formNumber.clear();
                    formNumber.sendKeys(rowData[24]);
                    childTest.log(Status.INFO, "Enter member application form number: " + rowData[24]);
                }

                if (!rowData[25].isEmpty() && !rowData[25].equals("0")) {
                    WebElement memberType = driver.findElement(By.xpath("//select[@name='form_field_data.cbo_member_type']"));
                    Select typeOfMember = new Select(memberType);
                    typeOfMember.selectByVisibleText(rowData[25]);
                    childTest.log(Status.INFO, "Select member type: " + rowData[25]);
                }

                if (!rowData[25].isEmpty() && !rowData[25].equals("0") && (rowData[25].equalsIgnoreCase("I") || rowData[25].equalsIgnoreCase("Inactive"))) {
                    WebElement memberStatus = driver.findElement(By.xpath("//select[@name='form_field_data.cbo_member_status']"));
                    Select status = new Select(memberStatus);
                    status.selectByVisibleText(rowData[26]);
                    childTest.log(Status.INFO, "Select member status: " + rowData[26]);
                }

                WebElement mobileNo = driver.findElement(By.id("txt_mobile_no"));
                mobileNo.clear();
                if (!rowData[27].startsWith("0")) {
                    mobileNo.sendKeys("0" + rowData[27]);
                } else {
                    mobileNo.sendKeys(rowData[27]);
                }
                childTest.log(Status.INFO, "Enter member mobile number: " + rowData[27]);

                if (!rowData[28].isEmpty() && !rowData[28].equals("0")) {
                    WebElement landArea = driver.findElement(By.id("txt_land_area"));
                    landArea.clear();
                    landArea.sendKeys(rowData[28]);
                    childTest.log(Status.INFO, "Enter member total area in Acre: " + rowData[28]);
                }

                if (!rowData[29].isEmpty() && !rowData[29].equals("0")) {
                    WebElement familyContactNo = driver.findElement(By.id("txt_family_contact_no"));
                    familyContactNo.clear();
                    familyContactNo.sendKeys(rowData[29]);
                    childTest.log(Status.INFO, "Enter member family contact number: " + rowData[29]);
                }

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