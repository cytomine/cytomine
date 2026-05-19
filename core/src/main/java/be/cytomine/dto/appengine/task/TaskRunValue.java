package be.cytomine.dto.appengine.task;

import java.util.UUID;

public record TaskRunValue(UUID taskRunId, String parameterName, String type, Object value, String subType) {}
