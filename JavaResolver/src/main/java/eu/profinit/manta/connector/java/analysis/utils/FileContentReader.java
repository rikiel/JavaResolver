package eu.profinit.manta.connector.java.analysis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Helper for reading content of a file from JARs or classpath
 */
public class FileContentReader {
    private static final Logger log = LoggerFactory.getLogger(FileContentReader.class);

    private final List<JarFile> jarFiles;

    public FileContentReader() {
        this(ImmutableList.of());
    }

    public FileContentReader(@Nonnull final List<JarFile> jarFiles) {
        Validate.notNull(jarFiles);
        this.jarFiles = jarFiles;

        log.trace("Using jar files: {}", jarFiles.stream().map(JarFile::getName).collect(Collectors.joining(", ", "[", "]")));
    }

    @Nonnull
    public String readFile(@Nonnull final String fileName) throws FileReadingException {
        try {
            // Try to find file in JAR files
            for (JarFile jarFile : jarFiles) {
                final ZipEntry entry = jarFile.getEntry(fileName);
                if (entry != null) {
                    log.trace("Reading file '{}' from JAR '{}'", fileName, jarFile.getName());
                    return readFile(jarFile.getInputStream(entry));
                }
            }

            // Try to find file on classpath
            final InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
            if (stream != null) {
                log.trace("Reading file '{}' from classloader", fileName);
                return readFile(stream);
            }
        } catch (IOException e) {
            // file was found, but failed to read it
            log.error("Failed to read a file '{}'", fileName, e);
            throw new FileReadingException("Failed to read a file " + fileName, e);
        }

        throw new FileReadingException("File was not found: " + fileName);
    }

    @Nonnull
    private String readFile(@Nonnull final InputStream stream) throws IOException {
        return IOUtils.toString(stream, StandardCharsets.UTF_8);
    }

    public static final class FileReadingException extends IOException {
        FileReadingException(String message) {
            super(message);
        }

        FileReadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
