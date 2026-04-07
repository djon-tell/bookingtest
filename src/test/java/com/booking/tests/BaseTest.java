package com.booking.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.time.Duration;

public class BaseTest {

    public WebDriver driver;
    public WebDriverWait wait;
    public String originalWindow;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("Browser launched.");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
        System.out.println("Browser closed.");
    }

    // Dismisses cookie popup if present
    public void dismissCookiePopup() {
        try {
            driver.findElement(By.id("onetrust-accept-btn-handler")).click();
        } catch (NoSuchElementException e) {
            System.out.println("No cookie popup.");
        }
    }

    // Dismisses sign-in overlay if present
    public void dismissSignInOverlay() {
        try {
            driver.findElement(
                    By.xpath("//button[@aria-label='Dismiss sign-in info.']")).click();
        } catch (NoSuchElementException e) {
            System.out.println("No sign-in overlay.");
        }
    }

    // Dismisses any popup on the results page
    public void dismissResultsPopup() {
        try {
            driver.findElement(
                    By.xpath("//button[@aria-label='Dismiss sign-in info.']")).click();
        } catch (NoSuchElementException e) {
            System.out.println("No results popup.");
        }
    }

    // Closes the date picker by pressing Escape twice on the body
    public void closeDatePicker() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    // Closes any extra tabs that opened during a test
    public void closeExtraTabs() {
        try {
            for (String w : driver.getWindowHandles()) {
                if (!w.equals(originalWindow)) {
                    driver.switchTo().window(w);
                    driver.close();
                }
            }
            driver.switchTo().window(originalWindow);
        } catch (Exception e) {
            System.out.println("Tab cleanup: " + e.getMessage());
        }
    }

    // Navigates to homepage and handles all popups
    public void goToHomepage() {
        driver.get("https://www.booking.com");
        originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@name='ss' or @placeholder='Where are you going?']")));
        dismissCookiePopup();
        dismissSignInOverlay();
    }

    // Types destination, selects autocomplete, closes date picker, clicks search
    // and waits for results — shared by any class that needs to search
    public void searchFor(String destination) {
        driver.findElement(
                        By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                .sendKeys(destination);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//div[@data-testid='autocomplete-result'])[1]"))).click();
        } catch (Exception e) {
            driver.findElement(
                            By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                    .sendKeys(Keys.ENTER);
        }

        closeDatePicker();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-testid='searchbox-submit-button']"))).click();
        } catch (Exception e) {
            driver.findElement(
                            By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                    .sendKeys(Keys.ENTER);
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@data-testid='property-card']")));

        dismissResultsPopup();
        System.out.println("Search results loaded.");
    }

    // Clicks the first property from search results and switches to its tab
    public void openFirstProperty() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);

        wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[@data-testid='property-card']//a[@data-testid='title-link'])[1]")))
                .sendKeys(Keys.RETURN);

        wait.until(d -> d.getWindowHandles().size() > 1);

        for (String w : driver.getWindowHandles()) {
            if (!w.equals(originalWindow)) {
                driver.switchTo().window(w);
                break;
            }
        }

        wait.until(d -> !d.getCurrentUrl().contains("searchresults"));
        System.out.println("Navigated to: " + driver.getCurrentUrl());
    }
}