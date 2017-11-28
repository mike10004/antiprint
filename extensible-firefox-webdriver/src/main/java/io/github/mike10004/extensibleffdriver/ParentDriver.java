package io.github.mike10004.extensibleffdriver;

import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

interface ParentDriver {

    SessionId getSessionId();

    CommandExecutor getCommandExecutor();

    static ParentDriver fromRemoteDriver(RemoteWebDriver driver) {
        return new ParentDriver() {
            @Override
            public SessionId getSessionId() {
                return driver.getSessionId();
            }

            @Override
            public CommandExecutor getCommandExecutor() {
                return driver.getCommandExecutor();
            }
        };
    }
}
