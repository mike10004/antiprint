package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentFamily;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Tests {

    private static final Supplier<Properties> supplier = Suppliers.memoize(() -> {
        Properties p = new Properties();
        try (Reader reader = new InputStreamReader(Tests.class.getResourceAsStream("/tests.properties"), UTF_8)) {
            p.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return p;
    });

    public static Properties getProperties() {
        return supplier.get();
    }

    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public static File getParentBaseDir() {
        return new File(getProperty("project.parent.basedir"));
    }

    public static ImmutableList<File> getNavigatorTestCaseFiles() {
        File dir = getParentBaseDir().toPath()
                .resolve("antiprint-extension")
                .resolve("src/test/resources")
                .resolve("fixtures")
                .toFile();
        return ImmutableList.copyOf(FileUtils.listFiles(dir, new String[]{"json"}, false));
    }

    public static Function<CharSource, Map<String, Object>> navigatorObjectLoader() {
        return cs -> {
            try {
                try (Reader reader = cs.openStream()) {
                    Map<String, Object> map = new Gson().fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
                    return Maps.transformValues(map, o -> {
                        if (o instanceof Double) {
                            Double d = (Double) o;
                            if (d.longValue() == d.doubleValue()) {
                                return d.longValue();
                            }
                        }
                        return o;
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Map<String, Object> getNavigatorTestCase(UserAgentFamily userAgentFamily, OperatingSystemFamily operatingSystemFamily) {
        return getNavigatorTestCasesByUserAgent(userAgent -> {
            return userAgentFamily == userAgent.getFamily() && operatingSystemFamily == userAgent.getOperatingSystem().getFamily();
        }).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no test cases for given requirements"));
    }

    public static ImmutableList<Map<String, Object>> getNavigatorTestCasesByUserAgent(Predicate<? super ReadableUserAgent> filter) {
        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        return getNavigatorTestCases(testCase -> {
            ReadableUserAgent agent = parser.parse(testCase.get("userAgent").toString());
            return filter.test(agent);
        });
    }

    public static ImmutableList<Map<String, Object>> getNavigatorTestCases(Predicate<? super Map<String, Object>> filter) {
        return getNavigatorTestCaseFiles().stream()
                .map(file -> Files.asCharSource(file, UTF_8))
                .map(navigatorObjectLoader())
                .filter(filter)
                .collect(ImmutableList.toImmutableList());
    }

    public static File getBuildDir() {
        return new File(getProperty("project.build.directory"));
    }

    public static void setUpGeckodriver() {
        FirefoxDriverManager.getInstance().version(geckodriverVersion()).setup();
    }

    public static void setUpChromedriver() {
        Chromedrivers.findBestVersion().setup();
    }

    @Nullable
    private static String geckodriverVersion() {
        return System.getProperty("wdm.geckoDriverVersion");
    }

    public static boolean filesEqual(Unzippage a, Unzippage b) throws IOException {
        if (!ImmutableSet.copyOf(a.fileEntries()).equals(ImmutableSet.copyOf(b.fileEntries()))) {
            return false;
        }
        for (String entry : a.fileEntries()) {
            if (!a.getFileBytes(entry).contentEquals(b.getFileBytes(entry))) {
                return false;
            }
        }
        return true;
    }

    public static Unzippage pseudoUnzippage(Path parent) throws IOException {
        Collection<File> files = FileUtils.listFiles(parent.toFile(), null, true);
        Function<File, String> entryNameMapper = file -> FilenameUtils.normalizeNoEndSeparator(parent.relativize(file.toPath()).toString(), true) + (file.isDirectory() ? "/" : "");
        Set<String> directoryEntries = files.stream().map(File::getParentFile)
                .map(entryNameMapper)
                .collect(Collectors.toSet());
        Map<String, ByteSource> fileEntries = files.stream().collect(Collectors.toMap(entryNameMapper, Files::asByteSource));
        return new Unzippage() {
            @Override
            public Iterable<String> fileEntries() {
                return fileEntries.keySet();
            }

            @Override
            public Iterable<String> directoryEntries() {
                return directoryEntries;
            }

            @Override
            public ByteSource getFileBytes(String fileEntry) {
                return checkNotNull(fileEntries.get(fileEntry));
            }
        };
    }
}
