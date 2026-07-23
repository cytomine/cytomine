package org.cytomine.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cytomine.repository.http.serialization.ObjectMapperFactory;
import org.cytomine.repository.persistence.ConfigurationRepository;
import org.cytomine.repository.persistence.UserRepository;
import org.cytomine.repository.persistence.entity.ConfigurationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

@Slf4j
@SpringBootApplication
@Import(ObjectMapperFactory.class)
public class RepositoryApp {

    private static final String IMAGE_SERVER_USERNAME = "ImageServer1";

    private static final String ADMIN_EMAIL_KEY = "admin_email";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Value("${application.imageServerPrivateKey:}")
    private String imageServerPrivateKey;

    @Value("${application.imageServerPublicKey:}")
    private String imageServerPublicKey;

    @Value("${application.adminEmail:}")
    private String adminEmail;

    public static void main(String[] args) {
        SpringApplication.run(RepositoryApp.class, args);
    }

    @PostConstruct
    public void init() {
        assignImageServerKeys();
        seedAdminEmail();
    }

    private void assignImageServerKeys() {
        if (!StringUtils.hasText(imageServerPrivateKey) || !StringUtils.hasText(imageServerPublicKey)) {
            return;
        }

        userRepository.findByUsername(IMAGE_SERVER_USERNAME).ifPresent(imageServerUser -> {
            imageServerUser.setPrivateKey(imageServerPrivateKey);
            imageServerUser.setPublicKey(imageServerPublicKey);
            userRepository.save(imageServerUser);
            log.info("Assigned image server keys to user {}", IMAGE_SERVER_USERNAME);
        });
    }

    private void seedAdminEmail() {
        if (!StringUtils.hasText(adminEmail) || configurationRepository.findByKey(ADMIN_EMAIL_KEY).isPresent()) {
            return;
        }

        ConfigurationEntity configuration = new ConfigurationEntity();
        configuration.setKey(ADMIN_EMAIL_KEY);
        configuration.setValue(adminEmail);
        configuration.setReadingRole("ADMIN");
        configurationRepository.save(configuration);
        log.info("Seeded {} configuration", ADMIN_EMAIL_KEY);
    }
}
