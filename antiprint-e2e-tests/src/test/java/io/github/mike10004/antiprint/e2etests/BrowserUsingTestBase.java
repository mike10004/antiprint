package io.github.mike10004.antiprint.e2etests;

import com.github.mike10004.xvfbtesting.XvfbRule;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Map;

public abstract class BrowserUsingTestBase<W extends WebDriver, P> {

    protected static final boolean SHOW_BROWSER_WINDOW = false;
    protected static final boolean PAUSE_BEFORE_CLOSE = false;
    protected static final int TIMEOUT_SECONDS = 15;

    @Rule
    public final XvfbRule xvfb = XvfbRule.builder()
            .disabled(SHOW_BROWSER_WINDOW || PAUSE_BEFORE_CLOSE)
            .build();

    /**
     * Calls {@link Object#wait()} on a local reference, effectively halting on this
     * thread until the process is killed.
     */
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

    protected abstract WebDriverProvider<? extends W> getWebDriverProvider(P parametry);

    protected W createWebDriver(P parametry) throws IOException {
        Map<String, String> environment = xvfb.getController().newEnvironment();
        return getWebDriverProvider(parametry).provide(environment);
    }

}
