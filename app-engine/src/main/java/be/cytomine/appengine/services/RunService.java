package be.cytomine.appengine.services;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.appengine.dto.handlers.filestorage.Storage;
import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import be.cytomine.appengine.dto.responses.errors.ErrorBuilder;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.exceptions.RunTaskServiceException;
import be.cytomine.appengine.exceptions.SchedulingException;
import be.cytomine.appengine.handlers.SchedulerHandler;
import be.cytomine.appengine.handlers.StorageHandler;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.repositories.RunRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class RunService {

    private final RunRepository runRepository;

    private final SchedulerHandler schedulerHandler;

    private final StorageHandler storageHandler;

    public Run update(Run run) {
        run.setUpdatedAt(LocalDateTime.now());
        run.setLastStateTransitionAt(LocalDateTime.now());
        return runRepository.saveAndFlush(run);
    }

    private void deleteStorage(String storageName) throws RunTaskServiceException {
        try {
            log.info("Deleting storage '{}'", storageName);
            Storage storage = new Storage(storageName);
            storageHandler.deleteStorage(storage);
            log.info("Storage '{}' successfully deleted", storageName);
        } catch (FileStorageException e) {
            log.error("Failed to delete storage '{}': [{}]", storageName, e.getMessage());
            AppEngineError error = ErrorBuilder.build(ErrorCode.STORAGE_DELETE_FAILED);
            throw new RunTaskServiceException(error);
        }
    }

    public void deleteStorageIfExists(String storageName) {
        try {
            deleteStorage(storageName);
        } catch (RunTaskServiceException e) {
            log.warn("Failed to delete storage '{}': [{}]. Skipping.", storageName, e.getMessage());
        }
    }

    public void deleteRun(Run run) {
        deleteStorageIfExists("task-run-inputs-" + run.getId());
        deleteStorageIfExists("task-run-outputs-" + run.getId());

        try {
            schedulerHandler.deleteRun(run);
        } catch (SchedulingException exception) {
            log.warn(
                "Could not delete run {} in scheduler, continuing deletion. Cause: {}",
                run.getId(),
                exception.getMessage()
            );
        }
    }
}
