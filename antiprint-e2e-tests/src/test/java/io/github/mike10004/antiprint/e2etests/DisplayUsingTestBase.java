package io.github.mike10004.antiprint.e2etests;

import com.github.mike10004.xvfbtesting.XvfbRule;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.BeforeClass;
import org.junit.Rule;

/**
 * Superclass for test classes that use a virtual framebuffer display.
 * Also provides some facility methods for convenience.
 */
public abstract class DisplayUsingTestBase {

    protected static final boolean SHOW_BROWSER_WINDOW = false;
    protected static final boolean PAUSE_BEFORE_CLOSE = false;
    protected static final int TIMEOUT_SECONDS = 15;

    @Rule
    public final XvfbRule xvfb = XvfbRule.builder()
            .disabledOnWindows()
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

}
