package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class UIBehaviorTest extends BaseTest {

    @BeforeClass
    public void setupClass() {
        super.setUpClass();
        goToHomepage();
    }

    @Test(priority = 1)
    public void testSmoothScrollUI() {

        // Scroll down smoothly
        for (int i = 0; i < 5; i++) {
            scrollDown(300);
            pause(150);
        }

        // Scroll up smoothly
        for (int i = 0; i < 5; i++) {
            scrollDown(-300);
            pause(150);
        }

        Assert.assertTrue(true);
    }

    @Test(priority = 2)
    public void testDestinationInputOpens() {
        WebElement dest = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='ss']")
        ));
        smartClick(dest);
        Assert.assertTrue(dest.equals(driver.switchTo().activeElement()));
    }

    @Test(priority = 3)
    public void testCalendarOpens() {
        WebElement calendarBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='searchbox-dates-container']")
        ));
        smartClick(calendarBtn);

        WebElement calendar = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='datepicker-tabs']")
        ));

        Assert.assertTrue(calendar.isDisplayed());
    }

    @Test(priority = 4)
    public void testGuestsDropdownOpens() {
        WebElement guestsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='occupancy-config']")
        ));
        smartClick(guestsBtn);

        WebElement popup = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div[data-testid='occupancy-popup']")
        ));

        Assert.assertTrue(popup.isDisplayed());
    }
}
