package io.github.ya_b.registry.client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class IOUtils {

    public static String readString(Path path, Charset charset) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return readString(is, charset);
        }
    }

    public static String readString(InputStream is, Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }


    public static String replacePathChar(String str) {
        return str.replaceAll("[:*?\"<>|]", "/");
    }


}
