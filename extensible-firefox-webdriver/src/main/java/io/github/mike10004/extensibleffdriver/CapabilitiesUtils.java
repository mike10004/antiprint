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

package io.github.mike10004.extensibleffdriver;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;

import java.util.Set;

import static org.openqa.selenium.firefox.FirefoxDriver.BINARY;
import static org.openqa.selenium.firefox.FirefoxDriver.MARIONETTE;
import static org.openqa.selenium.firefox.FirefoxDriver.PROFILE;
import static org.openqa.selenium.firefox.FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE;
import static org.openqa.selenium.remote.CapabilityType.PROXY;

/**
 * Static utility methods borrowed from Selenium Java {@code FirefoxDriver}.
 */
class CapabilitiesUtils {

    private static boolean isLegacy(Capabilities desiredCapabilities) {
        Boolean forceMarionette = forceMarionetteFromSystemProperty();
        if (forceMarionette != null) {
            return !forceMarionette;
        }
        Object marionette = desiredCapabilities.getCapability(MARIONETTE);
        return marionette instanceof Boolean && !(Boolean) marionette;
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
     * <p>
     * Used for capabilities which aren't BeanToJson-convertable, and are only used by the local
     * launcher.
     */
    public static Capabilities dropCapabilities(Capabilities capabilities) {
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
}
