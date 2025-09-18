package be.cytomine.appengine.handlers.scheduler.impl.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import jakarta.persistence.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.repositories.RunRepository;
import be.cytomine.appengine.states.TaskRunState;

@Slf4j
@AllArgsConstructor
public class PodInformer implements ResourceEventHandler<Pod> {

    private static final Map<String, TaskRunState> STATUS = new HashMap<String, TaskRunState>() {
        {
            put("Running", TaskRunState.RUNNING);
            put("Succeeded", TaskRunState.RUNNING);
            put("Failed", TaskRunState.FAILED);
            put("Unknown", TaskRunState.FAILED);
        }
    };

    private static final Set<TaskRunState> FINAL_STATES = Set.of(
        TaskRunState.FAILED,
        TaskRunState.FINISHED
    );

    private final RunRepository runRepository;

    private Run getRun(Pod pod) {
        Map<String, String> labels = pod.getMetadata().getLabels();

        String runId = labels.get("runId");
        Optional<Run> runOptional = runRepository.findById(UUID.fromString(runId));
        if (runOptional.isEmpty()) {
            log.error("Pod Informer: run {} is empty", runId);
            return null;
        }

        return runOptional.get();
    }

    @Override
    public void onAdd(Pod pod) {
        int maxAttempts = 3;
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                Run run = getRun(pod);
                if (run == null || FINAL_STATES.contains(run.getState())) {
                    return;
                }

                run.setState(TaskRunState.PENDING);
                run = runRepository.saveAndFlush(run);
                log.info("Pod Informer: set Run {} to {}", run.getId(), run.getState());
                return;
            } catch (OptimisticLockException ex) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw ex;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", e);
                }
            }
        }

        // This line should never be reached.
        throw new IllegalStateException("Failed to update run after retries");

    }

    @Override
    public void onUpdate(Pod oldPod, Pod newPod) {
        int maxAttempts = 3;
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                Run run = getRun(newPod);
                if (Objects.isNull(run)) {
                    return;
                }

                boolean isFinalState = FINAL_STATES.contains(run.getState());
                boolean isPending = newPod.getStatus().getPhase().equals("Pending");
                boolean isRunning = newPod.getStatus().getPhase().equals("Running");
                if (isFinalState || isPending || isRunning) {
                    return;
                }

                run.setState(STATUS
                    .getOrDefault(newPod.getStatus().getPhase(), TaskRunState.FAILED));
                run = runRepository.saveAndFlush(run);
                log.info("Pod Informer: update Run {} to {}", run.getId(), run.getState());
                return;
            } catch (OptimisticLockException ex) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw ex;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", e);
                }
            }
        }

        // This line should never be reached.
        throw new IllegalStateException("Failed to update run after retries");

    }

    @Override
    public void onDelete(Pod pod, boolean deletedFinalStateUnknown) {
        log.info("Pod Informer: Pod deleted");
    }
}
