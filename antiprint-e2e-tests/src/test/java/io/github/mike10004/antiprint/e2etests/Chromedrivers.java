package io.github.mike10004.antiprint.e2etests;

import com.github.mike10004.nativehelper.subprocess.ProcessMonitor;
import com.github.mike10004.nativehelper.subprocess.ProcessResult;
import com.github.mike10004.nativehelper.subprocess.ScopedProcessTracker;
import com.github.mike10004.nativehelper.subprocess.Subprocess;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class Chromedrivers {

    private static final Logger log = LoggerFactory.getLogger(Chromedrivers.class);

    private Chromedrivers() {}

    private static class Compatibility {
        public final Range<Integer> chromeMajorVersionRange;
        public final String chromedriverVersion;
        private final BigDecimal numericChromedriverVersion;

        private Compatibility(Range<Integer> chromeMajorVersionRange, String chromedriverVersion) {
            this.chromeMajorVersionRange = requireNonNull(chromeMajorVersionRange);
            this.chromedriverVersion = requireNonNull(chromedriverVersion);
            checkArgument(!chromedriverVersion.trim().isEmpty());
            numericChromedriverVersion = new BigDecimal(chromedriverVersion);
        }
        private static final Ordering<Compatibility> orderingByChromedriverVersionAscending = Ordering.<BigDecimal>natural().onResultOf(compat -> compat.numericChromedriverVersion);

        public static Ordering<Compatibility> orderingByChromedriverVersion() {
            return orderingByChromedriverVersionAscending;
        }

        public static Compatibility of(String chromedriverVersion, int minChromeMajorInclusive, int maxChromeMajorInclusive) {
            return new Compatibility(Range.closed(minChromeMajorInclusive, maxChromeMajorInclusive), chromedriverVersion);
        }
    }

    private static class CompatibleVersionFinder {
        public final ImmutableList<Compatibility> compatibilityList;

        private CompatibleVersionFinder(Iterable<Compatibility> compatibilityList) {
            this.compatibilityList = Compatibility.orderingByChromedriverVersion().reverse().immutableSortedCopy(compatibilityList);
        }

        @Nullable
        public String findNewestCompatibleChromedriverVersion(int chromeMajorVersion) {
            for (Compatibility compatibility : compatibilityList) {
                if (compatibility.chromeMajorVersionRange.contains(chromeMajorVersion)) {
                    return compatibility.chromedriverVersion;
                }
            }
            return null;
        }
    }

    // https://stackoverflow.com/a/49618567/2657036
    //        2.36            63-65
    //        2.35            62-64
    //        2.34            61-63
    //        2.33            60-62
    //        ---------------------
    //        2.28            57+
    //        2.25            54+
    //        2.24            53+
    //        2.22            51+
    //        2.19            44+
    //        2.15            42+

    private static final ImmutableList<Compatibility> COMPATIBILITY_TABLE = ImmutableList.<Compatibility>builder()
            .add(Compatibility.of("2.37", 64, 66))
            .add(Compatibility.of("2.36", 63, 65))
            .add(Compatibility.of("2.35", 62, 64))
            .add(Compatibility.of("2.34", 61, 63))
            .add(Compatibility.of("2.33", 60, 62))
            .add(Compatibility.of("2.32", 57, 62))
            // info before that is foggy; see https://sites.google.com/a/chromium.org/chromedriver/downloads
            .build();

    private static CompatibleVersionFinder FINDER_INSTANCE = new CompatibleVersionFinder(COMPATIBILITY_TABLE);

    private static CompatibleVersionFinder getFinderInstance() {
        return FINDER_INSTANCE;
    }

    private static WebDriverManager configureWebDriverManager(@Nullable String chromedriverVersion) {
        WebDriverManager m = ChromeDriverManager.getInstance();
        if (chromedriverVersion != null) {
            return m.version(chromedriverVersion);
        }
        return m;
    }

    static String determineBestChromedriverVersion() {
        @Nullable String chromeVersionString = new WhichingChromeVersionQuerier().getChromeVersionString();
        if (chromeVersionString == null && SystemUtils.IS_OS_WINDOWS) {
            chromeVersionString = new WindowsChromeVersionQuerier().getChromeVersionString();
        }
        String chromedriverVersion = null;
        if (chromeVersionString != null) {
            int chromeMajorVersion = -1;
            try {
                chromeMajorVersion = parseChromeMajorVersion(chromeVersionString);
            } catch (RuntimeException e) {
                log.info("failed to parse major version from {} due to {}", StringUtils.abbreviate(chromeVersionString, 128), e.toString());
            }
            chromedriverVersion = getFinderInstance().findNewestCompatibleChromedriverVersion(chromeMajorVersion);
        }
        return chromedriverVersion;
    }

    /**
     * Determines the appropriate chromedriver version for the installed version of Chrome.
     * @return a {@link ChromeDriverManager} instance configured with that version
     */
    public static WebDriverManager findBestVersion() {
        return configureWebDriverManager(determineBestChromedriverVersion());
    }

    private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().trimResults();
    private static final Splitter DOT_SPLITTER = Splitter.on('.').omitEmptyStrings();

    public static int parseChromeMajorVersion(String chromeVersionString) {
        Iterable<String> tokens = WHITESPACE_SPLITTER.split(chromeVersionString);
        for (String token : tokens) {
            if (token.matches("^\\d+(\\.\\d+)+")) {
                return Integer.parseInt(DOT_SPLITTER.split(token).iterator().next());
            }
        }
        throw new IllegalArgumentException(String.format("no tokens look like a version in \"%s\"", StringEscapeUtils.escapeJava(StringUtils.abbreviate(chromeVersionString, 256))));
    }

    interface ChromeVersionQuerier {
        @Nullable
        String getChromeVersionString();
    }

    static abstract class ExecutingChromeVersionQuerier implements ChromeVersionQuerier {

        private static final Logger log = LoggerFactory.getLogger(ExecutingChromeVersionQuerier.class);

        @Nullable
        @Override
        public String getChromeVersionString() {
            @Nullable File chromeExecutable = resolveChromeExecutable();
            if (chromeExecutable != null) {
                log.info("chrome executable resolved at {}", chromeExecutable);
                return captureVersion(chromeExecutable);
            }
            log.info("chrome executable could not be detected");
            return null;
        }

        @Nullable
        protected abstract File resolveChromeExecutable();

        /**
         * Captures the version string printed when Chrome is executed with the {@code --version} option.
         * @param chromeExecutable chrome executable pathname
         * @return the version string, or null if it execution failed
         */
        @Nullable
        protected String captureVersion(File chromeExecutable) {
            String value = execute(chromeExecutable, "--version");
            if (value != null) {
                log.info("chrome version: {}", String.format("\"%s\"", StringEscapeUtils.escapeJava(value)));
            } else {
                log.warn("failed to capture version from chrome executable {}", chromeExecutable);
            }
            return value;
        }

    }

    private static final int PROCESS_EXECUTION_TIMEOUT_MILLIS = 5000;

    @Nullable
    private static String execute(File executable, String...args) {
        return execute(executable.getName(), Subprocess.running(executable), args);
    }

    @Nullable
    private static String execute(String executable, String...args) {
        return execute(executable, Subprocess.running(executable), args);
    }

    private static String execute(String executableName, Subprocess.Builder subprocessBuilder, String...args) {
        try (ScopedProcessTracker processTracker = new ScopedProcessTracker()) {
            Subprocess subproc = subprocessBuilder.args(Arrays.asList(args)).build();
            ProcessMonitor<String, String> processMonitor = subproc.launcher(processTracker).outputStrings(Charset.defaultCharset()).launch();
            ProcessResult<String, String> result = processMonitor.await(PROCESS_EXECUTION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            if (result.exitCode() == 0) {
                String stdout = result.content().stdout().trim();
                if (stdout.trim().isEmpty()) {
                    log.warn("stdout is empty; stderr: {}", result.content().stderr());
                }
                return stdout;
            } else {
                log.warn("executing {} with arguments {} failed: {}", executableName, Arrays.toString(args), result);
            }
        } catch (InterruptedException | java.util.concurrent.TimeoutException e) {
            log.warn("failed to await termination of {} process after {} millis: {}", executableName, PROCESS_EXECUTION_TIMEOUT_MILLIS, e.toString());
        }
        return null;
    }

    static class WindowsChromeVersionQuerier extends ExecutingChromeVersionQuerier {

        private static final String REG_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe";

        private static List<File> buildLikelyPathsList() {
            File userExecutable = new File(System.getProperty("user.home")).toPath()
                    .resolve("AppData")
                    .resolve("Local")
                    .resolve("Google")
                    .resolve("Chrome")
                    .resolve("Application")
                    .resolve("chrome.exe")
                    .toFile();
            List<File> systemRoots = Arrays.asList(new File("C:/Program Files/"), new File("C:/Program Files (x86)/"));
            Stream<File> systemExePaths = systemRoots.stream().map(root -> {
                return root.toPath().resolve("Google").resolve("Chrome").resolve("Application").resolve("chrome.exe").toFile();
            });
            return Stream.concat(Stream.of(userExecutable), systemExePaths).collect(Collectors.toList());
        }

        @Nullable
        @Override
        protected File resolveChromeExecutable() {
            // "%UserProfile%\\AppData\\Local\\Google\\Chrome\\Application\\chrome --version"
            List<File> likelyExePaths = buildLikelyPathsList();
            for (File chromeExe : likelyExePaths) {
                if (chromeExe.isFile() && chromeExe.canExecute()) {
                    return chromeExe;
                }
            }
            return queryRegistryForExePath();
        }

        protected File queryRegistryForExePath() {
            String regOutput = execute("reg", "QUERY", REG_KEY);
            if (regOutput != null) {
                //                                                             C:\path\to\...
                Matcher m = Pattern.compile("\\s*\\(Default\\)\\s+REG_SZ\\s+(\\w:.+)\\s*$").matcher(regOutput);
                if (m.find()) {
                    File pathname = new File(m.group(1));
                    if (pathname.isFile() && pathname.canExecute()) {
                        return pathname;
                    }
                }
            }
            return null;
        }
    }

    static class WhichingChromeVersionQuerier extends ExecutingChromeVersionQuerier {

        private static final Logger log = LoggerFactory.getLogger(WhichingChromeVersionQuerier.class);

        private static final ImmutableSet<String> CHROME_EXECUTABLE_NAMES = ImmutableSet.of("google-chrome", "chromium-browser", "chrome");

        @Override
        protected File resolveChromeExecutable() {
            for (String chromeExecutableName : CHROME_EXECUTABLE_NAMES) {
                String path = findByNameOnSystemPath(chromeExecutableName);
                if (path != null) {
                    try {
                        return new File(path);
                    } catch (Exception e) {
                        log.info("failed to execute `{} --version`: {}", path, e.toString());
                    }
                }
            }
            return null;
        }

        @Nullable
        protected String findByNameOnSystemPath(String input) {
            try {
                Set<String> names = new HashSet<>();
                names.add(input);
                if (SystemUtils.IS_OS_WINDOWS) {
                    input = input.toLowerCase();
                    if (!input.matches("^.+\\.\\S{3}")) {
                        names.add(input + ".exe");
                    }
                }
                List<String> systemPathDirectories = Splitter.on(File.pathSeparatorChar).omitEmptyStrings()
                        .splitToList(Strings.nullToEmpty(System.getenv("PATH")));
                for (String dir : systemPathDirectories) {
                    for (String name : names) {
                        File f = new File(dir, name);
                        if (f.isFile() && f.canExecute()) {
                            return f.getAbsolutePath();
                        }
                    }
                }
            } catch (RuntimeException e) {
                log.warn("failed to which {}: {}", input, e.toString());
            }
            return null;
        }

    }
}
