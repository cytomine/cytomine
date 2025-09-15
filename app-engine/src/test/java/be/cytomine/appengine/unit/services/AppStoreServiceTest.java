package be.cytomine.appengine.unit.services;

import java.util.Optional;
import java.util.UUID;


import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.dto.responses.errors.ErrorDefinitions;
import be.cytomine.appengine.exceptions.AppStoreNotFoundException;
import be.cytomine.appengine.exceptions.ValidationException;
import be.cytomine.appengine.models.store.AppStore;
import be.cytomine.appengine.repositories.AppStoreRepository;
import be.cytomine.appengine.services.AppStoreService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppStoreServiceTest {

    @Mock
    private AppStoreRepository appStoreRepository;

    @InjectMocks
    private AppStoreService appStoreService;

    public AppStoreServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMakeDefaultWhenNoDefaultExistsAndValidId() throws AppStoreNotFoundException {
        UUID storeId = UUID.randomUUID();
        AppStore appStore = new AppStore();
        appStore.setId(storeId);
        appStore.setName("Test Store");
        appStore.setHost("https://teststore.com");
        appStore.setDefaultStore(false);

        when(appStoreRepository.findByDefaultStoreIsTrue())
            .thenReturn(Optional.empty());
        when(appStoreRepository.findById(storeId))
            .thenReturn(Optional.of(appStore));

        appStoreService.makeDefault(storeId);

        assertTrue(appStore.isDefaultStore());
        verify(appStoreRepository).saveAndFlush(appStore);
    }

    @Test
    void testMakeDefaultWhenDefaultAlreadyExists() throws AppStoreNotFoundException {
        UUID existingDefaultId = UUID.randomUUID();
        UUID newDefaultId = UUID.randomUUID();

        AppStore existingDefault = new AppStore();
        existingDefault.setId(existingDefaultId);
        existingDefault.setName("Existing Default Store");
        existingDefault.setHost("https://existing.com");
        existingDefault.setDefaultStore(true);

        AppStore newAppStore = new AppStore();
        newAppStore.setId(newDefaultId);
        newAppStore.setName("New Store");
        newAppStore.setHost("https://newstore.com");
        newAppStore.setDefaultStore(false);

        when(appStoreRepository.findByDefaultStoreIsTrue())
            .thenReturn(Optional.of(existingDefault));
        when(appStoreRepository.findById(newDefaultId))
            .thenReturn(Optional.of(newAppStore));

        appStoreService.makeDefault(newDefaultId);

        assertFalse(existingDefault.isDefaultStore());
        assertTrue(newAppStore.isDefaultStore());
        verify(appStoreRepository).saveAndFlush(existingDefault);
        verify(appStoreRepository).saveAndFlush(newAppStore);
    }

    @Test
    void testMakeDefaultThrowsExceptionWhenIdNotFound() {
        UUID storeId = UUID.randomUUID();

        when(appStoreRepository.findByDefaultStoreIsTrue())
            .thenReturn(Optional.empty());
        when(appStoreRepository.findById(storeId))
            .thenReturn(Optional.empty());

        AppStoreNotFoundException exception = assertThrows(
            AppStoreNotFoundException.class,
            () -> appStoreService.makeDefault(storeId)
        );

        assertNotNull(exception);
        verify(appStoreRepository, never()).saveAndFlush(any());
    }

    @Test
    void testSaveWithValidAppStore() throws ValidationException {
        AppStore appStore = new AppStore();
        appStore.setName("Valid Store");
        appStore.setHost("https://validstore.com");

        when(appStoreRepository.findByNameAndHost(appStore.getName(), appStore.getHost()))
            .thenReturn(Optional.empty());
        when(appStoreRepository.save(appStore))
            .thenReturn(appStore);

        AppStore savedStore = appStoreService.save(appStore);

        assertEquals(appStore, savedStore);
        verify(appStoreRepository).save(appStore);
    }

    @Test
    void testSaveThrowsValidationExceptionWhenNameIsNull() {
        AppStore appStore = new AppStore();
        appStore.setHost("https://validstore.com");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> appStoreService.save(appStore)
        );

        assertNotNull(exception);
        assertEquals(ErrorDefinitions.fromCode(ErrorCode.INTERNAL_INVALID_STORE_DATA).getMessage(), exception.getError().getMessage());
        verify(appStoreRepository, never()).save(any());
    }

    @Test
    void testSaveThrowsValidationExceptionWhenHostIsNull() {
        AppStore appStore = new AppStore();
        appStore.setName("Valid Store");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> appStoreService.save(appStore)
        );

        assertNotNull(exception);
        assertEquals(
            ErrorDefinitions.fromCode(ErrorCode.INTERNAL_INVALID_STORE_DATA).getMessage(), 
            exception.getError().getMessage());
        verify(appStoreRepository, never()).save(any());
    }

    @Test
    void testSaveThrowsValidationExceptionWhenStoreAlreadyExists() {
        AppStore appStore = new AppStore();
        appStore.setName("Valid Store");
        appStore.setHost("https://validstore.com");

        when(appStoreRepository.findByNameAndHost(appStore.getName(), appStore.getHost()))
            .thenReturn(Optional.of(appStore));

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> appStoreService.save(appStore)
        );

        assertNotNull(exception);
        assertEquals(
            ErrorDefinitions.fromCode(ErrorCode.INTERNAL_INVALID_STORE_ALREADY_EXISTS).getMessage(), 
            exception.getError().getMessage());
        verify(appStoreRepository, never()).save(any());
    }
}
