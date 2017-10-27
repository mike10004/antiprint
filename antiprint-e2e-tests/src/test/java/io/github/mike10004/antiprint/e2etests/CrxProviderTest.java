package io.github.mike10004.antiprint.e2etests;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CrxProviderTest {

    @Test
    void provide() throws Exception {
        CrxProvider provider = new CrxProvider();
        File existing = provider.getUsualLocation();
        //noinspection ResultOfMethodCallIgnored
        existing.delete();
        File crxFile1 = provider.provide();
        System.out.format("%s provided%n", crxFile1);
        assertTrue(crxFile1.isFile());
        File crxFile2 = provider.provide();
        assertSame(crxFile1, crxFile2);

        File crxFile3 = new CrxProvider() {
            @Override
            protected File rebuild() throws IOException {
                throw new IOException("not supported because we're testing rebuild-not-needed scenario");
            }
        }.provide();
        assertEquals(crxFile1, crxFile3);
    }

}