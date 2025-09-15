package be.cytomine.appengine.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import be.cytomine.appengine.dto.responses.errors.ErrorBuilder;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.ValidationException;
import be.cytomine.appengine.exceptions.AppStoreNotFoundException;
import be.cytomine.appengine.models.store.AppStore;
import be.cytomine.appengine.repositories.AppStoreRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppStoreService {

    private final AppStoreRepository appStoreRepository;

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
        log.info("make default {}", id);
        Optional<AppStore> defaultStore = appStoreRepository.findByDefaultStoreIsTrue();
        if (defaultStore.isPresent()) {
            AppStore store = defaultStore.get();
            store.setDefaultStore(false);
            appStoreRepository.saveAndFlush(store);
        }
        Optional<AppStore> appStore = appStoreRepository.findById(id);
        if (appStore.isPresent()) {
            AppStore store = appStore.get();
            store.setDefaultStore(true);
            appStoreRepository.saveAndFlush(store);
        } else {
            AppEngineError error = ErrorBuilder.build(
                    ErrorCode.INTERNAL_INVALID_STORE_NOT_FOUND);
            throw new AppStoreNotFoundException(error);
        }
        log.info("now default {}", id);
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
        log.info("saved");
        appStore.setDefaultStore(false);
        return appStoreRepository.save(appStore);
    }
}
