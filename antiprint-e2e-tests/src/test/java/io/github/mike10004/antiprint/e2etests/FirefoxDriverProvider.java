package io.github.mike10004.antiprint.e2etests;

import io.github.mike10004.extensibleffdriver.AddonInstallRequest;
import io.github.mike10004.extensibleffdriver.AddonPersistence;
import io.github.mike10004.extensibleffdriver.ExtensibleFirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FirefoxDriverProvider implements WebDriverProvider<ExtensibleFirefoxDriver> {

    private final String userAgent;

    public FirefoxDriverProvider() {
        this(null);
    }

    public FirefoxDriverProvider(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public DriverPlusService<ExtensibleFirefoxDriver> provideBoth(Map<String, String> environment) throws IOException {
        FirefoxProfile profile = new FirefoxProfile();
        File extensionZipFile = ExtensionFileProvider.ofDependency(ExtensionFileFormat.ZIP).provide();
        FirefoxOptions options = new FirefoxOptions();
        if (userAgent != null) {
            options.addPreference("general.useragent.override", userAgent);
        }
        options.setProfile(profile);
        GeckoDriverService service = new GeckoDriverService.Builder()
                .usingAnyFreePort()
                .withEnvironment(environment)
                .build();
        ExtensibleFirefoxDriver driver = new ExtensibleFirefoxDriver(service, options);
        driver.installAddon(AddonInstallRequest.fromFile(extensionZipFile, AddonPersistence.TEMPORARY));
        return new DriverPlusService<>(driver, service);
    }


}
