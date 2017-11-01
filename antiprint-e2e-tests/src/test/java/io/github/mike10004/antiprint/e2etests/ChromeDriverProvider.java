package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChromeDriverProvider {

    private CrxProvider crxProvider;
    @Nullable
    private final String userAgent;

    public ChromeDriverProvider() {
        this(null);
    }

    public ChromeDriverProvider(String userAgent) {
        this(CrxProvider.ofDependency(), userAgent);
    }

    public ChromeDriverProvider(CrxProvider crxProvider, @Nullable  String userAgent) {
        this.userAgent = userAgent;
        this.crxProvider = checkNotNull(crxProvider);
    }

    public ChromeDriver provide() throws IOException {
        return provide(ImmutableMap.of());
    }

    public ChromeDriver provide(Map<String, String> environment) throws IOException {
        ChromeOptions options = new ChromeOptions();
        if (userAgent != null) {
            options.addArguments("--user-agent=" + userAgent);
        }
        File crxFile = crxProvider.provide();
        options.addExtensions(crxFile);
        ChromeDriverService cds = new ChromeDriverService.Builder()
                .withEnvironment(environment)
                .build();
        return new ChromeDriver(cds, options);
    }
}
