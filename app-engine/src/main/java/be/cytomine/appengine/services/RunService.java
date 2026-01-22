package be.cytomine.appengine.services;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.appengine.dto.handlers.filestorage.Storage;
import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import be.cytomine.appengine.dto.responses.errors.ErrorBuilder;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.exceptions.RunTaskServiceException;
import be.cytomine.appengine.handlers.StorageHandler;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.repositories.RunRepository;
import be.cytomine.appengine.states.TaskRunState;

@Slf4j
@RequiredArgsConstructor
@Service
public class RunService {

    private final RunRepository runRepository;

    private final StorageHandler storageHandler;

    public Run findRun(String runid) {
        return runRepository.findById(UUID.fromString(runid)).orElse(null);
    }

    public Run update(Run run) {
        run.setUpdatedAt(LocalDateTime.now());
        run.setLastStateTransitionAt(LocalDateTime.now());
        return runRepository.saveAndFlush(run);
    }

    public boolean updateRunState(TaskRunState state) {
        log.info("Updating Run State: update to {}", state);
        return true;
    }

    private void deleteStorage(String storageName) throws RunTaskServiceException {
        try {
            log.info("Deleting storage {}", storageName);
            Storage storage = new Storage();
            storageHandler.deleteStorage(storage);
            log.info("Storage {} successfully deleted", storageName);
        } catch (FileStorageException e) {
            log.error("Failed to delete storage {}: [{}]", storageName, e.getMessage());
            AppEngineError error = ErrorBuilder.build(ErrorCode.STORAGE_DELETE_FAILED);
            throw new RunTaskServiceException(error);
        }
    }

    public void deleteRunStorage(Run run) throws RunTaskServiceException {
        deleteStorage("task-run-inputs-" + run.getId());
        deleteStorage("task-run-outputs-" + run.getId());
    }
}
