package io.github.mike10004.antiprint.e2etests;

/*
 * MIT License
 * 
 * Copyright (c) 2017 Mike Chaberski
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class that represents the result of unzipping a file. Use {@link #unzip(File)} or
 * {@link #unzip(InputStream)} create an instance.
 */
public abstract class Unzippage {

    protected Unzippage() {}

    /**
     * Returns an iterable over the names of zip entries that represent compressed files.
     * @return the file entries
     */
    public abstract Iterable<String> fileEntries();

    /**
     * Returns an iterable over the names of zip entries that represent directories.
     * @return the directory entries
     */
    public abstract Iterable<String> directoryEntries();

    /**
     * Returns a byte source that supplies a stream containing the decompressed
     * bytes of a zip entry.
     * @param fileEntry the file entry
     * @return the byte source
     */
    public abstract ByteSource getFileBytes(String fileEntry);

    private static class CollectionUnzippage extends Unzippage {

        private final ImmutableList<String> directoryEntries;
        private final ImmutableMap<String, ByteSource> fileEntries;

        protected CollectionUnzippage(Iterable<String> directoryEntries, Map<String, ByteSource> fileEntries) {
            this.directoryEntries = ImmutableList.copyOf(directoryEntries);
            this.fileEntries = ImmutableMap.copyOf(fileEntries);
        }

        @Override
        public Iterable<String> fileEntries() {
            return fileEntries.keySet();
        }

        @Override
        public Iterable<String> directoryEntries() {
            return directoryEntries;
        }

        @Override
        public ByteSource getFileBytes(String fileEntry) {
            return fileEntries.get(fileEntry);
        }
    }

    /**
     * Unzips data from an input stream. The stream must be open and positioned
     * at the beginning of the zip data.
     * @param inputStream the input stream
     * @return the unzippage
     * @throws IOException if something goes awry
     */
    public static Unzippage unzip(InputStream inputStream) throws IOException {
        return unzip(new StreamZipFacade(inputStream));
    }

    private static class StreamZipFacade implements ZipFacade {

        private final ZipInputStream inputStream;

        private StreamZipFacade(InputStream inputStream) {
            this.inputStream = new ZipInputStream(inputStream);
        }

        private class StreamEntryFacade implements EntryFacade {

            private final ZipEntry entry;

            private StreamEntryFacade(ZipEntry entry) {
                this.entry = Objects.requireNonNull(entry);
            }

            @Override
            public InputStream openStream() throws IOException {
                return new FilterInputStream(inputStream) {
                    @Override
                    public void close() throws IOException {
                        inputStream.closeEntry();
                    }
                };
            }

            @Override
            public ZipEntry getEntry() {
                return entry;
            }
        }

        @Nullable
        @Override
        public EntryFacade next() throws IOException {
            ZipEntry entry = inputStream.getNextEntry();
            if (entry == null) {
                return null;
            }
            return new StreamEntryFacade(entry);
        }

    }

    /**
     * Unzips a zip file.
     * @param zipPathname the pathname of the zip file
     * @return the unzippage
     * @throws IOException if something goes awry
     */
    public static Unzippage unzip(File zipPathname) throws IOException {
        try (ZipFile zf = new ZipFile(zipPathname)) {
            return unzip(new FileZipFacade(zf));
        }
    }

    private interface ZipFacade {
        @Nullable
        EntryFacade next() throws IOException;
    }

    private interface EntryFacade {
        InputStream openStream() throws IOException;
        ZipEntry getEntry();
    }

    private static class FileZipFacade implements ZipFacade {

        private final ZipFile zipFile;
        private final Iterator<? extends ZipEntry> entries;

        public FileZipFacade(ZipFile zipFile) throws IOException {
            entries = zipFile.stream().iterator();
            this.zipFile = zipFile;
        }

        @Nullable
        @Override
        public EntryFacade next() throws IOException {
            if (entries.hasNext()) {
                ZipEntry entry = entries.next();
                return new EntryFacade() {
                    @Override
                    public ZipEntry getEntry() {
                        return entry;
                    }

                    @Override
                    public InputStream openStream() throws IOException {
                        return zipFile.getInputStream(entry);
                    }
                };
            } else {
                return null;
            }
        }

    }

    private static Unzippage unzip(ZipFacade entryProvider) throws IOException {
        List<String> directoryEntries = new ArrayList<>();
        Map<String, byte[]> fileEntries = new HashMap<>();
        EntryFacade session;
        while ((session = entryProvider.next()) != null) {
            ZipEntry entry = session.getEntry();
            if (entry.isDirectory()) {
                directoryEntries.add(entry.getName());
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(Ints.checkedCast(entry.getSize()));
                try (InputStream input = session.openStream()) {
                    ByteStreams.copy(input, baos);
                }
                baos.flush();
                fileEntries.put(entry.getName(), baos.toByteArray());
            }
        }
        return new CollectionUnzippage(directoryEntries, Maps.transformValues(fileEntries, ByteSource::wrap));
    }

    public static class UnzippageTest {

        private static final Set<String> requiredReferenceDirs = ImmutableSet.of("ziptest/d1/d4/");
        private static final Map<String, byte[]> requiredReferenceFiles = ImmutableMap.of(
                                                                    "ziptest/d1/d2/d3/a", "a\n".getBytes(US_ASCII),
                                                                    "ziptest/d1/d5/b", "b\n".getBytes(US_ASCII),
                                                                    "ziptest/d1/d", "d\n".getBytes(US_ASCII),
                                                                    "ziptest/d1/c", "c\n".getBytes(US_ASCII)
                                                                    );
        @Test
        public void unzipFile() throws Exception {
            File zipFile = File.createTempFile("reference", ".zip");
            Files.write(zipFile.toPath(), getReferenceZipBytes());
            Unzippage unzippage = unzip(zipFile);
            check(unzippage);
        }

        private void check(Unzippage unzippage) throws IOException {
            System.out.format("directories: %s%n", ImmutableList.copyOf(unzippage.directoryEntries()));
            System.out.format("files: %s%n", ImmutableList.copyOf(unzippage.fileEntries()));
            assertTrue("contains empty reference dir", ImmutableSet.copyOf(unzippage.directoryEntries()).containsAll(requiredReferenceDirs));
            Map<String, byte[]> arrayValues = new HashMap<>();
            for (String entryName : unzippage.fileEntries()) {
                arrayValues.put(entryName, unzippage.getFileBytes(entryName).read());
            }
            assertEquals("files", requiredReferenceFiles.keySet(), arrayValues.keySet());
            requiredReferenceFiles.forEach((entryName, bytes) -> {
                assertArrayEquals(entryName, bytes, arrayValues.get(entryName));
            });
        }

        @Test
        public void unzipStream() throws Exception {
            byte[] bytes = getReferenceZipBytes();
            Unzippage unzippage;
            try (InputStream stream = new ByteArrayInputStream(bytes)) {
                unzippage = unzip(stream);
            }
            check(unzippage);
        }

        private static byte[] getReferenceZipBytes() {
            return Base64.getDecoder().decode(REFERENCE_ZIP_BASE64);
        }

        public static final String REFERENCE_ZIP_BASE64 =
                "UEsDBAoAAAAAADhtdUsAAAAAAAAAAAAAAAAIABwAemlwdGVzdC9VVAkAA2tzFFq8cxRadXgLAAEE" +
                "6AMAAAToAwAAUEsDBAoAAAAAAFVtdUsAAAAAAAAAAAAAAAALABwAemlwdGVzdC9kMS9VVAkAA6Jz" +
                "FFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoAAAAAADptdUsAAAAAAAAAAAAAAAAOABwAemlwdGVz" +
                "dC9kMS9kNC9VVAkAA29zFFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoAAAAAADhtdUsAAAAAAAAA" +
                "AAAAAAAOABwAemlwdGVzdC9kMS9kMi9VVAkAA2tzFFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoA" +
                "AAAAAEdtdUsAAAAAAAAAAAAAAAARABwAemlwdGVzdC9kMS9kMi9kMy9VVAkAA4VzFFq8cxRadXgL" +
                "AAEE6AMAAAToAwAAUEsDBAoAAAAAAEdtdUsHoerdAgAAAAIAAAASABwAemlwdGVzdC9kMS9kMi9k" +
                "My9hVVQJAAOFcxRahXMUWnV4CwABBOgDAAAE6AMAAGEKUEsDBAoAAAAAAFBtdUsAAAAAAAAAAAAA" +
                "AAAOABwAemlwdGVzdC9kMS9kNS9VVAkAA5hzFFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoAAAAA" +
                "AFBtdUvE8sf2AgAAAAIAAAAPABwAemlwdGVzdC9kMS9kNS9iVVQJAAOYcxRamHMUWnV4CwABBOgD" +
                "AAAE6AMAAGIKUEsDBAoAAAAAAFVtdUtCVZ2gAgAAAAIAAAAMABwAemlwdGVzdC9kMS9kVVQJAAOi" +
                "cxRaonMUWnV4CwABBOgDAAAE6AMAAGQKUEsDBAoAAAAAAFNtdUuFw9zvAgAAAAIAAAAMABwAemlw" +
                "dGVzdC9kMS9jVVQJAAOdcxRanXMUWnV4CwABBOgDAAAE6AMAAGMKUEsBAh4DCgAAAAAAOG11SwAA" +
                "AAAAAAAAAAAAAAgAGAAAAAAAAAAQAO1BAAAAAHppcHRlc3QvVVQFAANrcxRadXgLAAEE6AMAAATo" +
                "AwAAUEsBAh4DCgAAAAAAVW11SwAAAAAAAAAAAAAAAAsAGAAAAAAAAAAQAO1BQgAAAHppcHRlc3Qv" +
                "ZDEvVVQFAAOicxRadXgLAAEE6AMAAAToAwAAUEsBAh4DCgAAAAAAOm11SwAAAAAAAAAAAAAAAA4A" +
                "GAAAAAAAAAAQAO1BhwAAAHppcHRlc3QvZDEvZDQvVVQFAANvcxRadXgLAAEE6AMAAAToAwAAUEsB" +
                "Ah4DCgAAAAAAOG11SwAAAAAAAAAAAAAAAA4AGAAAAAAAAAAQAO1BzwAAAHppcHRlc3QvZDEvZDIv" +
                "VVQFAANrcxRadXgLAAEE6AMAAAToAwAAUEsBAh4DCgAAAAAAR211SwAAAAAAAAAAAAAAABEAGAAA" +
                "AAAAAAAQAO1BFwEAAHppcHRlc3QvZDEvZDIvZDMvVVQFAAOFcxRadXgLAAEE6AMAAAToAwAAUEsB" +
                "Ah4DCgAAAAAAR211Sweh6t0CAAAAAgAAABIAGAAAAAAAAQAAAKSBYgEAAHppcHRlc3QvZDEvZDIv" +
                "ZDMvYVVUBQADhXMUWnV4CwABBOgDAAAE6AMAAFBLAQIeAwoAAAAAAFBtdUsAAAAAAAAAAAAAAAAO" +
                "ABgAAAAAAAAAEADtQbABAAB6aXB0ZXN0L2QxL2Q1L1VUBQADmHMUWnV4CwABBOgDAAAE6AMAAFBL" +
                "AQIeAwoAAAAAAFBtdUvE8sf2AgAAAAIAAAAPABgAAAAAAAEAAACkgfgBAAB6aXB0ZXN0L2QxL2Q1" +
                "L2JVVAUAA5hzFFp1eAsAAQToAwAABOgDAABQSwECHgMKAAAAAABVbXVLQlWdoAIAAAACAAAADAAY" +
                "AAAAAAABAAAApIFDAgAAemlwdGVzdC9kMS9kVVQFAAOicxRadXgLAAEE6AMAAAToAwAAUEsBAh4D" +
                "CgAAAAAAU211S4XD3O8CAAAAAgAAAAwAGAAAAAAAAQAAAKSBiwIAAHppcHRlc3QvZDEvY1VUBQAD" +
                "nXMUWnV4CwABBOgDAAAE6AMAAFBLBQYAAAAACgAKAEMDAADTAgAAAAA=";

    }
}
