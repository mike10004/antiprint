package io.github.mike10004.antiprint.e2etests;

import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Map;

public interface WebDriverProvider<W extends WebDriver> {

    W provide(Map<String, String> environment) throws IOException;

}
