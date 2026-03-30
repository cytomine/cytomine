package be.cytomine.common.repository.model.command.api;

import java.util.UUID;

public record UndoCommand(long id, UUID nonce) {
}
