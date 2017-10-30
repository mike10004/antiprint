package io.github.mike10004.antiprint.e2etests;


import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class CrxProviderTest {

    @Test
    public void provide() throws Exception {
        CrxProvider provider = new CrxProvider();
        File file = provider.provide();
        Assert.assertTrue("make sure to run the process-test-resources phase before executing this test; not a file: " + file, file.isFile());
    }

}