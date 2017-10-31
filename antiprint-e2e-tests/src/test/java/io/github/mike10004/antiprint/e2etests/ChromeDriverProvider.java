package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ChromeDriverProvider {

    private final String userAgent;

    public ChromeDriverProvider() {
        this(null);
    }

    public ChromeDriverProvider(String userAgent) {
        this.userAgent = userAgent;
    }

    public ChromeDriver provide() throws IOException {
        return provide(ImmutableMap.of());
    }

    public ChromeDriver provide(Map<String, String> environment) throws IOException {
        ChromeOptions options = new ChromeOptions();
        if (userAgent != null) {
            options.addArguments("--user-agent=" + userAgent);
        }
        File crxFile = new CrxProvider().provide();
        options.addExtensions(crxFile);
        ChromeDriverService cds = new ChromeDriverService.Builder()
                .withEnvironment(environment)
                .build();
        return new ChromeDriver(cds, options);
    }
}
