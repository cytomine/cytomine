package be.cytomine.service.database;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.domain.meta.Configuration;
import be.cytomine.domain.meta.ConfigurationReadingRole;
import be.cytomine.domain.processing.ImageFilter;
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.repository.meta.ConfigurationRepository;
import be.cytomine.repository.processing.ImageFilterRepository;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.SecUserSecRoleRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.image.server.StorageService;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class BootstrapUtilsService {

    private final SecRoleRepository secRoleRepository;

    private final SecUserSecRoleRepository secSecUserSecRoleRepository;

    private final UserRepository userRepository;

    private final StorageService storageService;

    private final ImageFilterRepository imageFilterRepository;

    private final ConfigurationRepository configurationRepository;

    public void createRole(String role) {
        secRoleRepository.createIfNotExist(role);
    }

    public void createUser(String username, String firstname, String lastname, List<String> roles) {
        if (userRepository.findByUsernameLikeIgnoreCase(username).isEmpty()) {
            log.info("Creating {}...", username);
            User user = new User();
            user.setUsername(username);
            user.setName(firstname + " " + lastname);
            user.setReference(UUID.randomUUID().toString());
            user.generateKeys();

            log.info("Saving {}...", user.getUsername());
            user = userRepository.save(user);

            for (String role : roles) {
                SecUserSecRole secSecUserSecRole = new SecUserSecRole();
                secSecUserSecRole.setSecRole(secRoleRepository.getByAuthority(role));
                secSecUserSecRole.setSecUser(user);
                secSecUserSecRoleRepository.save(secSecUserSecRole);
            }

            storageService.initUserStorage(user);
        }
    }

    public void createFilter(String name, String method, Boolean available) {
        ImageFilter filter = imageFilterRepository.findByName(name)
            .orElseGet(ImageFilter::new);
        filter.setName(name);
        filter.setMethod(method);
        filter.setAvailable(available);
        imageFilterRepository.save(filter);
    }

    public void createConfigurations(String key, String value, ConfigurationReadingRole readingRole) {
        Configuration configuration = new Configuration();
        configuration.setKey(key);
        configuration.setValue(value);
        configuration.setReadingRole(readingRole);
        configurationRepository.save(configuration);
    }
}
