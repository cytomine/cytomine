package com.cytomine.registry.client.config;


import com.cytomine.registry.client.http.auth.Authenticator;
import com.cytomine.registry.client.http.auth.Credential;

public class Configurer {
    private String url;
    private String userName;
    private String password;
    private boolean authenticated;
    private static Configurer configurer;

    private static final Authenticator AUTHENTICATOR = Authenticator.instance();

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

    public static String url(String url) {
        return configurer.url = url;
    }

    public static void authenticate(String userName, String password) {
        configurer.authenticated = true;
        configurer.userName = userName;
        configurer.password = password;
        AUTHENTICATOR.basic(url, new Credential(configurer.userName, configurer.password));
    }

    public static boolean authenticated() {
       return configurer.authenticated;
    }
}
