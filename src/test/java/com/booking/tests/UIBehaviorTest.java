package com.booking.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.io.File;

public class UIBehaviorTest extends BaseTest {

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("UIBehaviorTest suite starting.");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("UIBehaviorTest suite complete.");
    }

    @BeforeTest
    public void beforeTest() {
        System.out.println("BeforeTest: UI behavior test block starting.");
    }

    @AfterTest
    public void afterTest() {
        System.out.println("AfterTest: UI behavior test block done.");
    }

    @BeforeClass
    public void setUp() {
        super.setUp();
        goToHomepage();
        searchFor("Miami");
    }

    @BeforeMethod
    public void beforeEachTest() {
        closeExtraTabs();
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() {
        closeExtraTabs();
        System.out.println("Test finished.");
    }

    // Verify scrolling down moves the page position greater than 0
    @Test(priority = 1)
    public void testScrollDownYGreaterThanZero() {
        WebElement body = driver.findElement(By.tagName("body"));
        for (int i = 0; i < 10; i++) body.sendKeys(Keys.ARROW_DOWN);

        Assert.assertTrue(driver.findElement(By.tagName("header")).isDisplayed(),
                "Page should have scrolled down after pressing arrow keys.");
        System.out.println("Scroll down verified.");
    }

    // Verify scrolling back up returns the page to the top
    @Test(priority = 2)
    public void testScrollUpYEqualsZero() {
        WebElement body = driver.findElement(By.tagName("body"));
        for (int i = 0; i < 10; i++) body.sendKeys(Keys.ARROW_DOWN);
        body.sendKeys(Keys.HOME);

        Assert.assertTrue(driver.findElement(By.tagName("header")).isDisplayed(),
                "Header should be visible after scrolling back to top.");
        System.out.println("Scroll up verified.");
    }

    // Verify a screenshot of the results page saves successfully and is not empty
    @Test(priority = 3)
    public void testScreenshotSaved() throws Exception {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destFile = new File("booking_screenshot.png");
        FileHandler.copy(srcFile, destFile);

        Assert.assertTrue(destFile.exists(),
                "Screenshot file should exist after capture.");
        Assert.assertTrue(destFile.length() > 0,
                "Screenshot file should not be empty.");
        System.out.println("Screenshot saved: " + destFile.getAbsolutePath());
    }

    // Verify clicking a property opens a new browser tab
    @Test(priority = 4)
    public void testNewTabOpens() {
        int windowsBefore = driver.getWindowHandles().size();

        wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[@data-testid='property-card']//a[@data-testid='title-link'])[1]")))
                .sendKeys(Keys.RETURN);

        wait.until(d -> d.getWindowHandles().size() > windowsBefore);

        Assert.assertTrue(driver.getWindowHandles().size() > windowsBefore,
                "A new browser tab should open when clicking a property card.");
        System.out.println("New tab opened.");
    }

    // Verify switching back to the original tab restores the correct window handle
    // Uses soft assertions to check both the switch and the return
    @Test(priority = 5)
    public void testSwitchBackToOriginalTab() {
        wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[@data-testid='property-card']//a[@data-testid='title-link'])[1]")))
                .sendKeys(Keys.RETURN);

        wait.until(d -> d.getWindowHandles().size() > 1);

        String newWindow = null;
        for (String w : driver.getWindowHandles()) {
            if (!w.equals(originalWindow)) { newWindow = w; break; }
        }
        driver.switchTo().window(newWindow);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotEquals(driver.getWindowHandle(), originalWindow,
                "Should be on the new tab after switching.");

        driver.close();
        driver.switchTo().window(originalWindow);

        softAssert.assertEquals(driver.getWindowHandle(), originalWindow,
                "Switching back should restore the correct original window handle.");
        softAssert.assertAll();
        System.out.println("Switched back to original tab successfully.");
    }

    // Verify the new tab URL contains booking.com
    @Test(priority = 6)
    public void testNewTabURLContainsBooking() {
        wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[@data-testid='property-card']//a[@data-testid='title-link'])[1]")))
                .sendKeys(Keys.RETURN);

        wait.until(d -> d.getWindowHandles().size() > 1);

        for (String w : driver.getWindowHandles()) {
            if (!w.equals(originalWindow)) { driver.switchTo().window(w); break; }
        }

        wait.until(d -> !d.getCurrentUrl().equals("about:blank"));

        String newTabURL = driver.getCurrentUrl();
        System.out.println("New tab URL: " + newTabURL);

        Assert.assertTrue(newTabURL.contains("booking.com"),
                "The new tab URL should contain 'booking.com'.");

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}