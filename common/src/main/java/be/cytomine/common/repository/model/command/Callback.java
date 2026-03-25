package be.cytomine.common.repository.model.command;

import java.util.Optional;

public record Callback(String method, Optional<Long> termId, Optional<Long> ontologyId,
                       Optional<Long> projectId) {
}
