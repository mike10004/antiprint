package io.github.mike10004.antiprint.e2etests;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CrxProviderTest {

    @Test
    void provide() throws Exception {
        CrxProvider provider = new CrxProvider();
        File file = provider.provide();
        assertTrue(file.isFile(), "not a file: " + file);
    }

}