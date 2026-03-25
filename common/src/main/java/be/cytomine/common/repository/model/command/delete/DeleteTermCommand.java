package be.cytomine.common.repository.model.command.delete;

import be.cytomine.common.repository.model.command.TermCommandPayload;

public record DeleteTermCommand(
    Long id,
    TermCommandPayload data,
    Long userId,
    Long projectId
) implements DeleteCommandRequest {

    public static final String SERVICE_NAME = "TermService";

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }
}
