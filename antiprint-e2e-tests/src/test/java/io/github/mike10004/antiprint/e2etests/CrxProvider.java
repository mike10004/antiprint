package io.github.mike10004.antiprint.e2etests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface CrxProvider {

    File provide() throws IOException;

    /*
     * Gets a provider implementation that provides the file copied by the
     * maven-dependency-plugin.
     */
    static CrxProvider ofDependency() {
        return new CrxProvider() {
            @Override
            public File provide() throws IOException {
                File crxFile = Tests.getBuildDir().toPath()
                        .resolve("dependency")
                        .resolve("antiprint-artifact.crx")
                        .toFile();
                if (!crxFile.isFile()) {
                    throw new FileNotFoundException("make sure to the process-test-resources phase " +
                            "is executed before this test; an IDE that runs a test may not execute the " +
                            "preparatory phases and if the project was recently cleaned, the dependency files " +
                            "will not have been copied yet; expected file at " + crxFile);
                }
                return crxFile;
            }
        };
    }


}
