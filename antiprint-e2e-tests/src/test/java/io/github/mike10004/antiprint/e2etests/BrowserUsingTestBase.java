package io.github.mike10004.antiprint.e2etests;

import com.github.mike10004.xvfbtesting.XvfbRule;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.BeforeClass;
import org.junit.Rule;

public class BrowserUsingTestBase {

    protected static final boolean SHOW_BROWSER_WINDOW = false;
    protected static final boolean PAUSE_BEFORE_CLOSE = false;
    protected static final int TIMEOUT_SECONDS = 15;

    @Rule
    public final XvfbRule xvfb = XvfbRule.builder()
            .disabledOnWindows()
            .disabled(SHOW_BROWSER_WINDOW || PAUSE_BEFORE_CLOSE)
            .build();

    protected void maybePauseUntilKilled() {
        if (PAUSE_BEFORE_CLOSE) {
            Object local = new Object();
            try {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (local) {
                    local.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
