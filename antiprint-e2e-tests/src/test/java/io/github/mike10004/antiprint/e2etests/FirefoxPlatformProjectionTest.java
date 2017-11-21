package io.github.mike10004.antiprint.e2etests;

import com.google.common.io.ByteStreams;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.mike10004.crxtool.CrxParser;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class FirefoxPlatformProjectionTest extends PlatformProjectionTestBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        FirefoxDriverManager.getInstance().version(Tests.geckodriverVersion()).setup();
    }

    protected File prepareZipFile() throws IOException {
        File zipFile = File.createTempFile("antiprint-extension-firefox", ".zip");
        File crxFile = CrxProvider.ofDependency().provide();
        try (InputStream crxStream = new FileInputStream(crxFile)) {
            CrxParser.getDefault().parseMetadata(crxStream);
            try (OutputStream out = new FileOutputStream(zipFile)) {
                ByteStreams.copy(crxStream, out);
            }
        }
        return zipFile;
    }

    @Override
    protected WebDriver createDriver(String userAgent, Map<String, String> environment) throws IOException {
        FirefoxProfile profile = new FirefoxProfile();
        File extensionZipFile = prepareZipFile();
        profile.addExtension(extensionZipFile);
        FirefoxOptions options = new FirefoxOptions();
        System.out.format("setting user agent to %s%n", userAgent);
        options.addPreference("general.useragent.override", userAgent);
        options.setProfile(profile);

        GeckoDriverService service = new GeckoDriverService.Builder()
                .withEnvironment(environment)
                .build();
        FirefoxDriver driver = new FirefoxDriver(service, options);
        return driver;
    }

    @Test
    public void windows() throws Exception {
        testNavigatorProperties(UserAgentFamily.FIREFOX, OperatingSystemFamily.WINDOWS);
    }

    @Test
    public void osx() throws Exception {
        testNavigatorProperties(UserAgentFamily.FIREFOX, OperatingSystemFamily.OS_X);
    }
    @Test
    public void linux() throws Exception {
        testNavigatorProperties(UserAgentFamily.FIREFOX, OperatingSystemFamily.LINUX);
    }
}
