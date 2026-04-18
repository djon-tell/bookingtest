package com.booking.base;

import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // ───────────────────────────────────────────────────────────────
    // SETUP + TEARDOWN
    // ───────────────────────────────────────────────────────────────
    @BeforeClass
    public void setUpClass() {

        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--remote-allow-origins=*");

        driver = new EdgeDriver(options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(6));
        wait.pollingEvery(Duration.ofMillis(120));

        driver.get("https://www.booking.com/");
        dismissAllPopups();
    }

    @AfterClass
    public void tearDownClass() {
        if (driver != null) driver.quit();
    }

    // ───────────────────────────────────────────────────────────────
    // NAVIGATION
    // ───────────────────────────────────────────────────────────────
    protected void goToHomepage() {
        driver.get("https://www.booking.com/");
        dismissAllPopups();
    }

    // ───────────────────────────────────────────────────────────────
    // POPUP HANDLING
    // ───────────────────────────────────────────────────────────────
    protected void dismissAllPopups() {
        try {
            WebElement cookieBtn = driver.findElement(By.cssSelector("button[aria-label='Dismiss']"));
            cookieBtn.click();
        } catch (Exception ignored) {}

        try {
            WebElement closeBtn = driver.findElement(By.cssSelector("button[aria-label*='close']"));
            closeBtn.click();
        } catch (Exception ignored) {}
    }

    // ───────────────────────────────────────────────────────────────
    // CALENDAR HANDLING
    // ───────────────────────────────────────────────────────────────
    protected void closeCalendarIfVisible() {
        try {
            WebElement anyDate = driver.findElement(
                    By.cssSelector("td[data-date]:not([aria-disabled='true']) span, span[data-date]")
            );
            smartClick(anyDate);
            pause(200);
        } catch (Exception ignored) {}
    }

    // ───────────────────────────────────────────────────────────────
    // CLICK HELPERS
    // ───────────────────────────────────────────────────────────────
    protected void smartClick(By locator) {
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            scrollIntoView(el);
            el.click();
        } catch (Exception e) {
            WebElement el = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    protected void smartClick(WebElement el) {
        try {
            scrollIntoView(el);
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    // ───────────────────────────────────────────────────────────────
    // SCROLL HELPERS
    // ───────────────────────────────────────────────────────────────
    protected void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior:'smooth', block:'center'});", el);
        } catch (Exception ignored) {}
    }

    protected void scrollDown(int px) {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollBy({top:" + px + ", behavior:'smooth'});");
    }

    protected void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({top:0, behavior:'smooth'});");
    }

    // ───────────────────────────────────────────────────────────────
    // PAUSE
    // ───────────────────────────────────────────────────────────────
    protected void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
