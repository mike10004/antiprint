package io.github.mike10004.extensibleffdriver;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArtifactInfoTest {

    @Test
    public void getArtifactInfo() {
        System.out.println("getArtifactInfo");
        ExtensibleFirefoxDriver.ArtifactInfo info = ExtensibleFirefoxDriver.getArtifactInfo();
        System.out.println(info);
        assertEquals("driver artifactId", "extensible-firefox-webdriver", info.getArtifactId());
        ExtensibleFirefoxDriver.ArtifactInfo parentInfo = ExtensibleFirefoxDriver.getParentArtifactInfo();
        String parentVersion = parentInfo.getVersion();
        // we already do this in check_version.py, which is executed during the build of
        // the dependency artifact, so we can probably remove this test
        boolean consistent = info.getVersion().matches("^\\d+\\.\\d+\\.\\d+x\\Q" + parentVersion + "\\E$");
        assertTrue("version suffix equals parent version: " + info.getVersion() + " is not consistent with " + parentVersion, consistent);
    }

    @Test
    public void getParentArtifactInfo() {
        System.out.println("getParentArtifactInfo");
        ExtensibleFirefoxDriver.ArtifactInfo parentInfo = ExtensibleFirefoxDriver.getParentArtifactInfo();
        System.out.println(parentInfo);
        assertEquals("parent artifactId", "antiprint", parentInfo.getArtifactId());
    }

}
