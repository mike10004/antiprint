package io.github.mike10004.antiprint.e2etests;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;

public class ChromeDriverProvider {

    public ChromeDriver provide(String userAgent) throws IOException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent=" + userAgent);
        File crxFile = new CrxProvider().provide();
        options.addExtensions(crxFile);
        return new ChromeDriver(options);
    }
}
