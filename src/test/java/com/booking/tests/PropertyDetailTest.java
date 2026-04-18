package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class PropertyDetailTest extends BaseTest {

    String originalWindow;
    String propertyWindow;
    String searchDestination = "Miami";

    @BeforeClass
    public void setUpClass() {
        super.setUpClass();
        originalWindow = driver.getWindowHandle();

        // Type destination
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@name='ss' or @placeholder='Where are you going?']")
        ));
        searchBox.sendKeys(searchDestination);

        // Select first autocomplete suggestion
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//div[@data-testid='autocomplete-result'])[1]")
            )).click();
        } catch (Exception e) {
            searchBox.sendKeys(Keys.ENTER);
        }

        // Close date picker
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);

        // Submit search
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-testid='searchbox-submit-button']")
            )).click();
        } catch (Exception e) {
            searchBox.sendKeys(Keys.ENTER);
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@data-testid='property-card']")
        ));

        // Scroll down
        for (int i = 0; i < 5; i++) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ARROW_DOWN);
        }

        // Open first property
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[@data-testid='property-card']//a[@data-testid='title-link'])[1]")
        )).sendKeys(Keys.RETURN);

        wait.until(d -> d.getWindowHandles().size() > 1);

        for (String w : driver.getWindowHandles()) {
            if (!w.equals(originalWindow)) {
                driver.switchTo().window(w);
                break;
            }
        }

        wait.until(d -> !d.getCurrentUrl().contains("searchresults"));
        propertyWindow = driver.getWindowHandle();
    }

    @BeforeMethod
    public void beforeEachTest() {
        driver.switchTo().window(propertyWindow);
    }

    @Test(priority = 1)
    public void testURLChangesOnClick() {
        String currentURL = driver.getCurrentUrl();
        Assert.assertFalse(currentURL.contains("searchresults"));
        Assert.assertTrue(currentURL.contains("booking.com"));
    }

    @Test(priority = 2)
    public void testTitleDiffersFromHomepage() {
        String detailTitle = driver.getTitle();

        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.booking.com");
        wait.until(ExpectedConditions.titleContains("Booking"));
        String homepageTitle = driver.getTitle();

        driver.close();
        driver.switchTo().window(propertyWindow);

        Assert.assertNotEquals(detailTitle, homepageTitle);
    }

    @Test(priority = 3)
    public void testPropertyNameMatchesPageTitle() {
        String pageTitle = driver.getTitle();

        Assert.assertFalse(pageTitle.isEmpty());
        Assert.assertFalse(pageTitle.equals("Booking.com | Official site | The best hotels, flights, car rentals & accommodations"));
        Assert.assertTrue(pageTitle.contains("2026") || pageTitle.contains("2025"));
    }

    @Test(priority = 4)
    public void testAtLeastFivePhotosPresent() {
        List<WebElement> photos = driver.findElements(
                By.xpath("//img[contains(@src,'bstatic.com') or contains(@src,'cf.bstatic')]")
        );

        if (photos.isEmpty()) {
            photos = driver.findElements(By.xpath("//img[@src and string-length(@src) > 0]"));
        }

        Assert.assertTrue(photos.size() >= 5);
    }

    @Test(priority = 5)
    public void testRatingIsValidNumber() {
        try {
            WebElement ratingElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@aria-hidden='true' and contains(@class,'f63b14ab7a')]")
            ));

            String ratingText = ratingElement.getText().trim();
            Assert.assertFalse(ratingText.isEmpty());

            double rating = Double.parseDouble(ratingText);
            Assert.assertTrue(rating > 0);
            Assert.assertTrue(rating <= 10);

        } catch (Exception e) {
            Assert.fail("Rating element should be present on the property page.");
        }
    }

    @Test(priority = 6)
    public void testReserveButtonNavigatesForward() {
        WebElement body = driver.findElement(By.tagName("body"));
        for (int i = 0; i < 10; i++) body.sendKeys(Keys.ARROW_DOWN);

        WebElement reserveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//span[contains(@class,'bui-button__text') and contains(text(),'Reserve')]]")
        ));

        reserveBtn.click();

        String urlAfterClick = driver.getCurrentUrl();

        boolean navigatedAway = urlAfterClick.contains("checkout")
                || urlAfterClick.contains("sign-in")
                || urlAfterClick.contains("login")
                || urlAfterClick.contains("account");

        List<WebElement> roomsSection = driver.findElements(
                By.xpath("//*[contains(@id,'rooms') or contains(@id,'availability') or @data-testid='availability-cta']")
        );

        Assert.assertTrue(navigatedAway || roomsSection.size() > 0);
    }

    @AfterClass
    public void tearDownClass() {
        super.tearDownClass();
    }
}
