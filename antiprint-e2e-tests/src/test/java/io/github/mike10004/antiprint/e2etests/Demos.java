package io.github.mike10004.antiprint.e2etests;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;

public class Demos {

    public static class ChromeDemo {

        public static void main(String[] args) throws Exception {
            Tests.setUpChromedriver();
            String chromeOnWindowsUserAgent = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";
            ChromeDriver driver = new ChromeDriverProvider(chromeOnWindowsUserAgent).provide(new HashMap<>());
            try {
                new java.util.concurrent.CountDownLatch(1).await();
            } finally {
                driver.quit();
            }
        }

    }

    public static class FirefoxDemo {

        public static void main(String[] args) throws Exception {
            Tests.setUpGeckodriver();
            String firefoxOnWindowsUserAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";
            FirefoxDriver driver = new FirefoxDriverProvider(firefoxOnWindowsUserAgent).provide(new HashMap<>());
            try {
                new java.util.concurrent.CountDownLatch(1).await();
            } finally {
                driver.quit();
            }
        }

    }
}
