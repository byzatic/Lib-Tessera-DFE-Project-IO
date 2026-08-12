package io.github.byzatic.lib.configio.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ProjectArchiveExtractorUtilityTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void extractsProjectAndDeletesTemporaryDirectoryOnClose() throws Exception {
        Path archivePath = temporaryFolder.newFile("project.zip").toPath();
        createProjectArchive(archivePath);

        Path temporaryDirectoryPath;

        try (ExtractedProjectDataObject extractedProject =
                     ProjectArchiveExtractorUtility.extract(archivePath)) {
            temporaryDirectoryPath = extractedProject.getTemporaryDirectoryPath();

            assertEquals(
                    archivePath.toAbsolutePath().normalize(),
                    extractedProject.getOriginalArchivePath()
            );
            assertEquals(
                    "MyProject",
                    extractedProject.getExtractedProjectDirectoryPath().getFileName().toString()
            );
            assertTrue(Files.exists(
                    extractedProject.getExtractedProjectDirectoryPath()
                            .resolve("data")
                            .resolve("Project.json")
            ));
            assertFalse(extractedProject.isClosed());
        }

        assertFalse(Files.exists(temporaryDirectoryPath));
    }

    @Test
    public void rejectsZipSlipEntry() throws Exception {
        Path archivePath = temporaryFolder.newFile("malicious.zip").toPath();

        try (OutputStream outputStream = Files.newOutputStream(archivePath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            writeEntry(zipOutputStream, "../outside.txt", "forbidden");
        }

        IOException exception = assertThrows(
                IOException.class,
                new org.junit.function.ThrowingRunnable() {
                    @Override
                    public void run() throws Throwable {
                        ProjectArchiveExtractorUtility.extract(archivePath);
                    }
                }
        );

        assertTrue(exception.getMessage().contains("escapes the temporary directory"));
    }

    private void createProjectArchive(Path archivePath) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(archivePath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            writeEntry(zipOutputStream, "MyProject/data/Project.json", "{\"name\":\"test\"}");
            writeEntry(zipOutputStream, "MyProject/data/Global.json", "{}");
            writeEntry(zipOutputStream, "__MACOSX/metadata", "ignored");
        }
    }

    private void writeEntry(ZipOutputStream zipOutputStream, String name, String value)
            throws IOException {
        ZipEntry zipEntry = new ZipEntry(name);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(value.getBytes(StandardCharsets.UTF_8));
        zipOutputStream.closeEntry();
    }
}
