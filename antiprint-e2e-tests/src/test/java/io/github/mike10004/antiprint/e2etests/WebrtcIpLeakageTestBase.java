package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.CharMatcher;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import com.google.common.util.concurrent.Uninterruptibles;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public abstract class WebrtcIpLeakageTestBase extends BrowserUsingTestBase<WebDriver, Void> {

    @Rule
    public Timeout timeout = Timeout.seconds(TIMEOUT_SECONDS);

    private interface Fixture<T extends Closeable> {
        T startServer() throws IOException;
        void visitPage(T serverControl, WebDriver driver) throws MalformedURLException, URISyntaxException;
    }

    @org.junit.Ignore
    @Test
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
            public void visitPage(NanoControl control, WebDriver driver) throws MalformedURLException, URISyntaxException {
                driver.get(control.buildUri().build().toURL().toString());
            }
        });
    }

    @Test
    public void confirmNoLeakage_remote() throws Exception {
        confirmNoLeakage(new Fixture<Closeable>() {

            @Override
            public Closeable startServer() throws IOException {
                return () -> {};
            }

            @Override
            public void visitPage(Closeable serverControl, WebDriver driver) throws MalformedURLException, URISyntaxException {
                /*
                 * This is dicey. The browser extension executes an async operation to change the WebRTC privacy
                 * setting, and we want to wait for that operation to complete before testing a page. However,
                 * we have no guarantee about how long it should take. It's possible in Chrome to visit an
                 * extension page poll the contents that are updated when the async operation completes, but
                 * that is not possible in Firefox because the moz-extensions://${UUID} is intentionally
                 * not knowable beforehand.
                 */
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                driver.get(new URL(WEBRTC_REFLECTION_URL).toString());
            }
        });
    }

    static final String WEBRTC_REFLECTION_URL =  "https://mike10004.github.io/webrtc-ips/";

    private <T extends Closeable> void confirmNoLeakage(Fixture<T> fixture) throws Exception {
        WebDriver driver = createWebDriver(null);
        String rawInfo;
        try (T control = fixture.startServer()) {
            try {
                fixture.visitPage(control, driver);
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
                .flatMap(line -> Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings().splitToList(line).stream())
                .filter(token -> InetAddressValidator.getInstance().isValid(token))
                .collect(Collectors.toList());
        Predicate<String> allowedIpAddresses = Predicates.in(ImmutableSet.of("127.0.0.1", "0.0.0.0", "127.0.1.1"));
        assertEquals("ip address in info", Collections.emptyList(), ipAddresses.stream().filter(allowedIpAddresses.negate()).collect(Collectors.toList()));
    }
}
