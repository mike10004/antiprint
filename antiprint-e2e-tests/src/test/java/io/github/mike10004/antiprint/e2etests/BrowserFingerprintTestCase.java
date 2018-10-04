package io.github.mike10004.antiprint.e2etests;

import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Assert;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

class BrowserFingerprintTestCase {

    public final Input input;

    public final Output output;

    public BrowserFingerprintTestCase(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrowserFingerprintTestCase)) return false;
        BrowserFingerprintTestCase that = (BrowserFingerprintTestCase) o;
        return Objects.equals(input, that.input) &&
                Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output);
    }

    public static class Input {

        public final UserAgentFamily userAgentFamily;

        public final OperatingSystemFamily os;

        public final String userAgent;

        public Input(UserAgentFamily userAgentFamily, OperatingSystemFamily os, String userAgent) {
            this.userAgentFamily = userAgentFamily;
            this.os = os;
            this.userAgent = userAgent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Input)) return false;
            Input input = (Input) o;
            return userAgentFamily == input.userAgentFamily &&
                    os == input.os &&
                    Objects.equals(userAgent, input.userAgent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userAgentFamily, os, userAgent);
        }
    }

    public static class Output {

        public final Window window;

        public Output(Window window) {
            this.window = window;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Output)) return false;
            Output output = (Output) o;
            return Objects.equals(window, output.window);
        }

        @Override
        public int hashCode() {
            return Objects.hash(window);
        }

        public static class Window {

            public Map<String, RequiredValue> navigator;

            public Window(Map<String, RequiredValue> navigator) {
                this.navigator = navigator;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Window)) return false;
                Window window = (Window) o;
                return Objects.equals(navigator, window.navigator);
            }

            @Override
            public int hashCode() {
                return Objects.hash(navigator);
            }
        }
    }

    public enum EvalMode {
        trivial,
        regex,
        literal,
        absent;

        private static <T> Predicate<T> vacant(boolean allowVacant) {
            return value -> allowVacant && (value == null || (value instanceof String && (((String)value).isEmpty())));
        }

        public Predicate<String> toPredicate(String data, boolean allowVacant) {
            switch (this) {
                case absent:
                    return new NamedPredicate<>("RequiredAbsent{}", Objects::isNull);
                case trivial:
                    return named(data, x -> Boolean.parseBoolean(data), allowVacant);
                case regex:
                    return named(data, x -> x.matches(data), allowVacant);
                case literal:
                    return named(data, x -> java.util.Objects.equals(data, x), allowVacant);
                default:
                    throw new RuntimeException("unhandled enum: " + name());
            }
        }

        private static class NamedPredicate<T> implements Predicate<T> {

            private final String name;
            private final Predicate<T> delegate;

            public NamedPredicate(String name, Predicate<T> delegate) {
                this.name = name;
                this.delegate = delegate;
            }

            @Override
            public boolean test(T t) {
                return delegate.test(t);
            }

            @Override
            public String toString() {
                return name;
            }
        }

        private <T> Predicate<T> named(String data, Predicate<T> predicate, boolean allowVacant) {
            String name = String.format("%s%s{\"%s\"}", StringUtils.capitalize(name()), allowVacant ? "?" : "", StringEscapeUtils.escapeJava(data));
            Predicate<T> vacant = vacant(allowVacant);
            predicate = vacant.or(predicate);
            return new NamedPredicate<>(name, predicate);
        }
    }

    public static class RequiredValue {

        public final EvalMode mode;
        public final String data;
        /**
         * Flag that means this requirement is satisfied if the actual value is absent, null, or empty.
         */
        @Nullable
        public final Boolean allowVacant;

        public RequiredValue(EvalMode mode, String data) {
            this(mode, data, null);
        }

        public RequiredValue(EvalMode mode, String data, @Nullable Boolean allowVacant) {
            this.mode = mode;
            this.data = data;
            this.allowVacant = allowVacant;
        }

        public Predicate<String> asPredicate() {
            checkState(mode != null, "mode is null but data = %s", data);
            return mode.toPredicate(data, allowVacant != null && allowVacant.booleanValue());
        }

        public void assertCorrect(String message, String actual) {
            Predicate<String> pred = asPredicate();
            Assert.assertTrue(String.format("message: %s; predicate %s evaluated to false on actual input \"%s\"", message, pred, StringEscapeUtils.escapeJava(StringUtils.abbreviate(actual, 1024))), pred.test(actual));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RequiredValue)) return false;
            RequiredValue that = (RequiredValue) o;
            return mode == that.mode &&
                    Objects.equals(data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mode, data);
        }

        public String toString() {
            return String.format("Requirement{%s}", asPredicate());
        }
    }

    public static List<BrowserFingerprintTestCase> loadAll() throws IOException {
        return loadSome(x -> true);
    }

    public static List<BrowserFingerprintTestCase> loadSome(Predicate<? super BrowserFingerprintTestCase> filter) throws IOException {
        return loadAsStream().filter(filter).collect(Collectors.toList());
    }

    public static List<BrowserFingerprintTestCase> loadSome(UserAgentFamily userAgentFamily) throws IOException {
        return loadSome(testCase -> testCase.input.userAgentFamily == userAgentFamily);
    }

    private static Stream<BrowserFingerprintTestCase> loadAsStream() throws IOException {
        URL resource = BrowserFingerprintTestCase.class.getResource("/attribution/navigator-test-cases.json");
        checkState(resource != null);
        CharSource source = Resources.asCharSource(resource, UTF_8);
        try (Reader reader = source.openStream()) {
            BrowserFingerprintTestCase[] testCases = new Gson().fromJson(reader, BrowserFingerprintTestCase[].class);
            checkState(testCases != null, "null loaded from %s", resource);
            checkState(Arrays.stream(testCases).noneMatch(Objects::isNull), "expect all test cases to be non-null");
            return Arrays.stream(testCases);
        }
    }

}
