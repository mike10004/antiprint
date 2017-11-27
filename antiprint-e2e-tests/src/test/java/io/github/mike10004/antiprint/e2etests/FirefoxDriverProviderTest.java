package io.github.mike10004.antiprint.e2etests;

import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.Objects;

public class FirefoxDriverProviderTest extends BrowserUsingTestBase<CustomFirefoxDriver, String> {

    @BeforeClass
    public static void setUpClass() throws Exception {
        Tests.setUpGeckodriver();
    }

    @Test
    public void provide() throws Exception {
        String userAgent = (String) Tests.getNavigatorTestCase(UserAgentFamily.CHROME, OperatingSystemFamily.OS_X).get("userAgent");
        WebDriver driver = createWebDriver(Objects.requireNonNull(userAgent));
        try {
            driver.get("https://www.example.com/");
            maybePauseUntilKilled();
        } finally {
            driver.quit();
        }
    }

    @Override
    protected WebDriverProvider<? extends CustomFirefoxDriver> getWebDriverProvider(String userAgent) {
        return new FirefoxDriverProvider(userAgent);
    }
}