package be.cytomine.service.project;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import be.cytomine.domain.project.Project;
import be.cytomine.repository.image.ImageInstanceRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.MeiliSearchService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetadataProjectBackfillServiceTests {

    @Mock
    private ImageInstanceRepository imageInstanceRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MeiliSearchService meiliSearchService;

    private MetadataProjectBackfillService service() {
        return new MetadataProjectBackfillService(
            imageInstanceRepository, projectRepository, meiliSearchService
        );
    }

    @Test
    void shouldGroupMembershipsPerProjectAndTagOncePerChunk() {
        when(imageInstanceRepository.findDistinctProjectBaseImagePairs(any(Pageable.class)))
            .thenReturn(List.<Object[]>of(new Object[]{7L, 10L}, new Object[]{7L, 11L}, new Object[]{8L, 20L}), List.of());
        Project project7 = project(7L, "Project 7");
        Project project8 = project(8L, "Project 8");
        when(projectRepository.findAllById(any())).thenReturn(List.of(project7, project8));
        when(meiliSearchService.addProjectToImages(anyList(), any(String.class))).thenReturn(2);

        Map<String, Object> result = service().backfill();

        verify(meiliSearchService).addProjectToImages(List.of(10L, 11L), "Project 7");
        verify(meiliSearchService).addProjectToImages(List.of(20L), "Project 8");
        assertEquals(3, result.get("memberships"));
        assertEquals(2, result.get("projects"));
        assertEquals(4, result.get("updatedImages"));
        assertEquals(0, result.get("failedChunks"));
    }

    @Test
    void shouldSkipProjectWhoseRepositoryEntryWasDeleted() {
        when(imageInstanceRepository.findDistinctProjectBaseImagePairs(any(Pageable.class)))
            .thenReturn(List.<Object[]>of(new Object[]{404L, 10L}), List.of());
        when(projectRepository.findAllById(any())).thenReturn(List.of());

        Map<String, Object> result = service().backfill();

        verifyNoInteractions(meiliSearchService);
        assertEquals(1, result.get("memberships"));
        assertEquals(0, result.get("projects"));
        assertEquals(0, result.get("updatedImages"));
    }

    private Project project(long id, String name) {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(id);
        when(project.getName()).thenReturn(name);
        return project;
    }
}