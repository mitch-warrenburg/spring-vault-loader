package com.github.vault.springvaultloader.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static com.github.vault.springvaultloader.spring.VaultContextInitializer.VAULT_CONFIG_PATH;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.*;

public class VaultFileUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> void writeJsonFile(T pojoType, String fileName) throws IOException {
        String json = mapper.writeValueAsString(pojoType);
        writeStringToFile(classPathFile(fileName), json, defaultCharset());
    }

    public static void writeJsonFile(String json, String fileName) throws IOException {
        writeStringToFile(classPathFile(fileName), json, defaultCharset());
    }

    public static String readJsonFile(String fileName) throws IOException {
        return readFileToString(classPathFile(fileName), defaultCharset());
    }

    public static boolean doesFileExist(String fileName) {
        return new ClassPathResource(fileName).exists();
    }

    private static File classPathFile(String fileName) throws IOException {

        String filePath = new ClassPathResource(".")
                .getFile()
                .getAbsolutePath()
                .concat("/")
                .concat(fileName)
                .replace("/classes/java", "/resources");

        File file = new File(filePath);

        if (!file.exists()) {
            touch(file);
        }

        return file;
    }
}