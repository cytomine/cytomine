package be.cytomine.appengine.unit.services;

import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.dto.responses.errors.ErrorDefinitions;
import be.cytomine.appengine.exceptions.AppStoreNotFoundException;
import be.cytomine.appengine.exceptions.AppStoreServiceException;
import be.cytomine.appengine.exceptions.ValidationException;
import be.cytomine.appengine.models.store.AppStore;
import be.cytomine.appengine.repositories.AppStoreRepository;
import be.cytomine.appengine.services.AppStoreService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppStoreServiceTest {

    @Mock
    private AppStoreRepository appStoreRepository;

    @InjectMocks
    private AppStoreService appStoreService;

    @Mock
    private RestTemplate restTemplate;

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

    @Test
    void testDownloadTaskSuccessful() throws Exception {
        UUID appStoreId = UUID.randomUUID();
        AppStore defaultStore = new AppStore();
        defaultStore.setId(appStoreId);
        defaultStore.setHost("https://defaultstore.com");
        defaultStore.setDefaultStore(true);

        String namespace = "someNamespace";
        String version = "1.0.0";

        Path tempPath = Path.of("bundle-" + UUID.randomUUID() + ".zip");
        when(appStoreService.findDefault()).thenReturn(Optional.of(defaultStore));
        when(restTemplate.execute(
                eq(defaultStore.getHost() + "/api/v1/tasks/" + namespace + "/" + version + "/bundle.zip"),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(),
                eq(namespace),
                eq(version)))
                .thenReturn(null);

        File downloadedFile = appStoreService.downloadTask(namespace, version);

        assertNotNull(downloadedFile);
    }

    @Test
    void testDownloadTaskThrowsExceptionWhenNoDefaultStore() {
        when(appStoreService.findDefault()).thenReturn(Optional.empty());

        String namespace = "someNamespace";
        String version = "1.0.0";

        AppStoreServiceException exception = assertThrows(
                AppStoreServiceException.class,
                () -> appStoreService.downloadTask(namespace, version)
        );

        assertNotNull(exception);
    }

    @Test
    void testDownloadTaskThrowsExceptionOnRestClientError() throws Exception {
        UUID appStoreId = UUID.randomUUID();
        AppStore defaultStore = new AppStore();
        defaultStore.setId(appStoreId);
        defaultStore.setHost("http://wronghost");
        defaultStore.setDefaultStore(true);

        String namespace = "someNamespace";
        String version = "1.0.0";

        when(appStoreService.findDefault()).thenReturn(Optional.of(defaultStore));
        when(restTemplate.execute(
                eq(defaultStore.getHost() + "/api/v1/tasks/{namespace}/{version}/bundle.zip"),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(),
                eq(namespace),
                eq(version)))
                .thenThrow(new RestClientException("Download failed"));

        RestClientException exception = assertThrows(
                RestClientException.class,
                () -> appStoreService.downloadTask(namespace, version)
        );

        assertNotNull(exception);
    }
}
