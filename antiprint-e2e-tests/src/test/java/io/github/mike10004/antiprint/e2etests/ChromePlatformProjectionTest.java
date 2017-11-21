package io.github.mike10004.antiprint.e2etests;

import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class ChromePlatformProjectionTest extends PlatformProjectionTestBase {

    @BeforeClass
    public static void setUp() {
        Tests.setUpChromedriver();
    }

    @Test
    public void windows() throws Exception {
        testNavigatorProperties(UserAgentFamily.CHROME, OperatingSystemFamily.WINDOWS);
    }

    @Test
    public void osx() throws Exception {
        testNavigatorProperties(UserAgentFamily.CHROME, OperatingSystemFamily.OS_X);
    }
    @Test
    public void linux() throws Exception {
        testNavigatorProperties(UserAgentFamily.CHROME, OperatingSystemFamily.LINUX);
    }

    @Override
    protected WebDriverProvider<? extends WebDriver> getWebDriverProvider(String userAgent) {
        return new ChromeDriverProvider(userAgent);
    }
}
