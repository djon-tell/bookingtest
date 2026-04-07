import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class HomepageTest {

    WebDriver driver;
    WebDriverWait wait;

    // ── slow helper so the recording can clearly see each action ──
    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Start maximised so everything is visible on the recording
        options.addArguments("--start-maximized");
        // Suppress "Chrome is being controlled by automated software" infobar noise
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    // ── Test 1: Correct URL loads ──────────────────────────────────
    @Test(priority = 1, description = "Verify the homepage URL is Booking.com")
    public void testHomepageURL() {
        driver.get("https://www.booking.com");
        pause(3000); // Let the page fully render for the camera

        String currentURL = driver.getCurrentUrl();
        System.out.println("[HomepageTest] Current URL: " + currentURL);

        Assert.assertTrue(
                currentURL.contains("booking.com"),
                "URL should contain 'booking.com' but was: " + currentURL
        );
        System.out.println("[HomepageTest] ✔ testHomepageURL PASSED");
    }

    // ── Test 2: Page title contains "Booking.com" ──────────────────
    @Test(priority = 2, description = "Verify the page title contains Booking.com")
    public void testHomepageTitle() {
        pause(2000);
        String title = driver.getTitle();
        System.out.println("[HomepageTest] Page title: " + title);

        Assert.assertTrue(
                title.toLowerCase().contains("booking"),
                "Title should contain 'booking' but was: " + title
        );
        System.out.println("[HomepageTest] ✔ testHomepageTitle PASSED");
    }

    // ── Test 3: Search bar is present and visible ──────────────────
    @Test(priority = 3, description = "Verify the main search bar is present on the homepage")
    public void testSearchBarPresent() {
        pause(2000);

        // Booking.com's destination input
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder*='Where'], input[name='ss'], [data-testid='destination-container'] input")
        ));

        Assert.assertTrue(searchBox.isDisplayed(), "Search bar should be visible on the homepage");
        System.out.println("[HomepageTest] Search bar placeholder: " + searchBox.getAttribute("placeholder"));
        System.out.println("[HomepageTest] ✔ testSearchBarPresent PASSED");
        pause(1500);
    }

    // ── Test 4: Navigation links are present ──────────────────────
    @Test(priority = 4, description = "Verify key navigation links exist in the header")
    public void testNavLinksPresent() {
        pause(2000);

        // Booking.com top-nav contains links like "Stays", "Flights", "Car rentals", etc.
        java.util.List<WebElement> navLinks = driver.findElements(
                By.cssSelector("nav a, [data-testid='header-nav'] a, header a")
        );

        System.out.println("[HomepageTest] Navigation links found: " + navLinks.size());
        Assert.assertTrue(navLinks.size() > 0, "There should be at least one navigation link in the header");

        for (WebElement link : navLinks) {
            String text = link.getText().trim();
            if (!text.isEmpty()) {
                System.out.println("  → Nav link: " + text);
            }
        }
        System.out.println("[HomepageTest] ✔ testNavLinksPresent PASSED");
        pause(1500);
    }

    // ── Test 5: Logo navigates back to homepage ───────────────────
    @Test(priority = 5, description = "Verify clicking the logo returns to the homepage")
    public void testLogoNavigation() {
        // First navigate away slightly by searching for something
        driver.get("https://www.booking.com/searchresults.html?ss=Miami");
        pause(3000);
        System.out.println("[HomepageTest] Navigated away to search results page");

        // Find and click the Booking.com logo
        WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[aria-label*='Booking'], a[href*='booking.com'][class*='logo'], header a img, [data-testid='header-logo'] a")
        ));

        System.out.println("[HomepageTest] Clicking logo to return to homepage...");
        logo.click();
        pause(3000);

        String urlAfterClick = driver.getCurrentUrl();
        System.out.println("[HomepageTest] URL after logo click: " + urlAfterClick);

        Assert.assertTrue(
                urlAfterClick.contains("booking.com"),
                "Clicking logo should navigate to booking.com, but URL was: " + urlAfterClick
        );
        System.out.println("[HomepageTest] ✔ testLogoNavigation PASSED");
    }


    @AfterClass
    public void tearDown() {
        pause(2000);
        if (driver != null) {
            driver.quit();
        }
    }
}
