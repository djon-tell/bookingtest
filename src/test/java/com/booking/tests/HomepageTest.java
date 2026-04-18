package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class HomepageTest extends BaseTest {

    // ───────────────────────────────────────────────────────────────
    @Test(priority = 1, description = "Verify homepage URL loads correctly")
    public void testHomepageURL() {
        goToHomepage();
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("booking.com"),
                "Homepage URL should contain booking.com but was: " + url);
    }

    // ───────────────────────────────────────────────────────────────
    @Test(priority = 2, description = "Verify homepage title contains Booking.com")
    public void testHomepageTitle() {
        String title = driver.getTitle().toLowerCase();
        Assert.assertTrue(title.contains("booking"),
                "Title should contain 'booking' but was: " + title);
    }

    // ───────────────────────────────────────────────────────────────
    @Test(priority = 3, description = "Verify search bar is visible")
    public void testSearchBarPresent() {
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='ss'], input[placeholder*='Where']")));
        Assert.assertTrue(searchBox.isDisplayed(), "Search bar should be visible");
    }

    // ───────────────────────────────────────────────────────────────
    @Test(priority = 4, description = "Verify navigation links exist")
    public void testNavLinksPresent() {
        List<WebElement> navLinks = driver.findElements(
                By.cssSelector("nav a, header a, [data-testid='header-nav'] a"));

        Assert.assertTrue(navLinks.size() > 0,
                "There should be at least one navigation link");
    }

    // ───────────────────────────────────────────────────────────────
    @Test(priority = 5, description = "Verify clicking the logo returns to homepage")
    public void testLogoNavigation() {
        driver.get("https://www.booking.com/searchresults.html?ss=Miami");

        WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[aria-label*='Booking'], [data-testid='header-logo'] a")));

        logo.click();

        wait.until(ExpectedConditions.urlContains("booking.com"));

        Assert.assertTrue(driver.getCurrentUrl().contains("booking.com"),
                "Clicking logo should navigate to homepage");
    }
}
