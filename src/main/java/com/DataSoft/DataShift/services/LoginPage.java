package com.DataSoft.DataShift.services;

import com.DataSoft.DataShift.models.AutomationRequest;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.stereotype.Service;

@Service
public class LoginPage {
    public String runAutomation(AutomationRequest request) {
        WebDriver driver = getDriver(request.getBrowser());
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            driver.manage().window().maximize();
            driver.get(request.getMfi());
            Thread.sleep(5000);

            WebElement usernameField = driver.findElement(By.xpath("//input[@placeholder='Username']"));
            usernameField.clear();
            usernameField.sendKeys(request.getUsername());

            WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Password']"));
            passwordField.clear();
            passwordField.sendKeys(request.getPassword());

            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

            loginButton.click();
            Thread.sleep(5000);
            return "Login successful";
        } catch (Exception e) {
            return "Login failed: " + e.getMessage();
        } finally {
            driver.quit();
        }
    }

    private WebDriver getDriver(String browser) {
        if ("chrome".equalsIgnoreCase(browser)) {
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver();
        } else if ("firefox".equalsIgnoreCase(browser)) {
            WebDriverManager.firefoxdriver().setup();
            return new FirefoxDriver();
        }
        return null;
    }
}