package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class WebRtcIpLeakageTest extends BrowserUsingTestBase {

    @Test(timeout = 15000)
    public void confirmNoLeakage() throws Exception {
        ChromeDriver driver = new ChromeDriverProvider().provide(xvfb.getController().newEnvironment());
        String rawInfo;
        byte[] pageHtmlBytes = Resources.toByteArray(getClass().getResource("/leak-ip-thru-webrtc.html"));
        NanoServer server = NanoServer.builder()
                .get(NanoResponse.status(200).content(MediaType.HTML_UTF_8, pageHtmlBytes).build())
                .build();
        try (NanoControl control = server.startServer()) {
            try {
                URL url = control.buildUri().build().toURL();
                driver.get(url.toString());
                rawInfo = new WebDriverWait(driver, 5).until(driver_ -> {
                    return Strings.emptyToNull(driver_.findElements(By.id("raw")).stream().map(WebElement::getText).findFirst().orElse(null));
                });
                maybePauseUntilKilled();
            } finally {
                driver.quit();
            }
        }
        System.out.println(rawInfo);
        List<String> ipAddresses = CharSource.wrap(rawInfo).readLines().stream()
                .filter(line -> line.startsWith("a=candidate:"))
                .flatMap(line -> Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings().splitToList(line).stream())
                .filter(token -> InetAddressValidator.getInstance().isValid(token))
                .collect(Collectors.toList());
        assertEquals("ip address in info", Collections.emptyList(), ipAddresses);
    }
}
