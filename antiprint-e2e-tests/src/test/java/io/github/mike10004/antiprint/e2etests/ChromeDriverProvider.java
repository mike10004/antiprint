package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.opencsv.CSVReader;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChromeDriverProvider implements WebDriverProvider<ChromeDriver> {

    public static final String SYSPROP_EXTRA_CHROME_ARGS = "antiprint.chrome.extraArgs";
    public static final String SYSPROP_CHROME_EXECUTABLE_PATH = "antiprint.chrome.executablePath";

    private ExtensionFileProvider crxProvider;

    @Nullable
    private final String userAgent;

    public ChromeDriverProvider() {
        this(null);
    }

    public ChromeDriverProvider(String userAgent) {
        this(ExtensionFileProvider.ofDependency(ExtensionFileFormat.CRX), userAgent);
    }

    public ChromeDriverProvider(ExtensionFileProvider crxProvider, @Nullable  String userAgent) {
        this.userAgent = userAgent;
        this.crxProvider = checkNotNull(crxProvider);
    }

    @Override
    public ChromeDriver provide(Map<String, String> environment) throws IOException {
        ChromeOptions options = new ChromeOptions();
        String[] extraChromeArgs = getExtraChromeArgs();
        options.addArguments(extraChromeArgs);
        if (userAgent != null) {
            options.addArguments("--user-agent=" + userAgent);
        }
        File crxFile = crxProvider.provide();
        options.addExtensions(crxFile);
        String executablePath = System.getProperty(SYSPROP_CHROME_EXECUTABLE_PATH);
        if (!Strings.isNullOrEmpty(executablePath)) {
            File executableFile = new File(executablePath);
            if (!executableFile.isFile()) {
                throw new FileNotFoundException(executablePath);
            }
            if (!executableFile.canExecute()) {
                throw new IOException("not executable: " + executableFile);
            }
            options.setBinary(executableFile);
        }
        ChromeDriverService cds = new ChromeDriverService.Builder()
                .withEnvironment(environment)
                .build();
        return new ChromeDriver(cds, options);
    }

    protected String[] getExtraChromeArgs() {
        String value = System.getProperty(SYSPROP_EXTRA_CHROME_ARGS, "").trim();
        final String[] args;
        if (!value.isEmpty()) {
            try {
                args = new CSVReader(new StringReader(value)).readNext();
                System.out.println("using extra chrome args: " + java.util.Arrays.toString(args));
                return args;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            args = new String[0];
        }
        return args;
    }
}
