package io.github.mike10004.extensibleffdriver;

import io.github.mike10004.extensibleffdriver.ExtendedCommandExecutor.Commands;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class AddonSupportingDriver extends RemoteWebDriver {

    private final ParentDriver parentDriver;

    public AddonSupportingDriver(ParentDriver parentDriver, GeckoDriverService geckoDriverService, FirefoxOptions firefoxOptions) {
        super(new ExtendedCommandExecutor(geckoDriverService), CapabilitiesUtils.dropCapabilities(firefoxOptions));
        this.parentDriver = Objects.requireNonNull(parentDriver);
    }

    /**
     * Installs an addon.
     * @param request installation request parameters
     * @throws IOException
     */
    public void installAddon(AddonInstallRequest request) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("path", request.zipPathname.getAbsolutePath());
        params.put("temporary", request.duration == AddonPersistence.TEMPORARY);
        Command command = new Command(parentDriver.getSessionId(), Commands.INSTALL_ADDON, params);
        Response response = parentDriver.getCommandExecutor().execute(command);
        if (!isSuccess(response)) {
            throw new NonSuccessResponseException(response);
        }
    }

    /**
     * Uninstalls an addon.
     * @param request uninstallation request parameters
     * @throws IOException
     */
    public void uninstallAddon(AddonUninstallRequest request) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", request.id);
        Command command = new Command(parentDriver.getSessionId(), Commands.UNINSTALL_ADDON, params);
        Response response = parentDriver.getCommandExecutor().execute(command);
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
