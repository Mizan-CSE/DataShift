package com.DataSoft.DataShift.services;

import com.DataSoft.DataShift.config.SeleniumConfig;
import com.DataSoft.DataShift.models.AutomationRequest;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkingArea {
    @Autowired
    SeleniumConfig config;

    public String areaMigration(AutomationRequest request) {
        WebDriver driver = config.getDriver(request.getBrowser()); // Obtain WebDriver instance
        if (driver == null) {
            return "Unsupported browser: " + request.getBrowser();
        }

        try {
            config.login(driver, request); // Use WebDriver instance
            return "Working area migration successful";
        } catch (Exception e) {
            return "Working area migration failed: " + e.getMessage();
        } finally {
            driver.quit(); // Close WebDriver instance
        }
    }
}
