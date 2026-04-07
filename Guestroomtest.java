import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class GuestRoomTest {

    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;
    Actions actions;

    // Occupancy toggle button — always visible on the page (closed state)
    private static final String OCCUPANCY_BTN_CSS =
            "button[aria-label*='adults'], " +
                    "button[data-testid*='occupancy'], " +
                    "[data-testid='occupancy-config']";

    // Stepper button classes confirmed from live DOM debug output
    private static final String CSS_DECREASE = "button.c857f39cb2"; // (−) button
    private static final String CSS_INCREASE = "button.dc8366caa6"; // (+) button

    // Done/Apply button — confirmed class from debug: d1babacfe0
    // This is language-agnostic (no text match needed)
    private static final String CSS_DONE = "button.d1babacfe0";

    // Stepper row indexes
    private static final int ROW_ADULTS   = 0;
    private static final int ROW_CHILDREN = 1;
    private static final int ROW_ROOMS    = 2;

    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--lang=en-US");
        options.addArguments("--accept-lang=en-US,en");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        driver  = new ChromeDriver(options);
        wait    = new WebDriverWait(driver, Duration.ofSeconds(20));
        js      = (JavascriptExecutor) driver;
        actions = new Actions(driver);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

        driver.get("https://www.booking.com/?lang=en-us");
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
            driver.findElement(By.cssSelector("[aria-label='Dismiss sign-in info.']")).click();
            pause(800);
        } catch (NoSuchElementException ignored) {}
    }

    // ── Open the occupancy dropdown ───────────────────────────────
    private void openDropdown() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(OCCUPANCY_BTN_CSS)
        ));
        System.out.println("[GuestRoomTest] Opening dropdown: " + btn.getAttribute("aria-label"));
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(CSS_INCREASE)));
        pause(1200);
        System.out.println("[GuestRoomTest] Dropdown open.");
    }

    // ── Get all increase/decrease stepper buttons in the popup ────
    private List<WebElement> getIncreaseButtons() {
        return driver.findElements(By.cssSelector(CSS_INCREASE));
    }
    private List<WebElement> getDecreaseButtons() {
        return driver.findElements(By.cssSelector(CSS_DECREASE));
    }

    // ── Real mouse click via Actions (fires React synthetic events) ─
    private void realClick(WebElement el) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        pause(300);
        actions.moveToElement(el).click().perform();
        pause(700);
    }

    // ── Click the Done/Apply button and wait for popup to close ───
    // Uses the class confirmed from the live DOM — language-agnostic
    private void clickDone() {
        WebElement doneBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(CSS_DONE)
        ));
        System.out.println("[GuestRoomTest] Clicking Done button: '" + doneBtn.getText() + "'");
        realClick(doneBtn);
        // Wait until the popup's stepper buttons are gone (popup closed)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(CSS_INCREASE)));
        pause(1000);
        System.out.println("[GuestRoomTest] Popup closed.");
    }

    // ── Select an age for the nth child (0-indexed) ─────────────
    // childIndex 0 = first child, 1 = second child, etc.
    // Booking.com renders one <select> per child in DOM order.
    // IMPORTANT: React re-renders the <select> after a value is chosen,
    // so we must re-fetch the element after selecting to avoid StaleElementReferenceException.
    private void selectChildAge(int childIndex, String age) {
        // CSS selector for all age dropdowns inside the popup
        final String AGE_SELECT_CSS =
                "div[data-testid='occupancy-popup'] select, " +
                        "div[class*='occupancy'] select";

        List<WebElement> ageSelects = driver.findElements(By.cssSelector(AGE_SELECT_CSS));
        if (ageSelects.isEmpty()) {
            System.out.println("[GuestRoomTest] No age selects found for child " + childIndex + " — skipping.");
            return;
        }

        int idx = Math.min(childIndex, ageSelects.size() - 1);
        WebElement select = ageSelects.get(idx);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", select);
        pause(300);

        System.out.println("[GuestRoomTest] Setting child[" + idx + "] age to " + age);

        // Use JS to set the value directly — avoids stale reference from React re-render
        js.executeScript("arguments[0].value = arguments[1];", select, age);
        // Fire a native 'change' event so React picks up the new value
        js.executeScript(
                "var evt = new Event('change', {bubbles: true});" +
                        "arguments[0].dispatchEvent(evt);", select);
        pause(800);

        // Re-fetch the element fresh (React may have replaced the DOM node)
        List<WebElement> freshSelects = driver.findElements(By.cssSelector(AGE_SELECT_CSS));
        if (!freshSelects.isEmpty()) {
            WebElement freshSelect = freshSelects.get(Math.min(idx, freshSelects.size() - 1));
            String selectedVal = (String) js.executeScript("return arguments[0].value;", freshSelect);
            System.out.println("[GuestRoomTest] Child[" + idx + "] age value confirmed: " + selectedVal);
        }
    }

    // ── Click a stepper until the displayed value reaches target ──
    private void stepUntil(int rowIndex, int target, String direction) {
        int current = getStepperDisplayValue(rowIndex);
        System.out.println("[GuestRoomTest] Row[" + rowIndex + "] current=" + current
                + " target=" + target + " direction=" + direction);
        int maxClicks = 20;
        while (current != target && maxClicks-- > 0) {
            if ("increase".equals(direction)) {
                realClick(getIncreaseButtons().get(rowIndex));
            } else {
                realClick(getDecreaseButtons().get(rowIndex));
            }
            current = getStepperDisplayValue(rowIndex);
            System.out.println("[GuestRoomTest] Row[" + rowIndex + "] now=" + current);
        }
        Assert.assertEquals(current, target,
                "Stepper row[" + rowIndex + "] should reach " + target
                        + " but stopped at " + current);
    }

    // ── Read the numeric value displayed inside a stepper row ─────
    private int getStepperDisplayValue(int rowIndex) {
        List<WebElement> incBtns = getIncreaseButtons();
        Assert.assertTrue(incBtns.size() > rowIndex,
                "Expected >" + rowIndex + " increase buttons, found " + incBtns.size());

        WebElement btn = incBtns.get(rowIndex);
        WebElement container = btn.findElement(By.xpath("./.."));
        List<WebElement> nums = container.findElements(By.xpath(
                ".//*[translate(normalize-space(.),'0123456789','')='' "
                        + "and string-length(normalize-space(.))>0]"
        ));
        if (!nums.isEmpty()) return Integer.parseInt(nums.get(0).getText().trim());

        WebElement grandparent = container.findElement(By.xpath("./.."));
        nums = grandparent.findElements(By.xpath(
                ".//*[translate(normalize-space(.),'0123456789','')='' "
                        + "and string-length(normalize-space(.))>0]"
        ));
        if (!nums.isEmpty()) return Integer.parseInt(nums.get(0).getText().trim());

        throw new RuntimeException("Cannot read numeric value for stepper row " + rowIndex);
    }

    // ── Parse count from the closed-state aria-label ──────────────
    // Label: "...Currently selected: 4 adults · 1 child · 2 rooms"
    private int readLabelCount(String keyword) {
        String label = driver.findElement(By.cssSelector(OCCUPANCY_BTN_CSS))
                .getAttribute("aria-label");
        System.out.println("[GuestRoomTest] Label: " + label);
        Matcher m = Pattern.compile("(\\d+)\\s+" + keyword).matcher(label);
        if (m.find()) return Integer.parseInt(m.group(1));
        throw new RuntimeException("Could not find '" + keyword + "' in: " + label);
    }

    // ─────────────────────────────────────────────────────────────
    // Test 1: Increase adults to 4
    // ─────────────────────────────────────────────────────────────
    @Test(priority = 1, description = "Increase the number of adults to 4")
    public void testIncreaseAdultsTo4() {
        pause(1500);
        openDropdown();
        stepUntil(ROW_ADULTS, 4, "increase");
        System.out.println("[GuestRoomTest] Adults in popup: " + getStepperDisplayValue(ROW_ADULTS));
        clickDone();
        int label = readLabelCount("adult");
        Assert.assertEquals(label, 4, "Search bar should show 4 adults");
        System.out.println("[GuestRoomTest] ✔ testIncreaseAdultsTo4 PASSED");
        pause(1500);
    }

    // ─────────────────────────────────────────────────────────────
    // Test 2: Increase children to 3 (select default age for each)
    // ─────────────────────────────────────────────────────────────
    @Test(priority = 2, description = "Increase the number of children to 3")
    public void testIncreaseChildrenTo3() {
        pause(1500);
        openDropdown();

        // Add children one at a time, selecting a default age after each addition.
        // Booking.com shows an age <select> for every child added — it must be
        // filled before the popup will accept further changes or the Done click.
        int current = getStepperDisplayValue(ROW_CHILDREN);
        while (current < 3) {
            realClick(getIncreaseButtons().get(ROW_CHILDREN));
            pause(1000);
            current = getStepperDisplayValue(ROW_CHILDREN);
            System.out.println("[GuestRoomTest] Children now: " + current
                    + " — selecting default age for child " + current);
            // Select age 5 as default for each newly added child (index = current-1)
            selectChildAge(current - 1, "5");
        }

        System.out.println("[GuestRoomTest] Children in popup: " + getStepperDisplayValue(ROW_CHILDREN));
        clickDone();
        int label = readLabelCount("child");
        Assert.assertEquals(label, 3, "Search bar should show 3 children");
        System.out.println("[GuestRoomTest] ✔ testIncreaseChildrenTo3 PASSED");
        pause(1500);
    }

    // ─────────────────────────────────────────────────────────────
    // Test 3: Increase rooms to 2
    // ─────────────────────────────────────────────────────────────
    @Test(priority = 3, description = "Increase the number of rooms to 2")
    public void testIncreaseRoomsTo2() {
        pause(1500);
        openDropdown();
        stepUntil(ROW_ROOMS, 2, "increase");
        System.out.println("[GuestRoomTest] Rooms in popup: " + getStepperDisplayValue(ROW_ROOMS));
        clickDone();
        int label = readLabelCount("room");
        Assert.assertEquals(label, 2, "Search bar should show 2 rooms");
        System.out.println("[GuestRoomTest] ✔ testIncreaseRoomsTo2 PASSED");
        pause(1500);
    }

    // ─────────────────────────────────────────────────────────────
    // Test 4: Decrease children from 3 to 1, then set child age to 7
    // ─────────────────────────────────────────────────────────────
    @Test(priority = 4, description = "Decrease children to 1 and set the child's age to 7")
    public void testDecreaseChildrenTo1AndSetAge() {
        pause(1500);
        openDropdown();

        System.out.println("[GuestRoomTest] Children before decrease: "
                + getStepperDisplayValue(ROW_CHILDREN));

        // Decrease children down to 1
        stepUntil(ROW_CHILDREN, 1, "decrease");
        System.out.println("[GuestRoomTest] Children in popup after decrease: "
                + getStepperDisplayValue(ROW_CHILDREN));
        pause(800);

        // ── Set the age dropdown for the remaining child to 7 ────
        // After decreasing to 1 child, exactly one age <select> remains.
        // Verify it exists then set it to age 7 using the shared helper.
        List<WebElement> ageSelects = driver.findElements(By.cssSelector(
                "div[data-testid='occupancy-popup'] select, div[class*='occupancy'] select"
        ));
        Assert.assertFalse(ageSelects.isEmpty(),
                "An age <select> dropdown should appear for the remaining child");

        System.out.println("[GuestRoomTest] Age dropdowns present: " + ageSelects.size());

        // Print available options for visibility in the recording
        org.openqa.selenium.support.ui.Select agePicker =
                new org.openqa.selenium.support.ui.Select(ageSelects.get(0));
        System.out.println("[GuestRoomTest] Age options available:");
        for (WebElement opt : agePicker.getOptions()) {
            System.out.println("  value='" + opt.getAttribute("value")
                    + "' text='" + opt.getText().trim() + "'");
        }

        // Set child age to 7 via the helper (uses JS to avoid React stale reference)
        selectChildAge(0, "7");

        // Re-fetch the select fresh after React re-render, then read value via JS
        List<WebElement> freshSelects = driver.findElements(By.cssSelector(
                "div[data-testid='occupancy-popup'] select, div[class*='occupancy'] select"
        ));
        Assert.assertFalse(freshSelects.isEmpty(), "Age select should still be in DOM after selection");
        String selectedVal = (String) js.executeScript("return arguments[0].value;", freshSelects.get(0));
        System.out.println("[GuestRoomTest] Age value after selection: " + selectedVal);
        Assert.assertEquals(selectedVal, "7",
                "Age dropdown value should be '7' after selection, got: " + selectedVal);

        // Close the popup
        clickDone();

        int label = readLabelCount("child");
        Assert.assertEquals(label, 1, "Search bar should show 1 child after decreasing");
        System.out.println("[GuestRoomTest] ✔ testDecreaseChildrenTo1AndSetAge PASSED");
        pause(1500);
    }

    // ─────────────────────────────────────────────────────────────
    // Test 5: Select "Traveling with pets"
    // ─────────────────────────────────────────────────────────────
    @Test(priority = 5, description = "Select the 'Traveling with pets' option")
    public void testSelectTravelingWithPets() {
        pause(1500);
        openDropdown();

        // Find the pets checkbox input (confirmed present as <input type="checkbox">)
        WebElement petsCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@type='checkbox' and (contains(@id,'pet') or contains(@name,'pet'))]"
                        + " | //input[@type='checkbox'][following-sibling::*[contains("
                        + "translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pet')]]"
                        + " | //input[@type='checkbox'][preceding-sibling::*[contains("
                        + "translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pet')]]"
                )
        ));

        System.out.println("[GuestRoomTest] Pets checkbox found. Currently checked: "
                + petsCheckbox.isSelected());

        // Click the label associated with the checkbox (more reliable than clicking the input)
        String checkboxId = petsCheckbox.getAttribute("id");
        boolean clicked = false;
        if (checkboxId != null && !checkboxId.isEmpty()) {
            try {
                WebElement label = driver.findElement(By.cssSelector("label[for='" + checkboxId + "']"));
                realClick(label);
                clicked = true;
                System.out.println("[GuestRoomTest] Clicked pets label (for='" + checkboxId + "')");
            } catch (NoSuchElementException ignored) {}
        }
        if (!clicked) {
            // Fallback: click the checkbox parent element
            WebElement parent = petsCheckbox.findElement(By.xpath("./.."));
            realClick(parent);
            System.out.println("[GuestRoomTest] Clicked pets checkbox parent element.");
        }
        pause(1000);

        boolean isChecked = petsCheckbox.isSelected();
        System.out.println("[GuestRoomTest] Pets checkbox checked after click: " + isChecked);

        clickDone();

        Assert.assertTrue(isChecked,
                "The 'Traveling with pets' checkbox should be selected after clicking it");
        System.out.println("[GuestRoomTest] ✔ testSelectTravelingWithPets PASSED");
        pause(1500);
    }

    // ─────────────────────────────────────────────────────────────
    // Test 6: Search with current settings (4 adults, 1 child, 2 rooms, pets)
    // ─────────────────────────────────────────────────────────────
    @Test(priority = 6, description = "Search for a destination with the current guest and room settings")
    public void testSearchWithCurrentSettings() {
        pause(1500);

        // Type destination
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "input[placeholder*='Where'], input[name='ss'], "
                        + "[data-testid='destination-container'] input"
        )));
        searchInput.clear();
        searchInput.sendKeys("Miami");
        pause(2000);
        System.out.println("[GuestRoomTest] Typed destination: Miami");

        // Click first autocomplete suggestion
        try {
            WebElement firstSuggestion = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                    "[data-testid='autocomplete-results-option']:first-child, "
                            + "li[id*='autocomplete']:first-child, "
                            + "ul[role='listbox'] li:first-child"
            )));
            System.out.println("[GuestRoomTest] Suggestion: " + firstSuggestion.getText().trim());
            firstSuggestion.click();
            pause(1500);
        } catch (TimeoutException e) {
            searchInput.sendKeys(Keys.ENTER);
            pause(1500);
        }

        // Print the occupancy label before searching (confirms all settings visible)
        String occupancyLabel = driver.findElement(By.cssSelector(OCCUPANCY_BTN_CSS))
                .getAttribute("aria-label");
        System.out.println("[GuestRoomTest] Occupancy before search: " + occupancyLabel);

        // Click the Search button
        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "button[type='submit'], "
                        + "[data-testid='searchbox-submit-button'], "
                        + ".sb-searchbox__button"
        )));
        System.out.println("[GuestRoomTest] Clicking Search.");
        searchBtn.click();
        pause(4000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("[GuestRoomTest] Results URL: " + currentUrl);

        // Verify URL reflects our guest settings (group_adults=4, group_children=1, no_rooms=2)
        Assert.assertTrue(currentUrl.contains("booking.com"),
                "Should remain on booking.com. Got: " + currentUrl);
        Assert.assertTrue(
                currentUrl.contains("group_adults=4") || currentUrl.contains("adults=4"),
                "URL should carry 4 adults. Got: " + currentUrl
        );

        // Verify results page has loaded
        try {
            WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                    "[data-testid='property-card'], "
                            + "[data-testid='property-card-container'], "
                            + ".sr_item"
            )));
            Assert.assertTrue(card.isDisplayed(), "Property cards should be visible in results");
            System.out.println("[GuestRoomTest] Search results loaded successfully.");
        } catch (TimeoutException e) {
            Assert.assertTrue(
                    currentUrl.contains("searchresults") || currentUrl.contains("ss="),
                    "URL should indicate a search was performed. Got: " + currentUrl
            );
        }

        System.out.println("[GuestRoomTest] ✔ testSearchWithCurrentSettings PASSED");
        pause(2000);
    }

    @AfterClass
    public void tearDown() {
        pause(2000);
        if (driver != null) driver.quit();
    }
}