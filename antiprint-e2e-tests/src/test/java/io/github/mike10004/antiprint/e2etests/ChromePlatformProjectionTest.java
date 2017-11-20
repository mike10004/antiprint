package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.ImmutableList;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChromePlatformProjectionTest extends PlatformProjectionTestBase {

    public ChromePlatformProjectionTest(UserAgentFamily userAgentFamily, OperatingSystemFamily osFamily) {
        super(userAgentFamily, osFamily);
    }

    @Override
    protected WebDriver createDriver(String userAgent, Map<String, String> environment) throws IOException {
        return new ChromeDriverProvider(userAgent).provide(environment);
    }

    @Parameters
    public static List<Object[]> parametersList() {
        return ImmutableList.<Object[]>builder()
                .add(new Object[]{UserAgentFamily.CHROME, OperatingSystemFamily.WINDOWS})
                .add(new Object[]{UserAgentFamily.CHROME, OperatingSystemFamily.OS_X})
                .add(new Object[]{UserAgentFamily.CHROME, OperatingSystemFamily.LINUX})
                .build();
    }

}
