package be.cytomine.common.repository.model.command.update;

public record UpdateTermCommand(
    String data,
    Long userId,
    Long projectId
) implements UpdateCommandRequest {

    public static final String SERVICE_NAME = "TermService";

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }
}
