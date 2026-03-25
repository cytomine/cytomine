package be.cytomine.common.repository.model.command.insert;

import be.cytomine.common.repository.model.command.TermCommandPayload;

public record InsertTermCommand(
    TermCommandPayload data,
    Long userId,
    Long projectId
) implements InsertCommandRequest<TermCommandPayload> {

    public static final String SERVICE_NAME = "TermService";

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }
}
