package io.github.mike10004.extensibleffdriver;

import com.github.mike10004.xvfbtesting.XvfbRule;
import com.google.common.net.MediaType;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExtensibleFirefoxDriverTest {

    @Rule
    public XvfbRule xvfb = XvfbRule.builder().build();

    @BeforeClass
    public static void setUpGeckodriver() {
        FirefoxDriverManager.getInstance().version("0.19.1").setup();
    }

    @Test
    public void installAddon_unsigned_temporary_file() throws Exception {
        File sampleExtensionZipFile = buildSampleExtension();
        AddonInstallRequest installRequest = AddonInstallRequest.fromFile(sampleExtensionZipFile, AddonPersistence.TEMPORARY);
        installAddon_unsigned_temporary(installRequest);
    }

    @Test
    public void installAddon_unsigned_temporary_base64() throws Exception {
        File sampleExtensionZipFile = buildSampleExtension();
        byte[] extensionBytes = Files.readAllBytes(sampleExtensionZipFile.toPath());
        String extensionBase64 = Base64.getEncoder().encodeToString(extensionBytes);
        AddonInstallRequest installRequest = AddonInstallRequest.fromBase64(extensionBase64, AddonPersistence.TEMPORARY);
        installAddon_unsigned_temporary(installRequest);
    }

    public void installAddon_unsigned_temporary(AddonInstallRequest installRequest) throws Exception {
        BehaviorVerifier<Void> verifier = (driver, baseUri) -> {
            driver.get(baseUri.toString());
            WebElement element = new WebDriverWait(driver, 3).until(ExpectedConditions.presenceOfElementLocated(By.id("injected")));
            String text = element.getText();
            assertEquals("element text", "Hello", text.trim());
            return (Void) null;
        };
        testInstallAddon(installRequest, verifier);
    }

    private interface BehaviorVerifier<T> {
        @SuppressWarnings("UnusedReturnValue")
        T verify(ExtensibleFirefoxDriver driver, URI baseUri) throws IOException;
    }

    @SuppressWarnings("UnusedReturnValue")
    private <T> T testInstallAddon(AddonInstallRequest installRequest, BehaviorVerifier<T> verifier) throws Exception {
        GeckoDriverService gecko = new GeckoDriverService.Builder()
                .usingAnyFreePort()
                .withEnvironment(xvfb.getController().newEnvironment())
                .build();
        String html = "<!DOCTYPE html><html><body></body></html>";
        NanoServer server = NanoServer.builder()
                .get(NanoResponse.status(200).content(MediaType.HTML_UTF_8, html, UTF_8).build())
                .build();
        try (NanoControl control = server.startServer()) {
            ExtensibleFirefoxDriver driver = new ExtensibleFirefoxDriver(gecko, new FirefoxOptions());
            try {
                driver.installAddon(installRequest);
                return verifier.verify(driver, control.baseUri());
            } catch (WebDriverException e) {
                e.printStackTrace(System.out);
                throw e;
            } finally {
                driver.quit();
            }
        }
    }

    private static final String SAMPLE_EXTENSION_ID = "sample-extension@antiprint.mike10004.github.io";

    private File buildSampleExtension() throws URISyntaxException, IOException {
        File manifestFile = new File(getClass().getResource("/sample-extension/manifest.json").toURI());
        Path extensionDir = manifestFile.getParentFile().toPath();
        File zipFile = File.createTempFile("sample-extension", ".zip");
        ZippingFileVisitor.zip(extensionDir, zipFile);
        return zipFile;
    }

    @Test
    public void uninstallAddon() throws Exception {
        File sampleExtensionZipFile = buildSampleExtension();
        AddonInstallRequest installRequest = AddonInstallRequest.fromFile(sampleExtensionZipFile, AddonPersistence.TEMPORARY);
        BehaviorVerifier<Void> verifier = (driver, baseUri) -> {
            AddonUninstallRequest uninstallRequest = AddonUninstallRequest.fromId(SAMPLE_EXTENSION_ID);
            driver.uninstallAddon(uninstallRequest);
            driver.get(baseUri.toString());
            try {
                new WebDriverWait(driver, 3).until(ExpectedConditions.presenceOfElementLocated(By.id("injected")));
                fail("extension still installed it seems");
            } catch (org.openqa.selenium.TimeoutException ignore) {
                // we expect NOT to find the element
            }
            return (Void) null;
        };
        testInstallAddon(installRequest, verifier);
    }

}
