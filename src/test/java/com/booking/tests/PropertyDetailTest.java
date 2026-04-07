package com.booking.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.util.List;

public class PropertyDetailTest extends BaseTest {

    String propertyWindow;

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("PropertyDetailTest suite starting.");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("PropertyDetailTest suite complete.");
    }

    @BeforeTest
    public void beforeTest() {
        System.out.println("BeforeTest: property detail test block starting.");
    }

    @AfterTest
    public void afterTest() {
        System.out.println("AfterTest: property detail test block done.");
    }

    @BeforeClass
    public void setUp() {
        super.setUp();
        goToHomepage();
        searchFor("Miami");
        openFirstProperty();
        propertyWindow = driver.getWindowHandle();
        System.out.println("Property window saved: " + propertyWindow);
    }

    @BeforeMethod
    public void beforeEachTest() {
        closeExtraTabsKeepProperty();
        driver.switchTo().window(propertyWindow);
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() {
        closeExtraTabsKeepProperty();
        System.out.println("Test finished.");
    }

    // Closes any tab that is not originalWindow or propertyWindow
    private void closeExtraTabsKeepProperty() {
        try {
            for (String w : driver.getWindowHandles()) {
                if (!w.equals(originalWindow) && !w.equals(propertyWindow)) {
                    driver.switchTo().window(w);
                    driver.close();
                }
            }
            driver.switchTo().window(propertyWindow);
        } catch (Exception e) {
            System.out.println("Tab cleanup: " + e.getMessage());
        }
    }

    // Verify the URL changes when navigating to a property page
    @Test(priority = 1)
    public void testURLChangesOnClick() {
        String currentURL = driver.getCurrentUrl();
        System.out.println("Property page URL: " + currentURL);

        Assert.assertFalse(currentURL.contains("searchresults"),
                "URL should no longer be the search results page.");
        Assert.assertTrue(currentURL.contains("booking.com"),
                "URL should still be on booking.com.");
    }

    // Verify the property name element text is not empty
    @Test(priority = 2)
    public void testPropertyNameNotEmpty() {
        WebElement propertyName;
        try {
            propertyName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[@class='pp-header__title']")));
        } catch (Exception e) {
            try {
                propertyName = driver.findElement(
                        By.xpath("//h2[contains(@class,'header__title') or contains(@class,'property-header')]"));
            } catch (NoSuchElementException e2) {
                propertyName = driver.findElement(
                        By.xpath("(//div[contains(@class,'pp-header') or contains(@id,'hp_hotel_name')]//h2)[1]"));
            }
        }

        String nameText = propertyName.getText().trim();
        System.out.println("Property name: " + nameText);

        Assert.assertFalse(nameText.isEmpty(),
                "Property name text should not be empty.");
    }

    // Verify the property page title is NOT equal to the homepage title
    @Test(priority = 3)
    public void testTitleDiffersFromHomepage() {
        String detailTitle = driver.getTitle();

        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.booking.com");
        wait.until(ExpectedConditions.titleContains("Booking"));
        String homepageTitle = driver.getTitle();

        driver.close();
        driver.switchTo().window(propertyWindow);

        System.out.println("Homepage title: " + homepageTitle);
        System.out.println("Detail title:   " + detailTitle);

        Assert.assertNotEquals(detailTitle, homepageTitle,
                "Property page title should NOT equal the homepage title.");
    }

    // Verify the Reserve button is present on the property page
    @Test(priority = 4)
    public void testBookButtonPresent() throws InterruptedException {
        // Scroll down to load the room/availability section
        WebElement body = driver.findElement(By.tagName("body"));
        for (int i = 0; i < 10; i++) body.sendKeys(Keys.ARROW_DOWN);
        Thread.sleep(1500);

        WebElement bookBtn = null;

        String[] xpaths = {
                "//button[@data-testid='reservation-block-availability-cta-2']",
                "//button[@data-testid='reservation-block-availability-cta']",
                "//button[contains(text(),'Reserve') or contains(text(),'Book') or contains(text(),'Availability') or contains(text(),'Check availability')]",
                "//a[contains(text(),'Reserve') or contains(text(),'Book')]",
                "//button[contains(@class,'js-booking-trigger')]",
                "//*[contains(@id,'availability') or contains(@id,'rooms')]//button",
                "(//button[@type='submit'])[1]"
        };

        for (String xpath : xpaths) {
            try {
                bookBtn = driver.findElement(By.xpath(xpath));
                if (bookBtn != null && bookBtn.isDisplayed()) {
                    System.out.println("Found with: " + xpath);
                    break;
                }
            } catch (Exception e) {
                System.out.println("Not found: " + xpath);
            }
        }

        Assert.assertNotNull(bookBtn,
                "Reserve/Book button should be present on the property page.");
        Assert.assertTrue(bookBtn.isDisplayed(),
                "Reserve/Book button should be visible.");
        System.out.println("Book button found: " + bookBtn.getText());
    }

    // Verify the price element text is not empty
    @Test(priority = 5)
    public void testPriceNotEmpty() throws InterruptedException {
        // Scroll down to load the pricing section
        WebElement body = driver.findElement(By.tagName("body"));
        for (int i = 0; i < 10; i++) body.sendKeys(Keys.ARROW_DOWN);
        Thread.sleep(1500);

        SoftAssert softAssert = new SoftAssert();

        List<WebElement> priceElements = driver.findElements(
                By.xpath("//*[@data-testid='price-and-discounted-price']"));

        if (priceElements.isEmpty()) {
            priceElements = driver.findElements(
                    By.xpath("//*[contains(@class,'prco-valign') or contains(@class,'bui-price')]"));
        }

        if (priceElements.isEmpty()) {
            priceElements = driver.findElements(
                    By.xpath("//span[contains(text(),'$') or contains(text(),'€') or contains(text(),'£')]"));
        }

        if (priceElements.isEmpty()) {
            priceElements = driver.findElements(
                    By.xpath("//*[contains(@class,'price') or contains(@class,'rate') or contains(@class,'cost')]"));
        }

        softAssert.assertTrue(priceElements.size() > 0,
                "At least one price element should be present on the property page.");

        if (priceElements.size() > 0) {
            String priceText = "";
            for (WebElement el : priceElements) {
                priceText = el.getText().trim();
                if (!priceText.isEmpty()) break;
            }
            System.out.println("Price: " + priceText);
            softAssert.assertFalse(priceText.isEmpty(),
                    "Price element text should not be empty.");
        }

        softAssert.assertAll();
    }

    // Verify the number of photo elements on the page is greater than 0
    @Test(priority = 6)
    public void testPhotoElementPresent() {
        List<WebElement> photos = driver.findElements(
                By.xpath("//img[contains(@src,'bstatic.com') or contains(@src,'cf.bstatic')]"));

        if (photos.isEmpty()) {
            photos = driver.findElements(
                    By.xpath("//img[@src and string-length(@src) > 0]"));
        }

        System.out.println("Photos found: " + photos.size());

        Assert.assertTrue(photos.size() > 0,
                "Number of photo elements on the property page should be greater than 0.");
    }
}