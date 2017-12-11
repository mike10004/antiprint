package io.github.mike10004.extensibleffdriver;

import com.google.common.base.Suppliers;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * A Firefox webdriver implementation that supports unsigned addon installation.
 * This is an extension of {@link FirefoxDriver} that provides additional methods
 * {@link #installAddon(AddonInstallRequest)} and {@link #uninstallAddon(AddonUninstallRequest)}.
 * A special command executor is created to handle the install/uninstall, and it is
 * less tolerant to strange inputs, so this may only work in limited cases.
 */
public class ExtensibleFirefoxDriver extends FirefoxDriver {

    private final AddonSupport addonSupport;

    /**
     * Constructs a driver instance using the given driver service and capabilities.
     * This constructor invokes the superconstructor
     * {@link FirefoxDriver#FirefoxDriver(GeckoDriverService, FirefoxOptions)} directly.
     * The instance will act just like a {@link FirefoxDriver} instance, except you
     * will be able to
     * @param service the driver service
     * @param options the capabilities
     */
    public ExtensibleFirefoxDriver(GeckoDriverService service, FirefoxOptions options) {
        super(service, options);
        this.addonSupport = new AddonSupport(this::getSessionId, service);
    }

    /**
     * Installs an addon.
     * @param request installation request parameters
     * @throws IOException
     */
    public void installAddon(AddonInstallRequest request) throws IOException {
        addonSupport.installAddon(request);
    }

    /**
     * Uninstalls an addon.
     * @param request uninstallation request parameters
     * @throws IOException
     */
    public void uninstallAddon(AddonUninstallRequest request) throws IOException {
        addonSupport.uninstallAddon(request);
    }

    public static ArtifactInfo getArtifactInfo() {
        return artifactInfo.get();
    }

    public static ArtifactInfo getParentArtifactInfo() {
        return parentArtifactInfo.get();
    }

    private static final String ARTIFACT_INFO_RESOURCE_PATH = "/extensible-firefox-webdriver/info.properties";

    private static final Supplier<ArtifactInfo> artifactInfo = Suppliers.memoize(() -> {
        try {
            return ArtifactInfo.fromResource(ARTIFACT_INFO_RESOURCE_PATH, "project.");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    });

    private static final Supplier<ArtifactInfo> parentArtifactInfo = Suppliers.memoize(() -> {
        try {
            return ArtifactInfo.fromResource(ARTIFACT_INFO_RESOURCE_PATH, "project.parent.");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    });

    public static class ArtifactInfo {

        private final String groupId;
        private final String artifactId;
        private final String version;

        public ArtifactInfo(String groupId, String artifactId, String version) {
            this.groupId = Objects.requireNonNull(groupId);
            this.artifactId = Objects.requireNonNull(artifactId);
            this.version = Objects.requireNonNull(version);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArtifactInfo that = (ArtifactInfo) o;
            return Objects.equals(groupId, that.groupId) &&
                    Objects.equals(artifactId, that.artifactId) &&
                    Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, artifactId, version);
        }

        @Override
        public String toString() {
            return "ArtifactInfo{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", version='" + version + '\'' +
                    '}';
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public static ArtifactInfo fromProperties(Properties properties, String prefix) {
            return new ArtifactInfo(properties.getProperty(prefix + "groupId"),
                    properties.getProperty(prefix + "artifactId"),
                    properties.getProperty(prefix + "version"));
        }

        public static ArtifactInfo fromResource(String resourcePath, String prefix) throws IOException {
            Objects.requireNonNull(resourcePath, "resource path must be non-null");
            try (InputStream in = ArtifactInfo.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new FileNotFoundException("classpath:" + resourcePath);
                }
                Properties p = new Properties();
                p.load(in);
                return fromProperties(p, prefix);
            }
        }
    }
}
