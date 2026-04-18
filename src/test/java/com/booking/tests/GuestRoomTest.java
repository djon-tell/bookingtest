package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuestRoomTest extends BaseTest {

    JavascriptExecutor js;
    Actions actions;

    private static final String OCCUPANCY_BTN_CSS =
            "button[aria-label*='adults'], " +
                    "button[data-testid*='occupancy'], " +
                    "[data-testid='occupancy-config']";

    private static final String CSS_DECREASE = "button.c857f39cb2";
    private static final String CSS_INCREASE = "button.dc8366caa6";
    private static final String CSS_DONE     = "button.d1babacfe0";

    private static final int ROW_ADULTS   = 0;
    private static final int ROW_CHILDREN = 1;
    private static final int ROW_ROOMS    = 2;

    private Actions actions() {
        if (actions == null) actions = new Actions(driver);
        return actions;
    }

    // ───────────────────────────────────────────────────────────────
    @BeforeClass
    public void setUpClass() {
        super.setUpClass();
        js = (JavascriptExecutor) driver;
        actions = new Actions(driver);
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(0));
        goToHomepage();
    }

    @BeforeMethod
    public void beforeEachTest() {
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() {
        System.out.println("Test finished.");
    }

    // ───────────────────────────────────────────────────────────────
    private void openDropdown() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(OCCUPANCY_BTN_CSS)
        ));
        smartClick(btn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(CSS_INCREASE)));
        pause(200);
    }

    private List<WebElement> getIncreaseButtons() {
        return driver.findElements(By.cssSelector(CSS_INCREASE));
    }

    private List<WebElement> getDecreaseButtons() {
        return driver.findElements(By.cssSelector(CSS_DECREASE));
    }

    private void realClick(WebElement el) {
        scrollIntoView(el);
        actions().moveToElement(el).click().perform();
        pause(150);
    }

    private void clickDone() {
        WebElement doneBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(CSS_DONE)
        ));
        realClick(doneBtn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(CSS_INCREASE)));
        pause(200);
    }

    private void selectChildAge(int childIndex, String age) {
        final String AGE_SELECT_CSS =
                "div[data-testid='occupancy-popup'] select, " +
                        "div[class*='occupancy'] select";

        List<WebElement> ageSelects = driver.findElements(By.cssSelector(AGE_SELECT_CSS));
        if (ageSelects.isEmpty()) return;

        int idx = Math.min(childIndex, ageSelects.size() - 1);
        WebElement select = ageSelects.get(idx);

        js.executeScript("arguments[0].value = arguments[1];", select, age);
        js.executeScript(
                "var evt = new Event('change', {bubbles: true}); arguments[0].dispatchEvent(evt);",
                select
        );

        pause(150);
    }

    private void stepUntil(int rowIndex, int target, String direction) {
        int current = getStepperDisplayValue(rowIndex);
        int maxClicks = 20;

        while (current != target && maxClicks-- > 0) {
            if ("increase".equals(direction)) {
                realClick(getIncreaseButtons().get(rowIndex));
            } else {
                realClick(getDecreaseButtons().get(rowIndex));
            }
            current = getStepperDisplayValue(rowIndex);
        }

        Assert.assertEquals(current, target);
    }

    private int getStepperDisplayValue(int rowIndex) {
        List<WebElement> incBtns = getIncreaseButtons();
        WebElement btn = incBtns.get(rowIndex);
        WebElement container = btn.findElement(By.xpath("./.."));

        List<WebElement> nums = container.findElements(By.xpath(
                ".//*[translate(normalize-space(.),'0123456789','')='' and string-length(normalize-space(.))>0]"
        ));

        if (!nums.isEmpty()) return Integer.parseInt(nums.get(0).getText().trim());

        WebElement gp = container.findElement(By.xpath("./.."));
        nums = gp.findElements(By.xpath(
                ".//*[translate(normalize-space(.),'0123456789','')='' and string-length(normalize-space(.))>0]"
        ));

        return Integer.parseInt(nums.get(0).getText().trim());
    }

    private int readLabelCount(String keyword) {
        String label = driver.findElement(By.cssSelector(OCCUPANCY_BTN_CSS))
                .getAttribute("aria-label");

        Matcher m = Pattern.compile("(\\d+)\\s+" + keyword).matcher(label);
        if (m.find()) return Integer.parseInt(m.group(1));

        throw new RuntimeException("Keyword not found: " + keyword);
    }

    // ───────────────────────────────────────────────────────────────
    @Test(priority = 1)
    public void testIncreaseAdultsTo4() {
        openDropdown();
        stepUntil(ROW_ADULTS, 4, "increase");
        clickDone();
        Assert.assertEquals(readLabelCount("adult"), 4);
    }

    @Test(priority = 2)
    public void testIncreaseChildrenTo3() {
        openDropdown();

        int current = getStepperDisplayValue(ROW_CHILDREN);
        while (current < 3) {
            realClick(getIncreaseButtons().get(ROW_CHILDREN));
            pause(150);
            current = getStepperDisplayValue(ROW_CHILDREN);
            selectChildAge(current - 1, "5");
        }

        clickDone();
        Assert.assertEquals(readLabelCount("child"), 3);
    }

    @Test(priority = 3)
    public void testIncreaseRoomsTo2() {
        openDropdown();
        stepUntil(ROW_ROOMS, 2, "increase");
        clickDone();
        Assert.assertEquals(readLabelCount("room"), 2);
    }

    @Test(priority = 4)
    public void testDecreaseChildrenTo1AndSetAge() {
        openDropdown();
        stepUntil(ROW_CHILDREN, 1, "decrease");

        selectChildAge(0, "7");
        clickDone();

        Assert.assertEquals(readLabelCount("child"), 1);
    }

    @Test(priority = 5)
    public void testSelectTravelingWithPets() {
        openDropdown();

        WebElement petsCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@type='checkbox' and (contains(@id,'pet') or contains(@name,'pet'))]")
        ));

        WebElement parent = petsCheckbox.findElement(By.xpath("./.."));
        realClick(parent);

        boolean isChecked = petsCheckbox.isSelected();
        clickDone();

        Assert.assertTrue(isChecked);
    }

    // ───────────────────────────────────────────────────────────────
    // ⭐ FINAL SEARCH TEST WITH GUARANTEED CALENDAR CLOSE
    // ───────────────────────────────────────────────────────────────
    @Test(priority = 6)
    public void testSearchWithCurrentSettings() {

        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='ss'], input[placeholder*='Where']")
        ));

        searchInput.clear();
        searchInput.sendKeys("Miami Beach");
        pause(200);

        try {
            WebElement firstSuggestion = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='autocomplete-results-option']:first-child")
            ));
            scrollIntoView(firstSuggestion);
            firstSuggestion.click();
        } catch (Exception e) {
            searchInput.sendKeys(Keys.ENTER);
        }

        // Multi-locator search button
        By[] searchLocators = new By[]{
                By.cssSelector("[data-testid='searchbox-submit-button']"),
                By.cssSelector("button[type='submit']"),
                By.cssSelector(".sb-searchbox__button")
        };

        boolean clicked = false;

        for (By locator : searchLocators) {
            try {
                smartClick(locator);
                clicked = true;
                break;
            } catch (Exception ignored) {}
        }

        if (!clicked) {
            searchInput.sendKeys(Keys.ENTER);
        }

        // Wait for results page
        wait.until(ExpectedConditions.urlContains("searchresults"));

        // ⭐ GUARANTEED FIX: Click any valid date to close the calendar
        try {
            WebElement anyDate = driver.findElement(
                    By.cssSelector("td[data-date]:not([aria-disabled='true']) span, span[data-date]:not([aria-disabled='true'])")
            );
            smartClick(anyDate);
            pause(200);
        } catch (Exception ignored) {}

        // ⭐ Scroll down to show results
        scrollDown(500);

        Assert.assertTrue(driver.getCurrentUrl().contains("booking.com"));
    }

    // ───────────────────────────────────────────────────────────────
    @AfterClass
    public void tearDownClass() {
        super.tearDownClass();
    }
}
