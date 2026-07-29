package be.cytomine.utils;

import java.util.Arrays;

import org.springframework.core.env.Environment;

public class EnvironmentUtils {

    public static boolean isTest(Environment environment) {
        return Arrays.asList(environment.getActiveProfiles()).contains("test");
    }

}
