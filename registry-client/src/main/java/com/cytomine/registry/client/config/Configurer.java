package com.cytomine.registry.client.config;


import com.cytomine.registry.client.http.auth.Authenticator;
import com.cytomine.registry.client.http.auth.Credential;

public class Configurer {
<<<<<<< HEAD
    private static final Authenticator AUTHENTICATOR = Authenticator.instance();
    private static Configurer configurer;
    private String url;
    private String userName;
    private String password;
    private boolean authenticated;
=======
    private String scheme;
    private String host;
    private String port;
    private String userName;
    private String password;
    private boolean authenticated;
    private static Configurer configurer;

    private static final Authenticator AUTHENTICATOR = Authenticator.instance();
>>>>>>> origin/main

    private Configurer() {
    }

    public static Configurer instance() {
        if (configurer == null) {
            configurer = new Configurer();
            return configurer;
        } else {
            return configurer;
        }
    }

<<<<<<< HEAD
    public static String url(String url) {
        return configurer.url = url;
    }

    public static String url() {
        return configurer.url;
=======
    public static void schema(String schema) {
        configurer.scheme = schema;
    }

    public static String schema() {
        return configurer.scheme;
    }

    public static String host(String host) {
        return configurer.host = host;
    }

    public static String host() {
        return configurer.host;
    }

    public static String port(String port) {
        return configurer.port = port;
    }

    public static String port() {
        return configurer.port;
>>>>>>> origin/main
    }

    public static void authenticate(String userName, String password) {
        configurer.authenticated = true;
        configurer.userName = userName;
        configurer.password = password;
<<<<<<< HEAD
        AUTHENTICATOR.basic(configurer.url, new Credential(configurer.userName,
            configurer.password));
    }

    public static boolean authenticated() {
        return configurer.authenticated;
=======
        String endpoint = String.format("%s://%s:%s" , configurer.scheme , configurer.host , configurer.port);
        AUTHENTICATOR.basic(endpoint, new Credential(configurer.userName, configurer.password));
    }

    public static boolean authenticated() {
       return configurer.authenticated;
>>>>>>> origin/main
    }
}
