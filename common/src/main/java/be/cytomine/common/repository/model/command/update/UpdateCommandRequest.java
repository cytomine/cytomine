package be.cytomine.common.repository.model.command.update;

public sealed interface UpdateCommandRequest permits UpdateTermCommand {

    String data();

    Long userId();

    Long projectId();

    String serviceName();
}
