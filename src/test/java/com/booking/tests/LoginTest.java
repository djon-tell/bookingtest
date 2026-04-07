package com.booking.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.util.List;

public class LoginTest extends BaseTest {

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("LoginTest suite starting.");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("LoginTest suite complete.");
    }

    @BeforeTest
    public void beforeTest() {
        System.out.println("BeforeTest: login test block starting.");
    }

    @AfterTest
    public void afterTest() {
        System.out.println("AfterTest: login test block done.");
    }

    @BeforeClass
    public void setUp() {
        super.setUp();
    }

    // Navigate fresh to homepage before each login test
    @BeforeMethod
    public void beforeEachTest() {
        goToHomepage();
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() {
        System.out.println("Test finished.");
    }

    // Verify the sign-in page URL matches the expected URL
    @Test(priority = 1)
    public void testSignInURLCorrect() {
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@data-testid='header-sign-up-button']")));
        signInBtn.sendKeys(Keys.RETURN);

        wait.until(d -> d.getCurrentUrl().contains("login")
                || d.getCurrentUrl().contains("signin")
                || d.getCurrentUrl().contains("account"));

        String currentURL = driver.getCurrentUrl();
        System.out.println("Sign-in URL: " + currentURL);

        Assert.assertTrue(
                currentURL.contains("login") || currentURL.contains("signin")
                        || currentURL.contains("account"),
                "Sign-in page URL should match the expected sign-in URL.");
    }

    // Verify the email input field is present on the sign-in page
    @Test(priority = 2)
    public void testEmailFieldPresent() {
        driver.get("https://account.booking.com/sign-in");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='username' or @id='username']")));

        Assert.assertNotNull(emailField,
                "Email input field should be present on the sign-in page.");
        Assert.assertTrue(emailField.isDisplayed(),
                "Email input field should be visible.");
        System.out.println("Email field found and visible.");
    }

    // Verify the sign-in page title is NOT equal to the homepage title
    @Test(priority = 3)
    public void testTitleDiffersFromHomepage() {
        String homepageTitle = driver.getTitle();

        driver.get("https://account.booking.com/sign-in");
        wait.until(ExpectedConditions.not(
                ExpectedConditions.titleIs(homepageTitle)));

        String signInTitle = driver.getTitle();
        System.out.println("Homepage title: " + homepageTitle);
        System.out.println("Sign-in title:  " + signInTitle);

        Assert.assertNotEquals(signInTitle, homepageTitle,
                "Sign-in page title should NOT equal the homepage title.");
    }

    // Verify submitting an empty form keeps you on the sign-in page
    @Test(priority = 4)
    public void testEmptyFormValidation() {
        driver.get("https://account.booking.com/sign-in");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit']"))).click();

        try {
            wait.until(d -> !d.getCurrentUrl()
                    .equals("https://account.booking.com/sign-in"));
        } catch (Exception e) {
            System.out.println("No redirect — stayed on sign-in page.");
        }

        String urlAfterSubmit = driver.getCurrentUrl();
        System.out.println("URL after empty submit: " + urlAfterSubmit);

        Assert.assertTrue(
                urlAfterSubmit.contains("sign-in") || urlAfterSubmit.contains("login"),
                "Should remain on the sign-in page after submitting an empty form.");
    }

    // Verify the number of input fields on the sign-in page is greater than 0
    // Uses soft assertions to check multiple input field conditions
    @Test(priority = 5)
    public void testSignInPageHasInputField() {
        driver.get("https://account.booking.com/sign-in");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));

        SoftAssert softAssert = new SoftAssert();

        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        softAssert.assertTrue(inputs.size() > 0,
                "Number of input fields on the sign-in page should be greater than 0.");

        String title = driver.getTitle();
        softAssert.assertFalse(title == null || title.isEmpty(),
                "Sign-in page title should not be empty.");

        String url = driver.getCurrentUrl();
        softAssert.assertFalse(url == null || url.isEmpty(),
                "Sign-in page URL should not be empty.");

        System.out.println("Input fields found: " + inputs.size());
        softAssert.assertAll();
    }
}