package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import io.github.mike10004.crxtool.CrxParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface CrxProvider {

    String SYSPROP_ALLOW_OUTDATED_CRX = "antiprint.e2e.tests.allowOutdatedCrxFile";

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
                if (!isOutdatedCrxAllowed() && !isUpToDate(crxFile)) {
                    throw new IllegalStateException("extension must be re-built because source files are " +
                            "more recent than crx file; execute 'process-test-resources' goal in " +
                            "antiprint-e2e-tests directory");
                }
                return crxFile;
            }
        };
    }

    static boolean isOutdatedCrxAllowed() {
        return Boolean.parseBoolean(System.getProperty(SYSPROP_ALLOW_OUTDATED_CRX, "false"));
    }

    static boolean isUpToDate(File crxFile) throws IOException {
        Unzippage crxUnzipped;
        try (InputStream in = new FileInputStream(crxFile)) {
            CrxParser.getDefault().parseMetadata(in);
            crxUnzipped = Unzippage.unzip(in);
        }
        File extensionSourcesDir = new File(Tests.getProperties().getProperty("project.parent.basedir"), "antiprint-extension");
        Unzippage expected = Tests.pseudoUnzippage(extensionSourcesDir.toPath());
        if (!Tests.filesEqual(expected, crxUnzipped)) {
            List<String> allEntries = ImmutableList.<String>builder()
                    .addAll(crxUnzipped.directoryEntries())
                    .addAll(crxUnzipped.fileEntries())
                    .build();
            System.err.format("%d file/directory entries in crx:%n", allEntries.size());
            allEntries.stream().sorted().forEach(System.err::println);
            for (String entry : expected.fileEntries()) {
                ByteSource actual = crxUnzipped.getFileBytes(entry);
                if (actual == null) {
                    System.err.format("crx does not have file: %s%n", entry);
                }
                if (actual != null && !expected.getFileBytes(entry).contentEquals(actual)) {
                    System.err.format("needs to be updated: %s%n", entry);
                }
            }
            return false;
        }
        return true;
    }

}
