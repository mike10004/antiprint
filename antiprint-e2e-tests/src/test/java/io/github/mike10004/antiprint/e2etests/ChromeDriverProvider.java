package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ChromeDriverProvider {

    public ChromeDriver provide(String userAgent) throws IOException {
        return provide(ImmutableMap.of(), userAgent);
    }

    public ChromeDriver provide(Map<String, String> environment, String userAgent) throws IOException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent=" + userAgent);
        File crxFile = new CrxProvider().provide();
        options.addExtensions(crxFile);
        ChromeDriverService cds = new ChromeDriverService.Builder()
                .withEnvironment(environment)
                .build();
        return new ChromeDriver(cds, options);
    }
}
