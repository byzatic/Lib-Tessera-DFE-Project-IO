package io.github.byzatic.lib.configio.infrastructure.util;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class GsonJsonFileReaderUtility {

    private final Gson gson;

    public GsonJsonFileReaderUtility() {
        this(new Gson());
    }

    public GsonJsonFileReaderUtility(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    public <T> T read(Path filePath, Class<T> targetClass)
            throws ProjectLoadingException {
        Objects.requireNonNull(filePath, "filePath");
        Objects.requireNonNull(targetClass, "targetClass");

        if (!Files.isRegularFile(filePath)) {
            throw new ProjectLoadingException("Configuration file does not exist: " + filePath);
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            T value = gson.fromJson(reader, targetClass);
            if (value == null) {
                throw new ProjectLoadingException("Configuration file is empty: " + filePath);
            }
            return value;
        } catch (JsonIOException exception) {
            throw new ProjectLoadingException("Cannot read configuration file: " + filePath, exception);
        } catch (JsonParseException exception) {
            throw new ProjectLoadingException("Cannot parse configuration file: " + filePath, exception);
        } catch (IOException exception) {
            throw new ProjectLoadingException("Cannot read configuration file: " + filePath, exception);
        }
    }
}
