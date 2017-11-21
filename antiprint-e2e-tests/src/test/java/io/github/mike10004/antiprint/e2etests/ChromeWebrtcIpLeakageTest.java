package io.github.mike10004.antiprint.e2etests;

import org.junit.BeforeClass;

public class ChromeWebrtcIpLeakageTest extends WebrtcIpLeakageTestBase {

    @BeforeClass
    public static void setUpChromeWebdriver() {
        Tests.setUpChromedriver();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected WebDriverProvider getWebDriverProvider(Void nothing) {
        return new ChromeDriverProvider();
    }
}
