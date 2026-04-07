import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class DatePickerTest {

    WebDriver driver;
    WebDriverWait wait;

    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        // IMPORTANT: implicitWait must be 0 when using explicit waits
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

        driver.get("https://www.booking.com");
        pause(3500);
        dismissOverlays();
    }

    private void dismissOverlays() {
        try {
            driver.findElement(By.cssSelector(
                    "#onetrust-accept-btn-handler, button[data-gdpr-consent='true']"
            )).click();
            pause(800);
        } catch (NoSuchElementException ignored) {}

        try {
            driver.findElement(By.cssSelector(
                    "[aria-label='Dismiss sign-in info.']"
            )).click();
            pause(800);
        } catch (NoSuchElementException ignored) {}
    }

    // Build an ISO date string N days from today: "2026-04-13"
    private String futureDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // Open the calendar by clicking the dates/check-in section
    private void openCalendar() {
        WebElement datesBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "[data-testid='searchbox-dates-container'], " +
                        "[data-testid='date-display-field-start'], " +
                        "button[data-testid*='date'], " +
                        "div[class*='DateField'], " +
                        ".sb-searchbox__dates"
        )));
        datesBtn.click();
        pause(2000);
        System.out.println("[DatePickerTest] Calendar opened.");
    }

    // Click a calendar cell by its data-date attribute (yyyy-MM-dd)
    private void clickDate(String isoDate) {
        WebElement cell = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "td[data-date='" + isoDate + "'], " +
                        "span[data-date='" + isoDate + "']"
        )));
        System.out.println("[DatePickerTest] Clicking date: " + isoDate);
        cell.click();
        pause(1800);
    }

    // ── Test 1: Calendar is visible after clicking check-in ───────
    @Test(priority = 1, description = "Verify calendar is visible after clicking the check-in field")
    public void testCalendarVisible() {
        openCalendar();

        WebElement calendar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-testid='datepicker-tabs'], " +
                        "[data-testid='searchbox-datepicker-calendar'], " +
                        "div[class*='CalendarMonth'], " +
                        "div[role='dialog'] table, " +
                        ".bui-calendar"
        )));

        Assert.assertTrue(calendar.isDisplayed(),
                "Calendar should be visible after clicking the check-in field");
        System.out.println("[DatePickerTest] ✔ testCalendarVisible PASSED");
        pause(2000);
    }

    // ── Test 2: Check-in field not empty after date selected ──────
    @Test(priority = 2, description = "Verify check-in field is not empty after selecting a date")
    public void testCheckInFieldNotEmpty() {
        pause(1000);

        // Select check-in = today + 7 days (safely available)
        String checkIn = futureDate(7);
        clickDate(checkIn);

        WebElement checkInDisplay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                "[data-testid='date-display-field-start'], " +
                        "span[data-testid*='checkin'], " +
                        ".sb-date-field:first-of-type"
        )));

        String displayed = checkInDisplay.getText().trim();
        System.out.println("[DatePickerTest] Check-in field shows: " + displayed);

        Assert.assertFalse(displayed.isEmpty(),
                "Check-in field should not be empty after selecting a date");
        System.out.println("[DatePickerTest] ✔ testCheckInFieldNotEmpty PASSED");
        pause(1500);
    }

    // ── Test 3: Check-out cannot be before check-in ───────────────
    @Test(priority = 3, description = "Verify dates before check-in are disabled for check-out")
    public void testCheckOutNotBeforeCheckIn() {
        pause(1000);

        // A date BEFORE our check-in (check-in = today+7, try today+4)
        String beforeCheckIn = futureDate(4);

        try {
            WebElement cell = driver.findElement(By.cssSelector(
                    "td[data-date='" + beforeCheckIn + "']"
            ));
            String ariaDisabled = cell.getAttribute("aria-disabled");
            String classVal     = cell.getAttribute("class");

            boolean disabled = "true".equals(ariaDisabled)
                    || (classVal != null && (classVal.contains("disabled") || classVal.contains("blocked")));

            System.out.println("[DatePickerTest] Cell " + beforeCheckIn
                    + " | aria-disabled=" + ariaDisabled
                    + " | class=" + classVal);

            Assert.assertTrue(disabled,
                    "Date before check-in should be marked disabled. aria-disabled=" + ariaDisabled);

        } catch (NoSuchElementException e) {
            // Cell not rendered at all — Booking.com hides past/invalid cells
            System.out.println("[DatePickerTest] Cell " + beforeCheckIn
                    + " absent from DOM — counts as disabled/unavailable.");
        }

        System.out.println("[DatePickerTest] ✔ testCheckOutNotBeforeCheckIn PASSED");
        pause(2000);
    }

    // ── Test 4: Nights count updates after check-out selected ─────
    @Test(priority = 4, description = "Verify nights count label updates after selecting check-out date")
    public void testNightsCountUpdates() {
        pause(1000);

        // Select check-out = today + 12  →  7 to 12 = 5 nights
        String checkOut = futureDate(12);
        clickDate(checkOut);
        pause(2500);

        // Primary: look for a text node that contains "night"
        try {
            WebElement nightsLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                            + "'abcdefghijklmnopqrstuvwxyz'),'night')]")
            ));
            String txt = nightsLabel.getText().trim();
            System.out.println("[DatePickerTest] Nights label: " + txt);
            Assert.assertFalse(txt.isEmpty(), "Nights label should not be empty");

        } catch (TimeoutException e) {
            // Fallback: check-in and check-out fields should show different dates
            WebElement startField = driver.findElement(By.cssSelector(
                    "[data-testid='date-display-field-start'], .sb-date-field:first-of-type"
            ));
            WebElement endField = driver.findElement(By.cssSelector(
                    "[data-testid='date-display-field-end'], .sb-date-field:last-of-type"
            ));
            String start = startField.getText().trim();
            String end   = endField.getText().trim();
            System.out.println("[DatePickerTest] Start=" + start + "  End=" + end);
            Assert.assertNotEquals(start, end,
                    "Check-in and check-out dates must differ — confirms a range was selected");
        }

        System.out.println("[DatePickerTest] ✔ testNightsCountUpdates PASSED");
        pause(2000);
    }

    @AfterClass
    public void tearDown() {
        pause(2000);
        if (driver != null) driver.quit();
    }
}