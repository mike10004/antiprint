// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.mike10004.antiprint.e2etests.CustomFirefoxDriver.AddonInstallation.AddonDuration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.service.DriverCommandExecutor;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openqa.selenium.firefox.FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE;
import static org.openqa.selenium.remote.CapabilityType.PROXY;

/**
 * An implementation of the {#link WebDriver} interface that drives Firefox.
 * <p>
 * The best way to construct a {@code FirefoxDriver} with various options is to make use of the
 * {@link FirefoxOptions}, like so:
 *
 * <pre>
 *FirefoxOptions options = new FirefoxOptions()
 *    .setProfile(new FirefoxProfile());
 *WebDriver driver = new FirefoxDriver(options);
 * </pre>
 */
public class CustomFirefoxDriver extends RemoteWebDriver {

    public static final class SystemProperty {

        /**
         * System property that defines the location of the Firefox executable file.
         */
        public static final String BROWSER_BINARY = "webdriver.firefox.bin";

        /**
         * System property that defines the location of the file where Firefox log should be stored.
         */
        public static final String BROWSER_LOGFILE = "webdriver.firefox.logfile";

        /**
         * System property that defines the additional library path (Linux only).
         */
        public static final String BROWSER_LIBRARY_PATH = "webdriver.firefox.library.path";

        /**
         * System property that defines the profile that should be used as a template.
         * When the driver starts, it will make a copy of the profile it is using,
         * rather than using that profile directly.
         */
        public static final String BROWSER_PROFILE = "webdriver.firefox.profile";

        /**
         * System property that defines the location of the webdriver.xpi browser extension to install
         * in the browser. If not set, the prebuilt extension bundled with this class will be used.
         */
        public static final String DRIVER_XPI_PROPERTY = "webdriver.firefox.driver";

        /**
         * Boolean system property that instructs FirefoxDriver to use Marionette backend,
         * overrides any capabilities specified by the user
         */
        public static final String DRIVER_USE_MARIONETTE = "webdriver.firefox.marionette";
    }

    public static final String BINARY = "firefox_binary";
    public static final String PROFILE = "firefox_profile";
    public static final String MARIONETTE = "marionette";

    protected FirefoxBinary binary;

    public CustomFirefoxDriver(GeckoDriverService service) {
        this(new BetterCommandExecutor(service), new FirefoxOptions());
    }

    public CustomFirefoxDriver(GeckoDriverService service, FirefoxOptions options) {
        this(new BetterCommandExecutor(service), options);
    }

    protected CustomFirefoxDriver(CommandExecutor executor, FirefoxOptions options) {
        super(executor, dropCapabilities(options));
    }

    @Override
    public void setFileDetector(FileDetector detector) {
        throw new WebDriverException(
                "Setting the file detector only works on remote webdriver instances obtained " +
                        "via RemoteWebDriver");
    }

    private static boolean isLegacy(Capabilities desiredCapabilities) {
        Boolean forceMarionette = forceMarionetteFromSystemProperty();
        if (forceMarionette != null) {
            return !forceMarionette;
        }
        Object marionette = desiredCapabilities.getCapability(MARIONETTE);
        return marionette instanceof Boolean && ! (Boolean) marionette;
    }

    private static Boolean forceMarionetteFromSystemProperty() {
        String useMarionette = System.getProperty(DRIVER_USE_MARIONETTE);
        if (useMarionette == null) {
            return null;
        }
        return Boolean.valueOf(useMarionette);
    }

    /**
     * Drops capabilities that we shouldn't send over the wire.
     *
     * Used for capabilities which aren't BeanToJson-convertable, and are only used by the local
     * launcher.
     */
    private static Capabilities dropCapabilities(Capabilities capabilities) {
        if (capabilities == null) {
            return new ImmutableCapabilities();
        }

        MutableCapabilities caps;

        if (isLegacy(capabilities)) {
            final Set<String> toRemove = Sets.newHashSet(BINARY, PROFILE);
            caps = new MutableCapabilities(
                    Maps.filterKeys(capabilities.asMap(), key -> !toRemove.contains(key)));
        } else {
            caps = new MutableCapabilities(capabilities);
        }

        // Ensure that the proxy is in a state fit to be sent to the extension
        Proxy proxy = Proxy.extractFrom(capabilities);
        if (proxy != null) {
            caps.setCapability(PROXY, proxy);
        }

        return caps;
    }

    public void installAddon(AddonInstallation request) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("path", request.zipPathname.getAbsolutePath());
        params.put("temporary", request.duration == AddonDuration.TEMPORARY);
        Command command = new Command(getSessionId(), MoreCommands.INSTALL_ADDON, params);
        Response response = getCommandExecutor().execute(command);
        if (!isSuccess(response)) {
            throw new AddonInstallationException(response);
        }
    }

    private boolean isSuccess(Response response) {
        return "success".equals(response.getState());
    }

    private static class AddonInstallationException extends IOException {
        public AddonInstallationException(Response response) {
            super(String.format("status %s in session %s: %s", response.getStatus(), response.getSessionId(), response.getState()));
        }
    }

    public static class AddonInstallation {
        public final File zipPathname;
        public final AddonDuration duration;

        public AddonInstallation(File zipPathname, AddonDuration duration) {
            this.zipPathname = zipPathname;
            this.duration = duration;
        }

        public enum AddonDuration {
            TEMPORARY, PERMANENT
        }
    }

    private interface MoreCommands {
        String INSTALL_ADDON = "installAddon";
        String UNINSTALL_ADDON = "uninstallAddon";
    }

    public static class BetterCommandExecutor extends DriverCommandExecutor {

        public BetterCommandExecutor(DriverService service) {
            super(service, buildAdditionalCommands());
        }

        public static Map<String, CommandInfo> buildAdditionalCommands() {
            Map<String, CommandInfo> map = new HashMap<>();
            CommandInfo installAddon = new CommandInfo("/session/:sessionId/moz/addon/install", HttpMethod.POST);
            map.put(MoreCommands.INSTALL_ADDON, installAddon);
            CommandInfo uninstallAddon = new CommandInfo("/session/:sessionId/moz/addon/uninstall", HttpMethod.POST);
            map.put(MoreCommands.UNINSTALL_ADDON, uninstallAddon);
            return map;
        }
    }
}
