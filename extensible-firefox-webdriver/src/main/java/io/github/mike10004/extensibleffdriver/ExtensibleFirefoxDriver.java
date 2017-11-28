package io.github.mike10004.extensibleffdriver;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.IOException;

/**
 * A Firefox webdriver implementation that supports unsigned addon installation.
 * This is an extension of {@link FirefoxDriver} that provides additional methods
 * {@link #installAddon(AddonInstallRequest)} and {@link #uninstallAddon(AddonUninstallRequest)}.
 * A private remote webdriver instance is used to send those custom commands.
 */
public class ExtensibleFirefoxDriver extends FirefoxDriver {

    private final AddonSupportingDriver addonSupportingDriver;

    public ExtensibleFirefoxDriver(GeckoDriverService service, FirefoxOptions options) {
        super(service, options);
        this.addonSupportingDriver = new AddonSupportingDriver(ParentDriver.fromRemoteDriver(this), service, options);
    }

    /**
     * Installs an addon.
     * @param request installation request parameters
     * @throws IOException
     */
    public void installAddon(AddonInstallRequest request) throws IOException {
        addonSupportingDriver.installAddon(request);
    }

    /**
     * Uninstalls an addon.
     * @param request uninstallation request parameters
     * @throws IOException
     */
    public void uninstallAddon(AddonUninstallRequest request) throws IOException {
        addonSupportingDriver.uninstallAddon(request);
    }

}
