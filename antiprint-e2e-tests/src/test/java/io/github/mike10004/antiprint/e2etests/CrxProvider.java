package io.github.mike10004.antiprint.e2etests;

import com.github.mike10004.nativehelper.Program;
import com.github.mike10004.nativehelper.ProgramWithOutputStrings;
import com.github.mike10004.nativehelper.ProgramWithOutputStringsResult;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public class CrxProvider {

    private static File crxFile;

    public File provide() throws IOException {
        if (crxFile == null) {
            crxFile = doProvide();
        }
        return crxFile;
    }

    private File doProvide() throws IOException {
        File usualLocation = getUsualLocation();
        if (usualLocation.exists()) {
            checkState(usualLocation.isFile(), "not a file? %s", usualLocation);
            long crxCreationTime = getCreationTime(usualLocation);
            @Nullable Long mostRecentModTime = listExtensionSourceFiles().stream()
                    .map(file -> getLongAttribute(file, attr -> attr.lastModifiedTime().toMillis()))
                    .max(Long::compareTo)
                    .orElse(null);
            if (mostRecentModTime != null && mostRecentModTime < crxCreationTime) {
                return usualLocation;
            }
        }
        return rebuild();
    }

    protected File rebuild() throws IOException {
        String packExtensionKey = Tests.getProperty("crx.packExtensionKey");
        Iterable<String> moreArgs = Stream.of(packExtensionKey).filter(Objects::nonNull).collect(Collectors.toSet());
        ProgramWithOutputStrings program = Program.running(findPackExtensionScript())
                .from(Tests.getParentBaseDir())
                .args(moreArgs)
                .outputToStrings();
        ProgramWithOutputStringsResult result = program.execute();
        if (result.getExitCode() == 0) {
            String outputCrxPathname = result.getStdoutString();
            File crxFile = new File(outputCrxPathname);
            File usualLocation = getUsualLocation();
            if (!crxFile.getCanonicalFile().equals(usualLocation.getCanonicalFile())) {
                throw new IOException("output file produced in unexpected location; expected " + crxFile + " to be at " + usualLocation);
            }
            return crxFile;
        } else {
            result.getStderr().copyTo(System.err);
            throw new IllegalStateException("rebuild exited dirty: " + result.getExitCode());
        }
    }

    File getUsualLocation() {
        return new File(Tests.getParentBaseDir(), "antiprint-extension.crx");
    }

    private static long getCreationTime(File file) {
        return getLongAttribute(file, attr -> attr.creationTime().toMillis());
    }

    private static long getLongAttribute(File file, Function<? super BasicFileAttributes, Long> attrGetter) {
        try {
            BasicFileAttributes attributes = java.nio.file.Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attrGetter.apply(attributes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File findPackExtensionScript() throws FileNotFoundException {
        return new File(Tests.getParentBaseDir(), "pack-extension.sh");
    }


    private static ImmutableList<File> listExtensionSourceFiles() throws IOException {
        Collection<File> files = FileUtils.listFiles(new File(Tests.getParentBaseDir(), "antiprint-extension"), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
        return ImmutableList.copyOf(files);
    }

}
