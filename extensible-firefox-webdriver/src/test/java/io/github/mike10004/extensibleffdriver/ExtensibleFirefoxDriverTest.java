package io.github.mike10004.extensibleffdriver;

import com.github.mike10004.xvfbtesting.XvfbRule;
import com.google.common.net.MediaType;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import org.apache.commons.io.FilenameUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ExtensibleFirefoxDriverTest {

    @Rule
    public XvfbRule xvfb = XvfbRule.builder().build();

    @BeforeClass
    public static void setUpGeckodriver() {
        FirefoxDriverManager.getInstance().version("0.19.1").setup();
    }

    @org.junit.Test
    public void installAddon_unsigned_temporary() throws Exception {
        File sampleExtensionZipFile = buildSampleExtension();
        AddonInstallation installRequest = new AddonInstallation(sampleExtensionZipFile, AddonPersistence.TEMPORARY);
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
    private <T> T testInstallAddon(AddonInstallation installRequest, BehaviorVerifier<T> verifier) throws Exception {
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
        zip(extensionDir, zipFile);
        return zipFile;
    }

    @org.junit.Test
    public void uninstallAddon() throws Exception {
        File sampleExtensionZipFile = buildSampleExtension();
        AddonInstallation installRequest = new AddonInstallation(sampleExtensionZipFile, AddonPersistence.TEMPORARY);
        BehaviorVerifier<Void> verifier = (driver, baseUri) -> {
            AddonUninstallation uninstallRequest = new AddonUninstallation(SAMPLE_EXTENSION_ID);
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

    private void zip(Path enclosure, File zipFile) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walkFileTree(enclosure, new ZippingFileVisitor(enclosure, zipOutputStream));
        }
    }

    private static class ZippingFileVisitor implements FileVisitor<Path> {

        private final Path enclosure;
        private final ZipOutputStream zipOutputStream;

        private ZippingFileVisitor(Path enclosure, ZipOutputStream zipOutputStream) {
            this.enclosure = enclosure;
            this.zipOutputStream = zipOutputStream;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            String relativeDirName = normalize(dir) + "/";
            zipOutputStream.putNextEntry(new ZipEntry(relativeDirName));
            zipOutputStream.closeEntry();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String fileEntryName = normalize(file);
            ZipEntry entry = new ZipEntry(fileEntryName);
            zipOutputStream.putNextEntry(entry);
            com.google.common.io.Files.asByteSource(file.toFile()).copyTo(zipOutputStream);
            zipOutputStream.closeEntry();
            return FileVisitResult.CONTINUE;
        }

        private String normalize(Path path) {
            Path relativeDir = enclosure.relativize(path);
            return FilenameUtils.normalizeNoEndSeparator(relativeDir.toString(), true);
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            throw exc;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            return FileVisitResult.CONTINUE;
        }

    }
}
