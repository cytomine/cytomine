package be.cytomine.dto.appengine.task.input;

import be.cytomine.dto.appengine.task.type.TaskParameterType;

public record IntegerInput(String parameterName, TaskParameterType type, Integer value) implements TaskRunInput {}
