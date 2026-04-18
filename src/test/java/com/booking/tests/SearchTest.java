package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class SearchTest extends BaseTest {

    @BeforeClass
    public void setupClass() {
        super.setUpClass();
        goToHomepage();
    }

    @Test(priority = 1)
    public void testSearchAndMapInteraction() {

        // ⭐ Enter destination
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='ss']")
        ));

        searchInput.clear();
        searchInput.sendKeys("Miami Beach");

        // ⭐ Select first suggestion if available
        try {
            WebElement suggestion = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='autocomplete-results-option']:first-child")
            ));
            smartClick(suggestion);
        } catch (Exception e) {
            searchInput.sendKeys(Keys.ENTER);
        }

        // ⭐ UNIVERSAL SEARCH BUTTON LOCATORS (Edge + Chrome + Firefox)
        By[] searchButtons = new By[]{
                By.cssSelector("[data-testid='searchbox-submit-button']"),   // new layout
                By.cssSelector("button[type='submit']"),                     // fallback
                By.cssSelector("button.sb-searchbox__button"),               // older layout
                By.cssSelector("button[data-sb-id='main']"),                 // legacy
                By.xpath("//button[contains(.,'Search')]")                   // text fallback
        };

        boolean clicked = false;

        for (By locator : searchButtons) {
            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(locator));
                smartClick(btn);
                clicked = true;
                break;
            } catch (Exception ignored) {}
        }

        // ⭐ LAST RESORT — press ENTER
        if (!clicked) {
            searchInput.sendKeys(Keys.ENTER);
        }

        // ⭐ Wait for results page
        wait.until(ExpectedConditions.urlContains("searchresults"));

        // ⭐ Close calendar if it appears
        try {
            WebElement anyDate = driver.findElement(
                    By.cssSelector("td[data-date]:not([aria-disabled='true']) span")
            );
            smartClick(anyDate);
        } catch (Exception ignored) {}

        scrollDown(400);

        // ⭐ Open map
        WebElement mapToggle = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='map-toggle']")
        ));
        smartClick(mapToggle);

        pause(800);

        // ⭐ Move map around
        WebElement mapCanvas = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("canvas")
        ));

        Actions act = new Actions(driver);
        act.clickAndHold(mapCanvas).moveByOffset(200, 0).release().perform();
        pause(300);
        act.clickAndHold(mapCanvas).moveByOffset(-200, 0).release().perform();
        pause(300);

        // ⭐ Return to list view
        smartClick(mapToggle);

        // ⭐ Verify results exist
        WebElement results = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='property-card']")
        ));

        Assert.assertTrue(results.isDisplayed(), "Results were not displayed after returning from map view");
    }
}
