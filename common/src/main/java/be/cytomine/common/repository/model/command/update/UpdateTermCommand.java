package be.cytomine.common.repository.model.command.update;

import be.cytomine.common.repository.model.command.TermCommandPayload;

public record UpdateTermCommand(
    Long id,
    TermCommandPayload data,
    Long userId,
    Long projectId
) implements UpdateCommandRequest<TermCommandPayload> {

    public static final String SERVICE_NAME = "TermService";

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }
}
