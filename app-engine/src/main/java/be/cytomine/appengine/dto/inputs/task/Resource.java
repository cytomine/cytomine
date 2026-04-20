package be.cytomine.appengine.dto.inputs.task;

import java.util.Date;
import java.util.UUID;

import be.cytomine.appengine.states.TaskRunState;

public record Resource(UUID id, TaskDescription task, TaskRunState state, Date createdAt, Date updatedAt) {}
