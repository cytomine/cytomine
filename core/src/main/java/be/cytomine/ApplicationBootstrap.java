package be.cytomine;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.config.nosqlmigration.InitialMongodbSetupMigration;
import be.cytomine.config.properties.ApplicationProperties;
import be.cytomine.domain.meta.Configuration;
import be.cytomine.domain.meta.ConfigurationReadingRole;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.meta.ConfigurationRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.database.BootstrapDataService;
import be.cytomine.service.database.BootstrapTestsDataService;
import be.cytomine.service.database.BootstrapUtilsService;
import be.cytomine.service.utils.Dataset;
import be.cytomine.utils.EnvironmentUtils;

import static be.cytomine.service.database.BootstrapTestsDataService.ADMIN;
import static be.cytomine.service.database.BootstrapTestsDataService.CREATOR;
import static be.cytomine.service.database.BootstrapTestsDataService.GUEST;
import static be.cytomine.service.database.BootstrapTestsDataService.SUPERADMIN;
import static be.cytomine.service.database.BootstrapTestsDataService.USER_ACL_ADMIN;
import static be.cytomine.service.database.BootstrapTestsDataService.USER_ACL_CREATE;
import static be.cytomine.service.database.BootstrapTestsDataService.USER_ACL_DELETE;
import static be.cytomine.service.database.BootstrapTestsDataService.USER_ACL_READ;
import static be.cytomine.service.database.BootstrapTestsDataService.USER_ACL_WRITE;
import static be.cytomine.service.database.BootstrapTestsDataService.USER_NO_ACL;
import be.cytomine.service.UrlApi;

@Component
@Order(0)
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
@Transactional
class ApplicationBootstrap {

    private final ApplicationProperties applicationProperties;

    private final Environment environment;

    private final InitialMongodbSetupMigration initialSetupMigration;

    private final UserRepository userRepository;

    private final ConfigurationRepository configurationRepository;

    @PostConstruct
    public void init() {

        initialSetupMigration.changeSet();

        log.info("#############################################################################");
        log.info("#############################################################################");
        log.info("#############################################################################");
        String cytomineWelcomeMessage = """

                               _____      _                  _
                              / ____|    | |                (_)
                             | |    _   _| |_ ___  _ __ ___  _ _ __   ___
                             | |   | | | | __/ _ \\| '_ ` _ \\| | '_ \\ / _ \\
                             | |___| |_| | || (_) | | | | | | | | | |  __/
                              \\_____\\__, |\\__\\___/|_| |_| |_|_|_| |_|\\___|
                             |  _ \\  __/ |     | |     | |
                             | |_) ||___/  ___ | |_ ___| |_ _ __ __ _ _ __
                             |  _ < / _ \\ / _ \\| __/ __| __| '__/ _` | '_ \\
                             | |_) | (_) | (_) | |_\\__ \\ |_| | | (_| | |_) |
                             |____/ \\___/ \\___/ \\__|___/\\__|_|  \\__,_| .__/
                                                                     | |
                                                                     |_|\
            """;
        log.info(cytomineWelcomeMessage);
        log.info("#############################################################################");
        log.info("#############################################################################");
        log.info("#############################################################################");
        log.info("Environment:" + Arrays.toString(environment.getActiveProfiles()));
        log.info("Current directory:" + new File("./").getAbsolutePath());
        log.info("HeadLess:" + java.awt.GraphicsEnvironment.isHeadless());
        log.info("JVM Args" + ManagementFactory.getRuntimeMXBean().getInputArguments());
        log.info(applicationProperties.toString());
        log.info("#############################################################################");
        log.info("#############################################################################");
        log.info("#############################################################################");

        UrlApi.setServerURL(applicationProperties.getServerURL());

        if (configurationRepository.findByKey("admin_email").isEmpty()) {
            Configuration configuration = new Configuration();
            configuration.setKey("admin_email");
            configuration.setValue(applicationProperties.getAdminEmail());
            configuration.setReadingRole(ConfigurationReadingRole.ADMIN);
            configurationRepository.save(configuration);
        }

        // Deprecated API keys. Will be removed in a future release. Still used for communication PIMS->core for now
        String privateKey = applicationProperties.getImageServerPrivateKey();
        String publicKey = applicationProperties.getImageServerPublicKey();
        if (privateKey != null && publicKey != null) {
            User imageServerUser = userRepository.findByUsernameLikeIgnoreCase("ImageServer1")
                .orElseThrow(() -> new ObjectNotFoundException("No user imageserver1, cannot assign keys"));
            imageServerUser.setPrivateKey(applicationProperties.getImageServerPrivateKey());
            imageServerUser.setPublicKey(applicationProperties.getImageServerPublicKey());
            userRepository.save(imageServerUser);
        }

        log.info("#############################################################################");
        log.info("###################              READY              #########################");
        log.info("#############################################################################");
    }
}
