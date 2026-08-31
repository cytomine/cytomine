package be.cytomine.appengine.dto.inputs.task;

import java.util.UUID;

import be.cytomine.appengine.states.TaskRunState;

public record TaskRun(UUID id, TaskDescription task, TaskRunState state) {}
