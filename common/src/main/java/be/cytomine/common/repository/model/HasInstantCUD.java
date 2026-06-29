package be.cytomine.common.repository.model;

import java.time.Instant;
import java.util.Optional;

public interface HasInstantCUD {
    Optional<Instant> updated();

    Optional<Instant> deleted();

    Instant created();
}
