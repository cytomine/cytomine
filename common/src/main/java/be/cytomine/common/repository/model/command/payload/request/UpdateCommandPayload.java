package be.cytomine.common.repository.model.command.payload.request;

import java.util.Optional;

public record UpdateCommandPayload<T>(Optional<T> before, Optional<T> after) {
}
