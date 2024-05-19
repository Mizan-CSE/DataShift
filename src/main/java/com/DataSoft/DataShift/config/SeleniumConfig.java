package com.DataSoft.DataShift.config;

import com.DataSoft.DataShift.models.AutomationRequest;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.stereotype.Component;

@Component
public class SeleniumConfig {

    public String login(WebDriver driver, AutomationRequest request) throws InterruptedException {
        if (driver == null) {
            return "Driver is null";
        }
        driver.manage().window().maximize();
        driver.get(request.getUrl());
        Thread.sleep(2000);

        WebElement usernameField = driver.findElement(By.xpath("//input[@placeholder='Username']"));
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameField.clear();
        usernameField.sendKeys(request.getUsername());

        passwordField.clear();
        passwordField.sendKeys(request.getPassword());

        loginButton.click();
        Thread.sleep(5000);
        return "Login successful";
    }

    public WebDriver getDriver(String browser) {
        if ("chrome".equalsIgnoreCase(browser)) {
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver();
        } else if ("firefox".equalsIgnoreCase(browser)) {
            WebDriverManager.firefoxdriver().setup();
            return new FirefoxDriver();
        } else if ("edge".equalsIgnoreCase(browser)) {
            WebDriverManager.edgedriver().setup();
            return new EdgeDriver();
        }
        return null;
    }

    public void memberMigrationPage(WebDriver driver) throws InterruptedException {
        driver.findElement(By.xpath("//input[@class='form-control search_box']")).click();
        driver.findElement(By.xpath("//h5[text()='Config']")).click();
        driver.findElement(By.xpath("//i[@id='main_menu' and @class='fa fa-exchange']")).click();
        driver.findElement(By.xpath("//h5[text()='Member Migration']")).click();
        Thread.sleep(3000);
    }
    public void loansSavingsMigrationPage(WebDriver driver) throws InterruptedException {
        driver.findElement(By.xpath("//input[@class='form-control search_box']")).click();
        driver.findElement(By.xpath("//h5[text()='Config']")).click();
        driver.findElement(By.xpath("//i[@id='main_menu' and @class='fa fa-exchange']")).click();
        driver.findElement(By.xpath("//h5[text()='Loan & Saving Migration']")).click();
        Thread.sleep(3000);
    }

}
