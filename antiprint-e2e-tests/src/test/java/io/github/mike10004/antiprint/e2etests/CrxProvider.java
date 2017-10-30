package io.github.mike10004.antiprint.e2etests;

import java.io.File;
import java.io.IOException;

public class CrxProvider {

    public File provide() throws IOException {
        /*
         * Copied by maven-dependency-plugin
         */
        File crxFile = Tests.getBuildDir().toPath()
                .resolve("dependency")
                .resolve("antiprint-artifact.crx")
                .toFile();
        return crxFile;
    }

}
