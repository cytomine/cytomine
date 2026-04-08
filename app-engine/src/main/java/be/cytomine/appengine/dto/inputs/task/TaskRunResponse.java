package be.cytomine.appengine.dto.inputs.task;

import java.time.LocalDateTime;
import java.util.UUID;

import be.cytomine.appengine.states.TaskRunState;

public record TaskRunResponse(
    UUID id,
    TaskDescription task,
    TaskRunState state,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
