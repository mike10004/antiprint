package io.github.mike10004.antiprint.e2etests;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.BeforeClass;

public class ChromeUsingTestBase extends BrowserUsingTestBase {

    @BeforeClass
    public static void setUpChromeWebdriver() {
        ChromeDriverManager.getInstance().version(Tests.chromeDriverVersion()).setup();
    }

}
