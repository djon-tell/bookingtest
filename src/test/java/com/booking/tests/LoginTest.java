package com.booking.tests;

import com.booking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

public class LoginTest extends BaseTest {

    @BeforeClass
    public void setUpClass() {
        super.setUpClass();
        goToHomepage();

        // Click the Sign in button in the header
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[.//span[contains(text(),'Sign in')]] | //button[.//span[contains(text(),'Sign in')]]")
        ));
        signInBtn.click();

        wait.until(d -> d.getCurrentUrl().contains("account") || d.getCurrentUrl().contains("sign-in"));
    }

    @Test(priority = 1)
    public void testSignInURLCorrect() {
        String currentURL = driver.getCurrentUrl();
        Assert.assertTrue(
                currentURL.contains("login") ||
                        currentURL.contains("signin") ||
                        currentURL.contains("account"),
                "Sign-in page URL should contain a sign-in related path."
        );
    }

    @Test(priority = 2)
    public void testValidEmailShowsVerificationStep() {
        driver.get("https://account.booking.com/sign-in");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        emailField.clear();
        emailField.sendKeys("djonleon00@gmail.com");

        String titleBefore = driver.getTitle();
        String urlBefore = driver.getCurrentUrl();

        driver.findElement(By.xpath("//span[contains(text(),'Continue with email')]")).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.not(ExpectedConditions.urlToBe(urlBefore)),
                ExpectedConditions.not(ExpectedConditions.titleIs(titleBefore)),
                ExpectedConditions.urlContains("otp"),
                ExpectedConditions.urlContains("code")
        ));

        String urlAfter = driver.getCurrentUrl();
        String titleAfter = driver.getTitle();

        boolean urlChanged = !urlAfter.equals(urlBefore);
        boolean titleChanged = !titleAfter.equals(titleBefore);
        boolean redirectedToOTP = urlAfter.contains("otp") || urlAfter.contains("code");

        Assert.assertTrue(urlChanged || titleChanged || redirectedToOTP);
    }

    @Test(priority = 3)
    public void testTitleDiffersFromHomepage() {
        driver.get("https://account.booking.com/sign-in");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        String signInTitle = driver.getTitle();

        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.booking.com");
        wait.until(ExpectedConditions.titleContains("Booking"));
        String homepageTitle = driver.getTitle();

        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());

        Assert.assertNotEquals(signInTitle, homepageTitle);
    }

    @Test(priority = 4)
    public void testEmptyAndInvalidEmailValidation() {
        driver.get("https://account.booking.com/sign-in");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));

        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(text(),'Continue with email')]")
        ));
        continueBtn.click();

        Assert.assertTrue(driver.getCurrentUrl().contains("sign-in") ||
                driver.getCurrentUrl().contains("login"));

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        emailField.clear();
        emailField.sendKeys("dro4898");

        continueBtn.click();

        Assert.assertTrue(driver.getCurrentUrl().contains("sign-in") ||
                driver.getCurrentUrl().contains("login"));
    }

    @Test(priority = 5)
    public void testSignInPageUIElements() {
        driver.get("https://account.booking.com/sign-in");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        Assert.assertTrue(emailField.isDisplayed());

        WebElement continueBtn = driver.findElement(By.xpath("//span[contains(text(),'Continue with email')]"));
        Assert.assertTrue(continueBtn.isDisplayed());

        Assert.assertFalse(driver.getTitle().isEmpty());

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("account") || url.contains("sign-in"));
    }

    @AfterClass
    public void tearDownClass() {
        super.tearDownClass();
    }
}
