package com.booking.base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CommonPopupsHandler {

    private WebDriver driver;
    private WebDriverWait wait;

    public CommonPopupsHandler(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void dismissAll() {
        dismissCookies();
        dismissSignInPopup();
        dismissLanguagePopup();
    }

    private void dismissCookies() {
        try {
            WebElement cookieBtn = driver.findElement(By.id("onetrust-accept-btn-handler"));
            cookieBtn.click();
        } catch (Exception ignored) {}
    }

    private void dismissSignInPopup() {
        try {
            WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@aria-label='Dismiss sign-in info.']")
            ));
            closeBtn.click();
        } catch (Exception ignored) {}
    }

    private void dismissLanguagePopup() {
        try {
            WebElement closeBtn = driver.findElement(By.cssSelector("[aria-label='Dismiss']"));
            closeBtn.click();
        } catch (Exception ignored) {}
    }
}
