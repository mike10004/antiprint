package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Tests {

    public static final String ENV_TIMEOUT_MEDIUM = "UNIT_TEST_TIMEOUT_MEDIUM_SECONDS";

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

    private static boolean isUnfiltered(String key, String value) {
        return ("${" + key + "}").equals(value);
    }

    public static File getParentBaseDir() {
        String val = getProperty("project.parent.basedir");
        if (isUnfiltered("project.parent.basedir", val)) {
            val = getProperty("project.basedir");
            checkState(!isUnfiltered("project.basedir", val), "property unfiltered: %s", val);
            File basedir = new File(val);
            return basedir.getParentFile();
        }
        return new File(val);
    }

    public static BrowserFingerprintTestCase getNavigatorTestCase(UserAgentFamily userAgentFamily, OperatingSystemFamily operatingSystemFamily) {
        Predicate<? super BrowserFingerprintTestCase> filter = testCase -> {
            return userAgentFamily == testCase.input.userAgentFamily && operatingSystemFamily == testCase.input.os;
        };
        List<BrowserFingerprintTestCase> cases = getNavigatorTestCases()
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
        checkArgument(!cases.isEmpty(), "no cases match %s/%s", userAgentFamily, operatingSystemFamily);
        checkArgument(cases.size() == 1, "multiple cases match %s/%s", userAgentFamily, operatingSystemFamily);
        return cases.get(0);
    }

    public static ImmutableList<BrowserFingerprintTestCase> getNavigatorTestCases() {
        try (Reader reader = Resources.asCharSource(Tests.class.getResource("/navigator-test-cases.json"), UTF_8).openStream()) {
            BrowserFingerprintTestCase testCases[] = new Gson().fromJson(reader, BrowserFingerprintTestCase[].class);
            checkState(testCases != null);
            return ImmutableList.copyOf(testCases);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getBuildDir() {
        return new File(getProperty("project.build.directory"));
    }

    public static void setUpGeckodriver() {
        FirefoxDriverManager.getInstance().setup();
    }

    public static void setUpChromedriver() {
        ChromeDriverManager.getInstance().setup();
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

    @Nullable
    public static Object deserialize(JsonElement serialized) {
        if (serialized.isJsonNull()) {
            return null;
        }
        JsonPrimitive primitive = serialized.getAsJsonPrimitive();
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        checkState(primitive.isNumber());
        Number number = primitive.getAsNumber();
        if (number.longValue() == number.doubleValue()) {
            return number.longValue();
        } else {
            return number.doubleValue();
        }
    }

    private static final String SYSPROP_ENV_BACKUP_PREFIX = "antiprint.env.";

    private static final LoadingCache<String, Optional<String>> ENV_CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Optional<String>>() {
                @Override
                public Optional<String> load(String envVarName) {
                    String envVarValue = System.getenv(envVarName);
                    if (envVarValue != null) {
                        System.err.format("%s=%s (setting defined in environment)%n", envVarName, StringUtils.abbreviate(envVarValue, 64));
                    } else {
                        envVarValue = System.getProperty(SYSPROP_ENV_BACKUP_PREFIX + envVarName);
                        if (envVarValue != null) {
                            System.err.format("%s=%s (setting defined in system properties)%n", envVarName, StringUtils.abbreviate(envVarValue, 64));
                        }
                    }
                    if (envVarValue == null) {
                        System.err.format("%s is not defined in environment or as %s%s system properties%n", envVarName, SYSPROP_ENV_BACKUP_PREFIX, envVarName);
                    }
                    return Optional.ofNullable(envVarValue);
                }
            });

    public static String getEnvironmentSetting(String envVarName, String defaultValue) {
        return getEnvironmentSetting(envVarName, Function.identity(), defaultValue);
    }

    public static <T> T getEnvironmentSetting(String envVarName, Function<? super String, T> transform, T defaultValue) {
        Optional<String> value = ENV_CACHE.getUnchecked(envVarName);
        Optional<T> typedValue = Optional.empty();
        try {
            typedValue = value.map(transform);
        } catch (RuntimeException e) {
            System.err.format("error transforming setting defined in environment: value %s caused %s%n", StringUtils.abbreviate(value.orElse(null), 64), e);
        }
        return typedValue.orElse(defaultValue);
    }

    public static int getMediumTimeoutSeconds(int defaultValue) {
        return getEnvironmentSetting(ENV_TIMEOUT_MEDIUM, Integer::parseInt, defaultValue);
    }

    public static int getGlobalBrowserTestTimeout() {
        return getEnvironmentSetting("UNIT_TEST_WEB_GLOBAL_TIMEOUT_SECONDS", Integer::parseInt, 20);
    }
}
