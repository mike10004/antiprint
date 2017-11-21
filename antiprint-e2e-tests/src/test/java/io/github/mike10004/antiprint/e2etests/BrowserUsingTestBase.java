package io.github.mike10004.antiprint.e2etests;

import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Map;

public abstract class BrowserUsingTestBase<W extends WebDriver, P> extends DisplayUsingTestBase {

    protected abstract WebDriverProvider<? extends W> getWebDriverProvider(P parametry);

    protected W createWebDriver(P parametry) throws IOException {
        Map<String, String> environment = xvfb.getController().newEnvironment();
        return getWebDriverProvider(parametry).provide(environment);
    }

}
