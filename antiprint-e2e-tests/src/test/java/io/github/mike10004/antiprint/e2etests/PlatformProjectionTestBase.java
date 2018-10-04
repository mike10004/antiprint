package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class PlatformProjectionTestBase extends BrowserUsingTestBase<WebDriver, String> {

    protected void testNavigatorProperties(UserAgentFamily requiredUserAgentFamily, OperatingSystemFamily requiredOsFamily, NavigatorEvaluator evaluator) throws Exception {
        BrowserFingerprintTestCase testCase = Tests.getNavigatorTestCase(requiredUserAgentFamily, requiredOsFamily);
        System.out.format("%s/%s%nexpected navigator: %s%n", requiredUserAgentFamily, requiredOsFamily, Joiner.on(System.lineSeparator()).withKeyValueSeparator(" = ").join(testCase.output.window.navigator));
        String userAgent = testCase.input.userAgent;
        WebDriver driver = createWebDriver(userAgent);
        try {
            String html = Resources.asCharSource(getClass().getResource("/print-navigator.html"), UTF_8).read();
            NanoServer server = NanoServer.builder()
                    .get(request -> {
                        return NanoResponse.status(200).htmlUtf8(html);
                    }).build();
            // the extension is only active if the page URL is http[s]
            try (NanoControl control = server.startServer()) {
                driver.get(control.baseUri().toString());
                Map<String, Optional<Object>> jsResults = new HashMap<>();
                testCase.output.window.navigator.keySet().forEach(navigatorPropertyName -> {
                    String js = "return window.navigator." + navigatorPropertyName;
                    Object navigatorPropertyValue = ((JavascriptExecutor)driver).executeScript(js);
                    jsResults.put(navigatorPropertyName, Optional.ofNullable(navigatorPropertyValue));
                });
                evaluator.evaluate("js execution", testCase, jsResults);
                /*
                 * Content is written with document.write, so we don't have to
                 * use a WebDriverWait to poll the page
                 */
                String json = driver.findElements(By.id("content")).stream()
                        .map(WebElement::getText)
                        .filter(text -> !text.trim().isEmpty())
                        .findFirst().orElse(null);
                assertNotNull("div#content contents", json);
                Map<String, Optional<Object>> actual = parseNavigatorJson(json);
                maybePauseUntilKilled();
                evaluator.evaluate("json on page", testCase, actual);
            }
        } finally {
            driver.quit();
        }
    }

    private Map<String, Optional<Object>> parseNavigatorJson(String json) {
        JsonParser p = new JsonParser();
        JsonObject object = p.parse(json).getAsJsonObject();
        Map<String, Optional<Object>> parsed = new HashMap<>();
        object.entrySet().forEach(entry -> {
            Object value = Tests.deserialize(entry.getValue());
            parsed.put(entry.getKey(), Optional.ofNullable(value));
        });
        return parsed;
    }

    protected interface NavigatorEvaluator {
        void evaluate(String description, BrowserFingerprintTestCase testCase, Map<String, Optional<Object>> actual) throws Exception;
    }

    private static class Mismatch {
        public final String key;
        public final Predicate<?> predicate;
        public final Object actualValue;

        public Mismatch(String key, Predicate<?> predicate, Object actualValue) {
            this.key = key;
            this.predicate = predicate;
            this.actualValue = actualValue;
        }

        public String describe() {
            String actualValueStr = null;
            if (actualValue != null) {
                actualValueStr = '"' + StringEscapeUtils.escapeJava(actualValue.toString()) + '"';
            }
            return String.format("%s: predicate=%s; actual=%s", key, predicate, actualValueStr);
        }

        public String toString() {
            return String.format("Mismatch{key=%s}", key);
        }
    }

    protected static class DefaultEvaluator implements NavigatorEvaluator {
        @Override
        public void evaluate(String description, BrowserFingerprintTestCase testCase, Map<String, Optional<Object>> actual) throws Exception {
            List<Mismatch> mismatches = new ArrayList<>();
            //noinspection Java8MapForEach
            testCase.output.window.navigator.entrySet().forEach(entry -> {
                String k = entry.getKey();
                BrowserFingerprintTestCase.RequiredValue requirement = entry.getValue();
                System.out.format("%s = %s (expectation: %s)%n", k, actual.get(k), requirement.asPredicate());
                // This treats the absence of a value the same as a present null value, which is not ideal, but it'll do for now
                @Nullable Object actualValue = actual.get(k).orElse(null);
                @Nullable String actualValueStr = actualValue == null ? null : actualValue.toString();
                Predicate<String> predicate = requirement.asPredicate();
                boolean ok = predicate.test(actualValueStr);
                if (!ok) {
                    mismatches.add(new Mismatch(k, predicate, actualValue));
                }
            });
            mismatches.forEach(m -> System.out.format("MISMATCH: %s%n", m.describe()));
            assertEquals("expected zero navigator property mismatches in " + description, Collections.emptyList(), mismatches);
        }

    }

}