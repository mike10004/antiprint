package io.github.mike10004.antiprint.e2etests;

import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoServer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Demos {

    public static class ChromeDemo {

        public static void main(String[] args) throws Exception {
            Tests.setUpChromedriver();
            String chromeOnWindowsUserAgent = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";
            openBrowser(new ChromeDriverProvider(chromeOnWindowsUserAgent));
        }

    }

    private static void openBrowser(WebDriverProvider<?> provider) throws Exception {
        WebDriverProvider.DriverPlusService<?> both = provider.provideBoth(new HashMap<>());
        CountDownLatch latch = new CountDownLatch(1);
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("driver-service-listener-%s")
                .build();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(threadFactory);
        executorService.scheduleWithFixedDelay(() -> {
            if (!both.service.isRunning()) {
                latch.countDown();
            }
        }, 1, 1, TimeUnit.SECONDS);
        NanoServer server = PlatformProjectionTestBase.buildPrintNavigatorServer();
        File file = File.createTempFile("start-page", ".html");
        file.deleteOnExit();

        try (NanoControl serverCtrl = server.startServer()) {
            String serverUrl = serverCtrl.baseUri().toString();
            String template = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "  <body>\n" +
                    "    <a href=\"%s\">%s</a>\n" +
                    "  </body>\n" +
                    "</html>\n";
            String html = String.format(template, serverUrl, serverUrl);
            Files.asCharSink(file, StandardCharsets.UTF_8).write(html);
            String url = file.toURI().toString();
            both.driver.get(url);
            latch.await();
        } finally {
            try {
                both.driver.quit();
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        System.out.println("exiting clean");
    }

    public static class FirefoxDemo {

        public static void main(String[] args) throws Exception {
            Tests.setUpGeckodriver();
            String firefoxOnWindowsUserAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";
            openBrowser(new FirefoxDriverProvider(firefoxOnWindowsUserAgent));
        }

    }
}
