package be.cytomine.common.repository.model.command.payload.term;

import java.util.Optional;

public record UpdateTermCommandPayload(Optional<TermCommandPayload> before, Optional<TermCommandPayload> after) {
}
