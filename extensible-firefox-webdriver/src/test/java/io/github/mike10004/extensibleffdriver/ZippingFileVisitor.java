package io.github.mike10004.extensibleffdriver;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZippingFileVisitor implements FileVisitor<Path> {

    private final Path enclosure;
    private final ZipOutputStream zipOutputStream;

    public ZippingFileVisitor(Path enclosure, ZipOutputStream zipOutputStream) {
        this.enclosure = enclosure;
        this.zipOutputStream = zipOutputStream;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (!dir.toFile().getAbsoluteFile().equals(enclosure.toFile().getAbsoluteFile())) {
            String relativeDirName = normalize(dir) + "/";
            zipOutputStream.putNextEntry(new ZipEntry(relativeDirName));
            zipOutputStream.closeEntry();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileEntryName = normalize(file);
        ZipEntry entry = new ZipEntry(fileEntryName);
        zipOutputStream.putNextEntry(entry);
        com.google.common.io.Files.asByteSource(file.toFile()).copyTo(zipOutputStream);
        zipOutputStream.closeEntry();
        return FileVisitResult.CONTINUE;
    }

    private String normalize(Path path) {
        Path relativeDir = enclosure.relativize(path);
        return FilenameUtils.normalizeNoEndSeparator(relativeDir.toString(), true);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) {
            throw exc;
        }
        return FileVisitResult.CONTINUE;
    }

    public static void zip(Path enclosure, File zipFile) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walkFileTree(enclosure, new ZippingFileVisitor(enclosure, zipOutputStream));
        }
    }

}
