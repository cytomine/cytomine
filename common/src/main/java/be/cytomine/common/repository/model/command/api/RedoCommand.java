package be.cytomine.common.repository.model.command.api;

import java.util.UUID;

public record RedoCommand(long id, UUID nonce) {
}
