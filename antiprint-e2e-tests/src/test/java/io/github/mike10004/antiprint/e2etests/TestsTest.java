package io.github.mike10004.antiprint.e2etests;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestsTest {

    @Test
    public void deserialize() {
        assertDeserialized("0", Long.class, 0L);
        assertDeserialized("1.5", Double.class, 1.5d);
        assertDeserialized("8", Long.class, 8L);
        assertDeserialized("8.0", Long.class, 8L);
        assertDeserialized("null", null, null);
    }

    private void assertDeserialized(String json, Class<?> expectedType, Object expectedValue) {
        JsonElement primitive = new JsonParser().parse(json);
        Object deserialized = Tests.deserialize(primitive);
        if (expectedType == null) {
            assertNull(deserialized);
            return;
        }
        assertTrue(String.format("expect %s is instance of %s", deserialized, expectedType), expectedType.isInstance(deserialized));
        assertEquals("value", expectedValue, deserialized);
    }

    @Test
    public void getNavigatorTestCase() throws Exception {
        for (UserAgentFamily uaFamily : new UserAgentFamily[]{UserAgentFamily.CHROME, UserAgentFamily.FIREFOX}) {
            for (OperatingSystemFamily osFamily : new OperatingSystemFamily[]{ OperatingSystemFamily.OS_X, OperatingSystemFamily.WINDOWS, OperatingSystemFamily.LINUX}) {
                BrowserFingerprintTestCase testCase = Tests.getNavigatorTestCase(uaFamily, osFamily);
                assertEquals("ua family", uaFamily, testCase.input.userAgentFamily);
                assertEquals("os family", osFamily, testCase.input.os);
            }
        }

    }
}