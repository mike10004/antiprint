package io.github.mike10004.extensibleffdriver;

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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openqa.selenium.firefox.FirefoxDriver.BINARY;
import static org.openqa.selenium.firefox.FirefoxDriver.MARIONETTE;
import static org.openqa.selenium.firefox.FirefoxDriver.PROFILE;
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
public class ExtensibleFirefoxDriver extends RemoteWebDriver {

    protected FirefoxBinary binary;

    public ExtensibleFirefoxDriver(GeckoDriverService service) {
        this(service, new FirefoxOptions());
    }

    public ExtensibleFirefoxDriver(GeckoDriverService service, FirefoxOptions options) {
        this(new ExtendedCommandExecutor(service), options);
    }

    protected ExtensibleFirefoxDriver(CommandExecutor executor, FirefoxOptions options) {
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

    /**
     * Installs an addon.
     * @param request installation request parameters
     * @throws IOException
     */
    public void installAddon(AddonInstallation request) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("path", request.zipPathname.getAbsolutePath());
        params.put("temporary", request.duration == AddonPersistence.TEMPORARY);
        Command command = new Command(getSessionId(), ExtendedCommands.INSTALL_ADDON, params);
        Response response = getCommandExecutor().execute(command);
        if (!isSuccess(response)) {
            throw new NonSuccessResponseException(response);
        }
    }

    /**
     * Uninstalls an addon.
     * @param request uninstallation request parameters
     * @throws IOException
     */
    public void uninstallAddon(AddonUninstallation request) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", request.id);
        Command command = new Command(getSessionId(), ExtendedCommands.UNINSTALL_ADDON, params);
        Response response = getCommandExecutor().execute(command);
        if (!isSuccess(response)) {
            throw new NonSuccessResponseException(response);
        }
    }

    private boolean isSuccess(Response response) {
        return "success".equals(response.getState());
    }

    static class NonSuccessResponseException extends IOException {
        public NonSuccessResponseException(Response response) {
            super(String.format("status %s in session %s: %s", response.getStatus(), response.getSessionId(), response.getState()));
        }
    }

}
