package be.cytomine.service.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import be.cytomine.config.security.IncomingAuthorizationContext;
import be.cytomine.config.security.IncomingAuthorizationContext.Headers;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.repository.image.ImageInstanceRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.MeiliSearchService;
import be.cytomine.service.image.ImageInstanceService;
import be.cytomine.service.utils.TaskService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectFromSearchAsyncService {

    private static final int CREATE_CHUNK_SIZE = 100;
    private static final int DUP_CHECK_CHUNK_SIZE = 1000;

    private final MeiliSearchService meiliSearchService;

    private final TaskService taskService;

    private final ProjectRepository projectRepository;

    private final ImageInstanceRepository imageInstanceRepository;

    private final ImageInstanceService imageInstanceService;

    @Async
    public void run(Long taskId, Long projectId, String query, List<String> filters, Headers headers) {
        Task task = taskService.get(taskId);
        Project project = task != null && projectId != null
            ? projectRepository.findById(projectId).orElse(null)
            : null;
        if (task == null || project == null) {
            log.error("Cannot run create-from-search job: task {} or project {} not found", taskId, projectId);
            return;
        }
        IncomingAuthorizationContext.runWithHeaders(
            headers, () -> createMatchingInstances(task, project, query, filters)
        );
    }

    private void createMatchingInstances(Task task, Project project, String query, List<String> filters) {
        Set<Long> abstractImageIds = new HashSet<>();
        List<Long> createdAbstractImageIds = new ArrayList<>();
        int skipped = 0;
        boolean taggingWarning = false;
        try {
            taskService.updateTask(task, 5, "Searching images");
            abstractImageIds.addAll(meiliSearchService.searchImageIds(query, filters));
            taskService.updateTask(task, 10, "Preparing images");

            List<Long> remaining = new ArrayList<>(abstractImageIds);
            imageInstanceService.setTagMode(ImageInstanceService.TagMode.DEFER);
            try {
                int progress = 15;
                for (int from = 0; from < remaining.size(); from += CREATE_CHUNK_SIZE) {
                    List<Long> chunk = remaining.subList(
                        from, Math.min(from + CREATE_CHUNK_SIZE, remaining.size())
                    );
                    List<Long> toCreate = filterOutAlreadyInProject(chunk, project);
                    skipped += chunk.size() - toCreate.size();
                    for (Long abstractImageId : toCreate) {
                        CommandResponse response = imageInstanceService.add(
                            JsonObject.of("baseImage", abstractImageId, "project", project.getId())
                        );
                        ImageInstance instance = (ImageInstance) response.getObject();
                        if (instance != null) {
                            createdAbstractImageIds.add(instance.getBaseImage().getId());
                        }
                        int target = Math.min(
                            90,
                            15 + (int) ((75.0 / Math.max(1, remaining.size()))
                                * createdAbstractImageIds.size())
                        );
                        if (target > progress) {
                            progress = target;
                            taskService.updateTask(
                                task, progress, "Created " + createdAbstractImageIds.size() + " images"
                            );
                        }
                    }
                    if (!tagProjectMemberships(toCreate, project)) {
                        taggingWarning = true;
                    }
                }
            } finally {
                imageInstanceService.setTagMode(ImageInstanceService.TagMode.NORMAL);
            }
        } catch (Exception e) {
            log.error("Create-from-search job failed for project {} (task {})", project.getId(), task.getId(), e);
            taskService.updateTask(task, 100, "Error: " + e.getMessage());
            return;
        }

        if (taggingWarning) {
            taskService.updateTask(task, 93, "Warning: images created but some metadata tagging failed");
        }
        taskService.updateTask(
            task,
            95,
            "Matched " + abstractImageIds.size()
                + " images, added " + createdAbstractImageIds.size()
                + ", skipped " + skipped
        );
        taskService.finishTask(task);
    }

    private boolean tagProjectMemberships(List<Long> abstractImageIds, Project project) {
        if (abstractImageIds.isEmpty()) {
            return true;
        }
        try {
            meiliSearchService.addProjectToImages(abstractImageIds, project.getName());
            return true;
        } catch (Exception e) {
            log.warn("Could not tag project '{}' in metadata after create-from-search", project.getName(), e);
            return false;
        }
    }

    private List<Long> filterOutAlreadyInProject(List<Long> abstractImageIds, Project project) {
        Set<Long> alreadyInProject = IntStream
            .iterate(0, from -> from < abstractImageIds.size(), from -> from + DUP_CHECK_CHUNK_SIZE)
            .mapToObj(from -> abstractImageIds.subList(
                from, Math.min(from + DUP_CHECK_CHUNK_SIZE, abstractImageIds.size())
            ))
            .flatMap(chunk -> imageInstanceRepository.findAllByBaseImageIdInAndProject(chunk, project).stream())
            .map(imageInstance -> imageInstance.getBaseImage().getId())
            .collect(Collectors.toSet());
        return abstractImageIds.stream().filter(id -> !alreadyInProject.contains(id)).toList();
    }
}