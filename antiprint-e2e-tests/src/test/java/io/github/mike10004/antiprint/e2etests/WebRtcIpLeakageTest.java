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

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class WebRtcIpLeakageTest extends BrowserUsingTestBase {

    private interface Fixture<T extends Closeable> {
        T startServer() throws IOException;
        URL getUrl(T serverControl) throws MalformedURLException, URISyntaxException;
    }

    @Test(timeout = 10000)
    public void confirmNoLeakage_local() throws Exception {
        byte[] pageHtmlBytes = Resources.toByteArray(getClass().getResource("/leak-ip-thru-webrtc.html"));
        NanoServer server = NanoServer.builder()
                .get(NanoResponse.status(200).content(MediaType.HTML_UTF_8, pageHtmlBytes).build())
                .build();
        confirmNoLeakage(new Fixture<NanoControl>() {

            @Override
            public NanoControl startServer() throws IOException {
                return server.startServer();
            }

            @Override
            public URL getUrl(NanoControl control) throws MalformedURLException, URISyntaxException {
                return control.buildUri().build().toURL();
            }
        });
    }

    @org.junit.Ignore // our network filters this URL, but in theory it should work
    @Test(timeout = 15000)
    public void confirmNoLeakage_remote() throws Exception {
        confirmNoLeakage(new Fixture<Closeable>() {

            @Override
            public Closeable startServer() throws IOException {
                return () -> {};
            }

            @Override
            public URL getUrl(Closeable control) throws MalformedURLException, URISyntaxException {
                return new URL("https://mike10004.github.io/webrtc-ips/");
            }
        });
    }

    private <T extends Closeable> void confirmNoLeakage(Fixture<T> fixture) throws Exception {
        ChromeDriver driver = new ChromeDriverProvider().provide(xvfb.getController().newEnvironment());
        String rawInfo;
        try (T control = fixture.startServer()) {
            try {
                URL url = fixture.getUrl(control);
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
