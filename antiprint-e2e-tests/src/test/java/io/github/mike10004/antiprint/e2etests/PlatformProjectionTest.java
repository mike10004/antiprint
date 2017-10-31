package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class PlatformProjectionTest extends BrowserUsingTestBase {

    private UserAgentFamily requiredUserAgentFamily;
    private OperatingSystemFamily requiredOsFamily;

    public PlatformProjectionTest(UserAgentFamily userAgentFamily, OperatingSystemFamily osFamily) {
        this.requiredUserAgentFamily = userAgentFamily;
        this.requiredOsFamily = osFamily;
    }

    @Parameters
    public static List<Object[]> parametersList() {
        return ImmutableList.<Object[]>builder()
                .add(new Object[]{UserAgentFamily.CHROME, OperatingSystemFamily.WINDOWS})
                .add(new Object[]{UserAgentFamily.CHROME, OperatingSystemFamily.OS_X})
                .add(new Object[]{UserAgentFamily.CHROME, OperatingSystemFamily.LINUX})
                .build();
    }

    @Test
    public void navigatorProperties() throws IOException, URISyntaxException, InterruptedException {
        Map<String, Object> navigator = Tests.getNavigatorTestCasesByUserAgent(userAgent -> {
            return userAgent.getFamily() == requiredUserAgentFamily
                    && userAgent.getOperatingSystem().getFamily() == requiredOsFamily;
        }).stream().findFirst().orElseThrow(() -> new IllegalArgumentException(requiredUserAgentFamily + "/" + requiredOsFamily));
        System.out.println(navigator);
        String userAgent = navigator.get("userAgent").toString();
        byte[] html = Resources.toByteArray(getClass().getResource("/print-navigator.html"));
        NanoServer server = NanoServer.builder()
                .get(request -> {
                    return NanoResponse.status(200).content(MediaType.HTML_UTF_8, html).build();
                }).build();
        ChromeDriver driver = new ChromeDriverProvider().provide(xvfb.getController().newEnvironment(), userAgent);
        try {
            // the extension is only active if the page URL is http[s]
            try (NanoControl control = server.startServer()) {
                driver.get(control.buildUri().build().toString());
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
                if (PAUSE_BEFORE_CLOSE) {
                    synchronized (this) { wait(); }
                }
                navigator.forEach((k, v) -> {
                    System.out.format("%s = %s%n", k, actual.get(k));
                    if (isImportant(k)) {
                        assertEquals("property: " + k, navigator.get(k), actual.get(k));
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

}