package be.cytomine.appengine.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import be.cytomine.appengine.dto.inputs.task.TaskDescription;
import be.cytomine.appengine.exceptions.AppStoreServiceException;
import be.cytomine.appengine.exceptions.BundleArchiveException;
import be.cytomine.appengine.exceptions.TaskServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import be.cytomine.appengine.dto.responses.errors.ErrorBuilder;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.AppStoreNotFoundException;
import be.cytomine.appengine.exceptions.ValidationException;
import be.cytomine.appengine.models.store.AppStore;
import be.cytomine.appengine.repositories.AppStoreRepository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppStoreService {

    private final AppStoreRepository appStoreRepository;
    private final TaskService taskService;

    public Optional<AppStore> findDefault() {
        return appStoreRepository.findByDefaultStoreIsTrue();
    }

    public List<AppStore> list() {
        return appStoreRepository.findAll();
    }

    public void delete(UUID id) {
        appStoreRepository.deleteById(id);
    }

    @Transactional
    public void makeDefault(UUID id) throws AppStoreNotFoundException {
        log.info("Setting store [{}] as default", id);
        Optional<AppStore> defaultStore = appStoreRepository.findByDefaultStoreIsTrue();
        if (defaultStore.isPresent()) {
            AppStore store = defaultStore.get();
            store.setDefaultStore(false);
            appStoreRepository.saveAndFlush(store);
        }
        Optional<AppStore> appStore = appStoreRepository.findById(id);
        AppStore store;
        if (appStore.isPresent()) {
            store = appStore.get();
            store.setDefaultStore(true);
            appStoreRepository.saveAndFlush(store);
        } else {
            AppEngineError error = ErrorBuilder.build(
                ErrorCode.INTERNAL_INVALID_STORE_NOT_FOUND);
            throw new AppStoreNotFoundException(error);
        }
        log.info("Successfully set store [{}] '{}' as default", id, store.getName());
    }

    public AppStore save(AppStore appStore) throws ValidationException {
        log.info("saving app store {}", appStore);
        if (Objects.isNull(appStore.getName()) || appStore.getName().isBlank()) {
            AppEngineError error = ErrorBuilder.build(
                ErrorCode.INTERNAL_INVALID_STORE_DATA);
            throw new ValidationException(error);
        }
        if (Objects.isNull(appStore.getHost()) || appStore.getHost().isBlank()) {
            AppEngineError error = ErrorBuilder.build(
                ErrorCode.INTERNAL_INVALID_STORE_DATA);
            throw new ValidationException(error);
        }
        Optional<AppStore> store = appStoreRepository.findByNameAndHost(
            appStore.getName(),
            appStore.getHost());
        if (store.isPresent()) {
            AppEngineError error = ErrorBuilder.build(
                ErrorCode.INTERNAL_INVALID_STORE_ALREADY_EXISTS);
            throw new ValidationException(error);
        }
        log.info("App store [{}] saved", appStore.getName());
        appStore.setDefaultStore(false);
        return appStoreRepository.save(appStore);
    }

    public Optional<TaskDescription> install(String namespace, String version)
        throws FileNotFoundException,
        TaskServiceException,
        ValidationException,
        BundleArchiveException,
        AppStoreServiceException
    {
        log.info("Install Task: installing ... {}:{}",
            namespace,
            version);
        File file = null;
        try {
            file = downloadTask(namespace, version);
        } catch (IOException | RestClientException e) {
            AppEngineError error = ErrorBuilder
                .build(ErrorCode.APPSTORE_DOWNLOAD_FAILED);
            throw new AppStoreServiceException(error);
        }
        assert file != null;
        Optional<TaskDescription> description = taskService.uploadTask(new FileInputStream(file));
        file.delete();
        log.info("Install Task: installed");
        return description;
    }

    public File downloadTask(String namespace, String version)
        throws IOException, RestClientException {
        log.info("Download Task: downloading ... {}:{}",
            namespace,
            version);
        Optional<AppStore> storeOptional = findDefault();
        AppStore appStore = new AppStore();
        if (storeOptional.isPresent()) {
            appStore = storeOptional.get();
        }
        RestTemplate restTemplate = new RestTemplate();
        Path tempPath = Path.of("bundle-" + UUID.randomUUID() + ".zip");
        restTemplate.execute(
            appStore.getHost() + "/api/v1/tasks/{namespace}/{version}/bundle.zip",
            org.springframework.http.HttpMethod.GET,
            null,
            clientHttpResponse -> {
                Files.copy(clientHttpResponse.getBody(), tempPath);
                return null;
            },
            namespace,
            version
        );
        log.info("Download Task: downloaded");
        return tempPath.toFile();
    }
}
