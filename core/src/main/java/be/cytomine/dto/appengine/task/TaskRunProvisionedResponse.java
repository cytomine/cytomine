package be.cytomine.dto.appengine.task;

import java.util.UUID;

public record TaskRunProvisionedResponse(String parameterName, UUID taskRunId, Object value) {}
