package io.github.mike10004.antiprint.e2etests;

import org.junit.Test;

import java.io.File;

public class CrxProviderTest {

    @Test
    public void isUpToDate() throws Exception {
        File crxFile = CrxProvider.ofDependency().provide();
        System.out.format("ok: %s%n", crxFile);
    }

}