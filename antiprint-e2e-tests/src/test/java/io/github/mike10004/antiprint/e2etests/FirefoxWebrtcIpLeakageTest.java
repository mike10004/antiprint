package io.github.mike10004.antiprint.e2etests;

import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;

public class FirefoxWebrtcIpLeakageTest extends WebrtcIpLeakageTestBase {

    @BeforeClass
    public static void setUpWebdriver() {
        Tests.setUpGeckodriver();
    }

    @Override
    protected WebDriverProvider<? extends WebDriver> getWebDriverProvider(Void nothing) {
        return new FirefoxDriverProvider();
    }
}
