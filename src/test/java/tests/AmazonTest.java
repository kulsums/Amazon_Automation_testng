package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.List;
import java.util.Set;

public class AmazonTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--disable-blink-features=AutomationControlled",
                "--start-maximized",
                "--disable-infobars",
                "--disable-dev-shm-usage",
                "--no-sandbox"
        );
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    public void amazonSearchTest() {
        driver.get("https://www.amazon.in/");

        Select categoryDropdown = new Select(driver.findElement(By.xpath("//*[@title='Search in']")));
        categoryDropdown.selectByVisibleText("Electronics");

        WebElement searchBox = driver.findElement(By.id("twotabsearchtextbox"));
        searchBox.sendKeys("iPhone 13");

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.autocomplete-results-container div.s-suggestion")));

        List<WebElement> suggestions = driver.findElements(By.cssSelector("div.autocomplete-results-container div.s-suggestion"));
        Assert.assertTrue(suggestions.size() > 0, "No suggestions appeared");

        for (WebElement suggestion : suggestions) {
            String text = suggestion.getText().toLowerCase();
            Assert.assertTrue(text.contains("iphone 13"), "Invalid suggestion: " + text);
        }

        searchBox.clear();
        searchBox.sendKeys("iPhone 13 128 GB");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@id,'sac-suggestion-row-1')][2]"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.a-price")));

        String originalWindow = driver.getWindowHandle();
        WebElement link = driver.findElement(By.cssSelector("h2 a"));
        Actions act = new Actions(driver);
        act.keyDown(Keys.COMMAND).click(link).keyUp(Keys.COMMAND).build().perform();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement appleWatchDropdown = driver.findElement(
                By.xpath("//li/a[@role='button']/span[text()='Apple Watch']")
        );
        js.executeScript("arguments[0].click();", appleWatchDropdown);

        WebElement appleWatchSE = driver.findElement(
                By.xpath("//li[@data-testid='nav-item']/a/span[text()='Apple Watch SE (GPS)']")
        );
        js.executeScript("arguments[0].click();", appleWatchSE);

        WebElement watchImage = driver.findElement(By.xpath("(//a[contains(@title,'Apple Watch SE')])[1]"));

        Actions actions = new Actions(driver);
        actions.moveToElement(watchImage).perform();

        WebElement quickLookBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[contains(text(),'Quick look') or contains(text(),'Quick Look')]")));

        Assert.assertTrue(quickLookBtn.isDisplayed(), "Quick Look is not displayed on hover");

        WebElement productTitle = driver.findElement(By.xpath("(//a[contains(@title, 'Apple Watch SE (2nd Gen, 2023) [GPS 40mm]')])[1]"));
        String expectedTitle = productTitle.getAttribute("title").toLowerCase();

        quickLookBtn.click();

        WebElement modalTitle = driver.findElement(By.xpath("//h2/a"));
        String actualTitle = modalTitle.getText().toLowerCase();

        Assert.assertTrue(actualTitle.contains("apple watch"), "Modal is not for Apple Watch");
        Assert.assertTrue(actualTitle.contains(expectedTitle.substring(0, 10)), "Product mismatch in modal");

        System.out.println("Test Completed Successfully");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
