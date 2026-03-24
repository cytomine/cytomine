package be.cytomine.common.repository.model.command.insert;

public sealed interface InsertCommandRequest permits InsertTermCommand {

    String data();

    Long userId();

    Long projectId();

    String serviceName();
}
