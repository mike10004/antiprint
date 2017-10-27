package io.github.mike10004.antiprint.e2etests;

import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmokeTest {

    @BeforeAll
    static void setUpChromeWebdriver() {
        ChromeDriverManager.getInstance().setup();
    }

    @ParameterizedTest
//    @ValueSource(strings = {"CHROME/WINDOWS", "CHROME/LINUX", "CHROME/OS_X"})
    @ValueSource(strings = {"CHROME/WINDOWS"})
    void navigatorProperties(String agentFamilySlashOsFamily) throws IOException, URISyntaxException, InterruptedException {
        UserAgentFamily requiredUserAgentFamily = UserAgentFamily.valueOf(agentFamilySlashOsFamily.split("/")[0]);
        OperatingSystemFamily requiredOsFamily = OperatingSystemFamily.valueOf(agentFamilySlashOsFamily.split("/")[1]);
        Map<String, Object> navigator = Tests.getNavigatorTestCasesByUserAgent(userAgent -> {
            return userAgent.getFamily() == requiredUserAgentFamily
                    && userAgent.getOperatingSystem().getFamily() == requiredOsFamily;
        }).stream().findFirst().orElseThrow(() -> new IllegalArgumentException(agentFamilySlashOsFamily));
        System.out.println(navigator);
        String userAgent = navigator.get("userAgent").toString();
        byte[] html = Resources.toByteArray(getClass().getResource("/print-navigator.html"));
        NanoServer server = NanoServer.builder()
                .get(request -> {
                    return NanoResponse.status(200).content(MediaType.HTML_UTF_8, html).build();
                }).build();
        ChromeDriver driver = new ChromeDriverProvider().provide(userAgent);
        try {
            // the extension is only active if the page URL is http[s]
            try (NanoControl control = server.startServer()) {
                driver.get(control.buildUri().build().toString());
                String json = new WebDriverWait(driver, 3).until(driver_ -> {
                    return driver_.findElements(By.id("content")).stream()
                            .map(WebElement::getText)
                            .filter(text -> !text.trim().isEmpty())
                            .findFirst().orElse(null);
                });
                Map<String, Object> actual = Tests.navigatorObjectLoader().apply(CharSource.wrap(json));
                navigator.forEach((k, v) -> {
                    System.out.format("%s = %s%n", k, actual.get(k));
                    if (isImportant(k)) {
                        assertEquals(navigator.get(k), actual.get(k), "property: " + k);
                    }
                });
            }
        } finally {
            driver.quit();
        }
    }

    private static boolean isImportant(String navigatorProperty) {
        return "platform".equals(navigatorProperty);
    }

//    private Object getNavigatorPropertyUsingScript(ChromeDriver driver, String property) {
//        checkArgument(CharMatcher.javaLetterOrDigit().matchesAllOf(property), "illegal property: %s", property);
////        Object value = driver.executeScript("return window.navigator['" + property + "'];");
////        return value;
//        driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);
//        Object value = driver.executeAsyncScript("const callback = arguments[arguments.length - 1]; " +
//                "window.setTimeout(function() { " +
//                "  callback(window.navigator['" + property + "']);" +
//                "}, 1);");
//        return value;
//    }
}