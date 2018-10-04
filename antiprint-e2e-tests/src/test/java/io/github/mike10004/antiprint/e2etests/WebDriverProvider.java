package io.github.mike10004.antiprint.e2etests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.util.Map;

public interface WebDriverProvider<W extends WebDriver> {

    default W provide(Map<String, String> environment) throws IOException {
        return provideBoth(environment).driver;
    }

    DriverPlusService<W> provideBoth(Map<String, String> environment) throws IOException;

    class DriverPlusService<W extends WebDriver> {

        public final W driver;
        public final DriverService service;

        public DriverPlusService(W driver, DriverService service) {
            this.driver = driver;
            this.service = service;
        }
    }
}
