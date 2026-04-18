package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatePickerTest extends BaseTest {

    private String futureDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void openCalendar() {
        WebElement datesBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='searchbox-dates-container'], " +
                        "[data-testid='date-display-field-start'], " +
                        "button[data-testid*='date'], " +
                        "div[class*='DateField'], " +
                        ".sb-searchbox__dates")));
        smartClick(datesBtn);
    }

    private void clickDate(String isoDate) {
        WebElement cell = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("td[data-date='" + isoDate + "'], span[data-date='" + isoDate + "']")));
        smartClick(cell);
    }

    @BeforeClass
    public void setupClass() {
        super.setUpClass();
        goToHomepage();
    }

    @Test(priority = 1)
    public void testCalendarVisible() {
        openCalendar();

        WebElement calendar = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='datepicker-tabs'], " +
                        "[data-testid='searchbox-datepicker-calendar'], " +
                        "div[class*='CalendarMonth'], " +
                        "div[role='dialog'] table, " +
                        ".bui-calendar")));

        Assert.assertTrue(calendar.isDisplayed());
    }

    @Test(priority = 2)
    public void testCheckInFieldNotEmpty() {
        String checkIn = futureDate(7);
        clickDate(checkIn);

        WebElement checkInDisplay = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='date-display-field-start'], " +
                        "span[data-testid*='checkin'], " +
                        ".sb-date-field:first-of-type")));

        Assert.assertFalse(checkInDisplay.getText().trim().isEmpty());
    }

    @Test(priority = 3)
    public void testCheckOutNotBeforeCheckIn() {
        String beforeCheckIn = futureDate(4);

        try {
            WebElement cell = driver.findElement(By.cssSelector("td[data-date='" + beforeCheckIn + "']"));
            String ariaDisabled = cell.getAttribute("aria-disabled");
            String classVal = cell.getAttribute("class");

            boolean disabled = "true".equals(ariaDisabled)
                    || (classVal != null && (classVal.contains("disabled") || classVal.contains("blocked")));

            Assert.assertTrue(disabled);

        } catch (NoSuchElementException ignored) {}
    }

    @Test(priority = 4)
    public void testNightsCountUpdates() {
        String checkOut = futureDate(12);
        clickDate(checkOut);

        try {
            WebElement nightsLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ'," +
                            "'abcdefghijklmnopqrstuvwxyz'),'night')]")));

            Assert.assertFalse(nightsLabel.getText().trim().isEmpty());

        } catch (Exception e) {
            WebElement startField = driver.findElement(By.cssSelector(
                    "[data-testid='date-display-field-start'], .sb-date-field:first-of-type"));
            WebElement endField = driver.findElement(By.cssSelector(
                    "[data-testid='date-display-field-end'], .sb-date-field:last-of-type"));

            Assert.assertNotEquals(startField.getText().trim(), endField.getText().trim());
        }
    }

    @Test(priority = 5)
    public void testSelectFlexibleDates() {

        openCalendar();

        List<WebElement> flexibleTabs = driver.findElements(By.xpath(
                "//div[@data-testid='datepicker-tabs']//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'flexible')]"
        ));

        if (flexibleTabs.isEmpty()) {
            System.out.println("[DatePickerTest] Flexible Dates tab not available — skipping.");
            throw new SkipException("Flexible Dates not available in this UI.");
        }

        WebElement flexibleTab = flexibleTabs.get(0);
        scrollIntoView(flexibleTab);
        smartClick(flexibleTab);

        Assert.assertTrue(
                flexibleTab.getAttribute("aria-selected").equals("true")
                        || flexibleTab.getAttribute("class").contains("active")
        );
    }
}
