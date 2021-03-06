package io.github.mike10004.antiprint.e2etests;

import io.github.mike10004.extensibleffdriver.ExtensibleFirefoxDriver;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.Objects;

public class FirefoxDriverProviderTest extends BrowserUsingTestBase<ExtensibleFirefoxDriver, String> {

    @BeforeClass
    public static void setUpClass() {
        Tests.setUpGeckodriver();
    }

    @Test
    public void provide() throws Exception {
        String userAgent = Tests.getNavigatorTestCase(UserAgentFamily.FIREFOX, OperatingSystemFamily.OS_X).input.userAgent;
        WebDriver driver = createWebDriver(Objects.requireNonNull(userAgent));
        try {
            driver.get("https://www.example.com/");
            maybePauseUntilKilled();
        } finally {
            driver.quit();
        }
    }

    @Override
    protected WebDriverProvider<? extends ExtensibleFirefoxDriver> getWebDriverProvider(String userAgent) {
        return new FirefoxDriverProvider(userAgent);
    }

}
