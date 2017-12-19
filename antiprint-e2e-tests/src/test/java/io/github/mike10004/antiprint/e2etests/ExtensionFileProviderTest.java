package io.github.mike10004.antiprint.e2etests;

import org.junit.Test;

import java.io.File;

public class ExtensionFileProviderTest {

    @Test
    public void isUpToDate_crx() throws Exception {
        File crxFile = ExtensionFileProvider.ofDependency(ExtensionFileFormat.CRX).provide();
        System.out.format("ok: %s%n", crxFile);
    }

    @Test
    public void isUpToDate_zip() throws Exception {
        File crxFile = ExtensionFileProvider.ofDependency(ExtensionFileFormat.ZIP).provide();
        System.out.format("ok: %s%n", crxFile);
    }

}