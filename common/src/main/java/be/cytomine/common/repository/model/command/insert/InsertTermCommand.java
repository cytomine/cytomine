package be.cytomine.common.repository.model.command.insert;

public record InsertTermCommand(
    String data,
    Long userId,
    Long projectId
) implements InsertCommandRequest {

    public static final String SERVICE_NAME = "TermService";

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }
}
