package be.cytomine.service.search;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.io.ParseException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.dto.image.CropParameter;
import be.cytomine.service.middleware.ImageServerService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RetrievalServiceTests {

    @Mock
    private ImageServerService imageServerService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RetrievalService retrievalService;

    @Test
    void createStorageShouldSucceedWhenApiReturnsOk() {
        Long projectId = 42L;
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("created"));

        assertDoesNotThrow(() -> retrievalService.createStorage(String.valueOf(projectId)));
    }

    @Test
    void createStorageShouldThrowExceptionWhenApiReturnsError() {
        Long projectId = 42L;
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something went wrong"));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> retrievalService.createStorage(String.valueOf(projectId))
        );

        assertTrue(exception.getMessage().contains("Failed to create storage"));
    }

    @Test
    void deleteStorageShouldSucceedWhenApiReturnsOk() {
        Long projectId = 42L;
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.DELETE), eq(null), eq(String.class)))
            .thenReturn(ResponseEntity.ok("deleted"));

        assertDoesNotThrow(() -> retrievalService.deleteStorage(String.valueOf(projectId)));
    }

    @Test
    void deleteStorageShouldThrowExceptionWhenApiReturnsError() {
        Long projectId = 42L;
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.DELETE), eq(null), eq(String.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something went wrong"));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> retrievalService.deleteStorage(String.valueOf(projectId))
        );

        assertTrue(exception.getMessage().contains("Failed to delete storage"));
    }

    @Test
    void indexAnnotationShouldSucceedWhenApiReturnsOk() throws UnsupportedEncodingException, ParseException {
        Long projectId = 42L;
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        AnnotationDomain annotation = mock(AnnotationDomain.class);
        when(annotation.getProject()).thenReturn(project);
        when(annotation.getId()).thenReturn(1L);
        when(annotation.getWktLocation()).thenReturn("POINT(1 1)");
        when(imageServerService.crop(eq(annotation), any(CropParameter.class), eq(null), eq(null)))
            .thenReturn(ResponseEntity.ok("image".getBytes()));
        when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.POST),
            any(),
            eq(String.class)
        )).thenReturn(ResponseEntity.ok("indexed"));

        ResponseEntity<String> result = retrievalService.indexAnnotation(annotation);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deleteIndexShouldSucceedWhenApiReturnsOk() {
        Long projectId = 42L;
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        AnnotationDomain annotation = mock(AnnotationDomain.class);
        when(annotation.getProject()).thenReturn(project);
        when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.DELETE),
            eq(null),
            eq(String.class)
        )).thenReturn(ResponseEntity.ok("deleted"));

        ResponseEntity<String> result = retrievalService.deleteIndex(annotation);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
