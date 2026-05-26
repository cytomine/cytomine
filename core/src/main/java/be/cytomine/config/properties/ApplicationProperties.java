package be.cytomine.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class ApplicationProperties {

    private String serverId;

    private String version;

    private String serverURL;

    private String storagePath;

    private CustomUIProperties customUI;

    private AuthenticationProperties authentication = new AuthenticationProperties();

    private AppEngineProperties appEngine;

    private String instanceHostWebsite;

    private String instanceHostSupportMail;

    private String instanceHostPhoneNumber;

    @NotNull
    @NotBlank
    private String adminPassword;

    @NotNull
    @NotBlank
    private String adminEmail;

    @NotNull
    @NotBlank
    private String adminPrivateKey;

    @NotNull
    @NotBlank
    private String adminPublicKey;

    @NotNull
    @NotBlank
    private String superAdminPrivateKey;

    @NotNull
    @NotBlank
    private String superAdminPublicKey;

    private String imageServerPrivateKey;

    private String imageServerPublicKey;

    private String defaultLanguage;

    private Annotation annotation;

    @ToString
    @Getter
    @Setter
    public static class Annotation {
        int maxNumberOfPoint;

    }
}
