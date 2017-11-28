package io.github.mike10004.extensibleffdriver;

import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.service.DriverCommandExecutor;
import org.openqa.selenium.remote.service.DriverService;

import java.util.HashMap;
import java.util.Map;

class ExtendedCommandExecutor extends DriverCommandExecutor {

    public ExtendedCommandExecutor(DriverService service) {
        super(service, buildAdditionalCommands());
    }

    public static Map<String, CommandInfo> buildAdditionalCommands() {
        Map<String, CommandInfo> map = new HashMap<>();
        CommandInfo installAddon = new CommandInfo("/session/:sessionId/moz/addon/install", HttpMethod.POST);
        map.put(ExtendedCommands.INSTALL_ADDON, installAddon);
        CommandInfo uninstallAddon = new CommandInfo("/session/:sessionId/moz/addon/uninstall", HttpMethod.POST);
        map.put(ExtendedCommands.UNINSTALL_ADDON, uninstallAddon);
        return map;
    }
}
