package io.github.byzatic.tessera.lib.configio.infrastructure.saver;

import io.github.byzatic.tessera.lib.configio.application.module.ModuleSaverInterface;
import io.github.byzatic.tessera.lib.configio.application.service.ServiceSaverInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginSavingException;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.ModuleSaverFactory;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.ServiceSaverFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PluginSaverStrategyTest {

    @Test
    public void shouldSaveModuleAndServiceJarsIntoCreatedProject() throws Exception {
        Path testDirectory = Files.createTempDirectory("plugin-saver-test-");
        Path projectDirectory = testDirectory.resolve("created-project");
        Path firstSourceDirectory = Files.createDirectories(
                testDirectory.resolve("first-source")
        );
        Path secondSourceDirectory = Files.createDirectories(
                testDirectory.resolve("second-source")
        );
        byte[] firstContent = new byte[]{1, 2, 3};
        byte[] replacementContent = new byte[]{4, 5, 6};
        Path firstJar = Files.write(
                firstSourceDirectory.resolve("plugin.jar"),
                firstContent
        );
        Path replacementJar = Files.write(
                secondSourceDirectory.resolve("plugin.jar"),
                replacementContent
        );

        try {
            ModuleSaverInterface moduleSaver = ModuleSaverFactory.create();
            ServiceSaverInterface serviceSaver = ServiceSaverFactory.create();

            Path moduleJar = moduleSaver.save(firstJar, projectDirectory);
            Path serviceJar = serviceSaver.save(firstJar, projectDirectory);

            assertEquals(
                    projectDirectory.toAbsolutePath().normalize()
                            .resolve("modules")
                            .resolve("workflow_routines")
                            .resolve("plugin.jar"),
                    moduleJar
            );
            assertEquals(
                    projectDirectory.toAbsolutePath().normalize()
                            .resolve("modules")
                            .resolve("services")
                            .resolve("plugin.jar"),
                    serviceJar
            );
            assertTrue(Files.isRegularFile(moduleJar));
            assertTrue(Files.isRegularFile(serviceJar));
            assertArrayEquals(firstContent, Files.readAllBytes(moduleJar));
            assertArrayEquals(firstContent, Files.readAllBytes(serviceJar));

            Path replacedModuleJar = moduleSaver.save(
                    replacementJar,
                    projectDirectory
            );
            assertEquals(moduleJar, replacedModuleJar);
            assertArrayEquals(
                    replacementContent,
                    Files.readAllBytes(replacedModuleJar)
            );
        } finally {
            deleteTree(testDirectory);
        }
    }

    @Test(expected = PluginSavingException.class)
    public void shouldRejectFileWithoutJarExtension() throws Exception {
        Path testDirectory = Files.createTempDirectory("plugin-saver-invalid-test-");
        Path invalidFile = Files.write(
                testDirectory.resolve("module.bin"),
                new byte[]{1}
        );

        try {
            ModuleSaverFactory.create().save(
                    invalidFile,
                    testDirectory.resolve("project")
            );
        } finally {
            deleteTree(testDirectory);
        }
    }

    private void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(
                    Path directory,
                    IOException exception
            ) throws IOException {
                if (exception != null) {
                    throw exception;
                }
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
