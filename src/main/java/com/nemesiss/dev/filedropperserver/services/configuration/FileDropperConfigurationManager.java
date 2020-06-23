package com.nemesiss.dev.filedropperserver.services.configuration;

import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;
import com.nemesiss.dev.filedropperserver.utils.AppUtils;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.util.UUID;

@AllArgsConstructor
public class FileDropperConfigurationManager {

    @Getter
    ServerConfiguration serverConfiguration;

    private static final Object WRITE_FILE_LOCK = new Object();

    public static void writeConfigurationToPathAsJsonString(ServerConfiguration configObject, String writePath)
            throws IllegalArgumentException, IllegalStateException, IOException {
        String jsonText = configObject.toJsonString();
        File writePathFile = new File(writePath);
        if (writePathFile.isDirectory()) {
            writePathFile = Paths.get(writePath, FileDropperConfigurationLoader.DEFAULT_CONFIGURATION_NAME).toFile();
        }
        String realRootDirectory = writePathFile.getParent();
        String realFileName = writePathFile.getName();
        String temporaryFileName = getTempFileName();

        File temporaryWritePath = new File(realRootDirectory, temporaryFileName);
        synchronized (WRITE_FILE_LOCK) {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temporaryWritePath));
            IOUtils.write(jsonText, bos, StandardCharsets.UTF_8);
            bos.close();
            if (temporaryWritePath.canRead()) {
                boolean casFinished = false;
                for (int i = 0; i < 50; ++i) {
                    if (temporaryWritePath.renameTo(writePathFile)) {
                        casFinished = true;
                        break;
                    }
                }
                if (!casFinished) {
                    throw new IllegalStateException("Cannot rename temporary file name: " + temporaryFileName + " to correct file name: " + realFileName + ". " +
                            "New server configuration may not be applied after restart server application.");
                }
            }
        }
    }

    public void writeConfigurationToPathAsJsonString(String writePath) throws IllegalArgumentException, IOException {
        writeConfigurationToPathAsJsonString(serverConfiguration, writePath);
    }

    public static String getTempFileName() {
        return UUID.randomUUID().toString() + ".json";
    }

    public static String getConfigurationFileStorePathWithDefaultName(String rootDirectory) {
        return Paths.get(rootDirectory, FileDropperConfigurationLoader.DEFAULT_CONFIGURATION_NAME).toFile().getAbsolutePath();
    }
}
