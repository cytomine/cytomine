package be.cytomine.service.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.SearchException;
import be.cytomine.repository.image.ImageInstanceRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.MeiliSearchService;

/**
 * One-time backfill of the {@code image.projects} meili attribute from the current database state.
 *
 * <p>Every existing {@code (project, baseImage)} membership coming from an ImageInstance is appended to the
 * {@code image.projects} list of the corresponding meili document, chunked per project. Used once at deploy after
 * the {@code dataset.alias} -> {@code image.projects} scope swap, so project-view scoping starts working for
 * images whose memberships predate the feature.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataProjectBackfillService {

    public static final int PAGE_SIZE = 5000;

    private static final int IMAGE_CHUNK_SIZE = 1000;

    private final ImageInstanceRepository imageInstanceRepository;

    private final ProjectRepository projectRepository;

    private final MeiliSearchService meiliSearchService;

    public Map<String, Object> backfill() {
        Map<Long, List<Long>> baseImageIdsByProject = new HashMap<>();
        int membershipCount = 0;
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        List<Object[]> rows;
        do {
            rows = imageInstanceRepository.findDistinctProjectBaseImagePairs(pageable);
            for (Object[] row : rows) {
                long projectId = ((Number) row[0]).longValue();
                long baseImageId = ((Number) row[1]).longValue();
                baseImageIdsByProject.computeIfAbsent(projectId, key -> new ArrayList<>()).add(baseImageId);
                membershipCount++;
            }
            pageable = pageable.next();
        } while (rows.size() == PAGE_SIZE);

        Map<Long, String> projectNames = projectRepository.findAllById(baseImageIdsByProject.keySet()).stream()
            .collect(Collectors.toMap(Project::getId, Project::getName));

        int backfilledProjects = 0;
        int updatedImages = 0;
        int failedChunks = 0;
        for (Map.Entry<Long, List<Long>> entry : baseImageIdsByProject.entrySet()) {
            String projectName = projectNames.get(entry.getKey());
            if (projectName == null) {
                log.warn("Backfill: project {} no longer exists, skipping its {} memberships",
                    entry.getKey(), entry.getValue().size());
                continue;
            }
            List<Long> baseImageIds = entry.getValue();
            for (int from = 0; from < baseImageIds.size(); from += IMAGE_CHUNK_SIZE) {
                List<Long> chunk = baseImageIds.subList(from, Math.min(from + IMAGE_CHUNK_SIZE, baseImageIds.size()));
                try {
                    updatedImages += meiliSearchService.addProjectToImages(chunk, projectName);
                } catch (SearchException e) {
                    failedChunks++;
                    log.warn("Backfill: failed to tag project '{}' ({} images)", projectName, chunk.size(), e);
                }
            }
            backfilledProjects++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("memberships", membershipCount);
        result.put("projects", backfilledProjects);
        result.put("updatedImages", updatedImages);
        result.put("failedChunks", failedChunks);
        return result;
    }
}