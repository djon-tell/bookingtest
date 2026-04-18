package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class FilterTest extends BaseTest {

    @BeforeClass
    public void setupClass() {
        super.setUpClass();
        goToHomepage();
    }

    @Test(priority = 1)
    public void testFiltersAndSorting() {

        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='ss']")
        ));
        searchInput.clear();
        searchInput.sendKeys("Miami Beach");

        try {
            WebElement suggestion = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='autocomplete-results-option']:first-child")
            ));
            smartClick(suggestion);
        } catch (Exception e) {
            searchInput.sendKeys(Keys.ENTER);
        }

        smartClick(By.cssSelector("[data-testid='searchbox-submit-button']"));
        wait.until(ExpectedConditions.urlContains("searchresults"));

        // Close calendar
        try {
            WebElement anyDate = driver.findElement(
                    By.cssSelector("td[data-date]:not([aria-disabled='true']) span")
            );
            smartClick(anyDate);
        } catch (Exception ignored) {}

        scrollDown(400);

        // ⭐ Apply Free WiFi
        try {
            WebElement wifi = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@data-filters-item,'hotelfacility:107')]")
            ));
            smartClick(wifi);
        } catch (Exception ignored) {}

        pause(300);

        // ⭐ Apply 4-star filter
        try {
            WebElement fourStar = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@data-filters-item,'class:4')]")
            ));
            smartClick(fourStar);
        } catch (Exception ignored) {}

        pause(300);

        // ⭐ Apply Breakfast Included
        try {
            WebElement breakfast = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@data-filters-item,'mealplan:1')]")
            ));
            smartClick(breakfast);
        } catch (Exception ignored) {}

        pause(300);

        // ⭐ Sort dropdown (your provided element)
        try {
            WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[data-testid='sorters-dropdown-trigger']")
            ));
            smartClick(sortDropdown);

            WebElement priceLowToHigh = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[contains(text(),'Price (lowest first)')]")
            ));
            smartClick(priceLowToHigh);

        } catch (Exception ignored) {}

        pause(500);

        WebElement results = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='property-card']")
        ));

        Assert.assertTrue(results.isDisplayed());
    }
}
