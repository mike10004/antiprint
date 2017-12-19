package io.github.mike10004.antiprint.e2etests;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import io.github.mike10004.crxtool.CrxParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public interface ExtensionFileProvider {

    String SYSPROP_ALLOW_OUTDATED_EXTENSION_FILE = "antiprint.e2e.tests.allowOutdatedExtensionFile";

    File provide() throws IOException;

    /*
     * Gets a provider implementation that provides the file copied by the
     * maven-dependency-plugin.
     */
    static ExtensionFileProvider ofDependency(ExtensionFileFormat format) {
        return new ExtensionFileProvider() {
            @Override
            public File provide() throws IOException {
                File extensionFile = Tests.getBuildDir().toPath()
                        .resolve("dependency")
                        .resolve("antiprint-extension" + format.suffix())
                        .toFile();
                if (!extensionFile.isFile()) {
                    throw new FileNotFoundException("make sure to the process-test-resources phase " +
                            "is executed before this test; an IDE that runs a test may not execute the " +
                            "preparatory phases and if the project was recently cleaned, the dependency files " +
                            "will not have been copied yet; expected file at " + extensionFile);
                }
                if (!isOutdatedCrxAllowed() && !isUpToDate(extensionFile, format)) {
                    throw new IllegalStateException("extension must be re-built because source files are " +
                            "more recent than crx/zip file; execute 'process-test-resources' goal in " +
                            "antiprint-e2e-tests directory");
                }
                return extensionFile;
            }
        };
    }

    static boolean isOutdatedCrxAllowed() {
        return Boolean.parseBoolean(System.getProperty(SYSPROP_ALLOW_OUTDATED_EXTENSION_FILE, "false"));
    }

    static boolean isUpToDate(File extensionFile, ExtensionFileFormat format) throws java.io.IOException {
        Unzippage unzipped;
        try (InputStream in = new FileInputStream(extensionFile)) {
            if (format == ExtensionFileFormat.CRX) {
                CrxParser.getDefault().parseMetadata(in);
            }
            unzipped = Unzippage.unzip(in);
        }
        File projectBasedir = new File(Tests.getProperties().getProperty("project.parent.basedir"), "antiprint-extension");
        checkState(projectBasedir.isDirectory(), "not a directory: %s", projectBasedir);
        File extensionSourcesDir = new File(projectBasedir, "src/main/extension");
        checkState(extensionSourcesDir.isDirectory(), "not a directory: %s", extensionSourcesDir);
        Unzippage expected = Tests.pseudoUnzippage(extensionSourcesDir.toPath());
        if (!Tests.filesEqual(expected, unzipped)) {
            List<String> allEntries = ImmutableList.<String>builder()
                    .addAll(unzipped.directoryEntries())
                    .addAll(unzipped.fileEntries())
                    .build();
            System.err.format("%d file/directory entries in extension archive:%n", allEntries.size());
            allEntries.stream().sorted().forEach(System.err::println);
            for (String entry : expected.fileEntries()) {
                ByteSource actual = unzipped.getFileBytes(entry);
                if (actual == null) {
                    System.err.format("extension archive does not have file: %s%n", entry);
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
