package io.github.mike10004.antiprint.e2etests;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

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
        BasicFileAttributes crxAttributes = Files.readAttributes(crxFile.toPath(), BasicFileAttributes.class);
        FileTime crxLastModified = crxAttributes.lastModifiedTime();
        File extensionSourcesDir = new File(Tests.getProperties().getProperty("project.parent.basedir"), "antiprint-extension");
        Collection<File> extensionSourceFiles = FileUtils.listFiles(extensionSourcesDir, null, true);
        FileTime latestSourceLastModified = extensionSourceFiles.stream().map(f -> {
            try {
                return Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                return null;
            }
        }).filter(Objects::nonNull)
          .map(BasicFileAttributes::lastModifiedTime)
          .max(FileTime::compareTo)
          .orElse(null);
        checkState(latestSourceLastModified != null);
        boolean sourcesModifiedLaterThanCrx = latestSourceLastModified.toInstant().isAfter(crxLastModified.toInstant());
        if (sourcesModifiedLaterThanCrx) {
            System.err.format("latest last modified date of crx source file: %s%n", latestSourceLastModified);
            System.err.format("crx last modified: %s (%s)%n", crxLastModified, crxFile);
        }
        return !sourcesModifiedLaterThanCrx;
    }

}
