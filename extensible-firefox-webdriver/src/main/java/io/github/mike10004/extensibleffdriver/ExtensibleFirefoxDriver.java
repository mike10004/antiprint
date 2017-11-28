package io.github.mike10004.extensibleffdriver;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.IOException;

/**
 * A Firefox webdriver implementation that supports unsigned addon installation.
 * This is an extension of {@link FirefoxDriver} that provides additional methods
 * {@link #installAddon(AddonInstallRequest)} and {@link #uninstallAddon(AddonUninstallRequest)}.
 * A special command executor is created to handle the install/uninstall, and it is
 * less tolerant to strange inputs, so this may only work in limited cases.
 */
public class ExtensibleFirefoxDriver extends FirefoxDriver {

    private final AddonSupport addonSupport;

    public ExtensibleFirefoxDriver(GeckoDriverService service, FirefoxOptions options) {
        super(service, options);
        this.addonSupport = new AddonSupport(this::getSessionId, service);
    }

    /**
     * Installs an addon.
     * @param request installation request parameters
     * @throws IOException
     */
    public void installAddon(AddonInstallRequest request) throws IOException {
        addonSupport.installAddon(request);
    }

    /**
     * Uninstalls an addon.
     * @param request uninstallation request parameters
     * @throws IOException
     */
    public void uninstallAddon(AddonUninstallRequest request) throws IOException {
        addonSupport.uninstallAddon(request);
    }

}
