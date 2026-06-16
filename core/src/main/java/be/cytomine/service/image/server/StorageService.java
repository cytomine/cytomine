package be.cytomine.service.image.server;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.security.User;
import be.cytomine.repository.image.server.StorageRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.PermissionService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.JsonObject;

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService extends ModelService {

    private final SecurityACLService securityACLService;

    private final StorageRepository storageRepository;

    private final CurrentUserService currentUserService;

    private final PermissionService permissionService;

    public List<Storage> list() {
        return securityACLService.getStorageList(currentUserService.getCurrentUser(), true);
    }

    public List<Storage> list(User user, String searchString) {
        return securityACLService.getStorageList(user, false, searchString);
    }

    public List<Storage> list(User user) {
        return storageRepository.findAllByUser(user);
    }

    public void initUserStorage(final User user) {
        log.info("Initialize storage for {}", user.getUsername());

        Storage storage = new Storage();
        storage.setUser(user);
        storage.setName(user.getUsername() + " storage");
        storage = storageRepository.save(storage);

        String username = user.getUsername();
        if (!permissionService.hasACLPermission(storage, username, READ)) {
            permissionService.addPermission(storage, storage.getUser().getUsername(), READ, user);
        }
        if (!permissionService.hasACLPermission(storage, username, WRITE)) {
            permissionService.addPermission(storage, storage.getUser().getUsername(), WRITE, user);
        }
        if (!permissionService.hasACLPermission(storage, username, ADMINISTRATION)) {
            permissionService.addPermission(storage, storage.getUser().getUsername(), ADMINISTRATION, user);
        }
    }

    @Override
    public Class currentDomain() {
        return Storage.class;
    }

    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new Storage().buildDomainFromJson(json, getEntityManager());
    }

    @Override
    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        return List.of(domain.getId(), ((Storage) domain).getName());
    }
}
