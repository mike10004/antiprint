package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.Joiner;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class PlatformProjectionTestBase extends BrowserUsingTestBase<WebDriver, String> {

    protected void testNavigatorProperties(UserAgentFamily requiredUserAgentFamily, OperatingSystemFamily requiredOsFamily) throws IOException, URISyntaxException, InterruptedException {
        Map<String, Object> navigator = Tests.getNavigatorTestCasesByUserAgent(userAgent -> {
            return userAgent.getFamily() == requiredUserAgentFamily
                    && userAgent.getOperatingSystem().getFamily() == requiredOsFamily;
        }).stream().findFirst().orElseThrow(() -> new IllegalArgumentException(requiredUserAgentFamily + "/" + requiredOsFamily));
        System.out.format("expected navigator: %s%n", Joiner.on(System.lineSeparator()).withKeyValueSeparator(" = ").join(navigator));
        String userAgent = navigator.get("userAgent").toString();
        byte[] html = Resources.toByteArray(getClass().getResource("/print-navigator.html"));
        NanoServer server = NanoServer.builder()
                .get(request -> {
                    return NanoResponse.status(200).content(MediaType.HTML_UTF_8, html).build();
                }).build();
        WebDriver driver = createWebDriver(userAgent);
        try {
            // the extension is only active if the page URL is http[s]
            try (NanoControl control = server.startServer()) {
                driver.get(control.baseUri().toString());
                Map<String, Object> jsResults = new HashMap<>();
                navigator.keySet().forEach(navigatorPropertyName -> {
                    String js = "return window.navigator." + navigatorPropertyName;
                    Object navigatorPropertyValue = ((JavascriptExecutor)driver).executeScript(js);
                    jsResults.put(navigatorPropertyName, navigatorPropertyValue);
                });
                assertNavigatorsEqual("js execution", navigator, jsResults);
                /*
                 * Content is written with document.write, so we don't have to
                 * use a WebDriverWait to poll the page
                 */
                String json = driver.findElements(By.id("content")).stream()
                        .map(WebElement::getText)
                        .filter(text -> !text.trim().isEmpty())
                        .findFirst().orElse(null);
                assertNotNull("div#content contents", json);
                Map<String, Object> actual = Tests.navigatorObjectLoader().apply(CharSource.wrap(json));
                maybePauseUntilKilled();
                assertNavigatorsEqual("json on page", navigator, actual);
            }
        } finally {
            driver.quit();
        }
    }

    private static void assertNavigatorsEqual(String source, Map<String, Object> expected, Map<String, Object> actual) {
        List<Map.Entry<String, Object>> mismatches = new ArrayList<>();
        expected.forEach((k, v) -> {
            System.out.format("%s = %s%n", k, actual.get(k));
            if (isImportant(k) && !Objects.equals(expected.get(k), actual.get(k))) {
                mismatches.add(actual.entrySet().stream().filter(e -> k.equals(e.getKey())).findFirst().orElseThrow(IllegalStateException::new));
            }
        });
        assertEquals("expected zero navigator property mismatches in " + source, Collections.emptyList(), mismatches);
    }

    private static boolean isImportant(String navigatorProperty) {
        return "platform".equals(navigatorProperty);
    }

}