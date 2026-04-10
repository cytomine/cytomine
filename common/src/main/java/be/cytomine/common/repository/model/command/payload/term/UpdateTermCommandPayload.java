package be.cytomine.common.repository.model.command.payload.term;

import java.util.Optional;

import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;

public record UpdateTermCommandPayload(Optional<TermCommandPayload> before, Optional<TermCommandPayload> after) {
}
