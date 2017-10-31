package io.github.mike10004.antiprint.e2etests;

import com.github.mike10004.xvfbtesting.XvfbRule;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.BeforeClass;
import org.junit.Rule;

public class BrowserUsingTestBase {
    @BeforeClass
    public static void setUpChromeWebdriver() {
        ChromeDriverManager.getInstance().setup();
    }

    protected static final boolean SHOW_BROWSER_WINDOW = false;
    protected static final boolean PAUSE_BEFORE_CLOSE = false;

    @Rule
    public final XvfbRule xvfb = XvfbRule.builder()
            .disabledOnWindows()
            .disabled(SHOW_BROWSER_WINDOW || PAUSE_BEFORE_CLOSE)
            .build();

}
