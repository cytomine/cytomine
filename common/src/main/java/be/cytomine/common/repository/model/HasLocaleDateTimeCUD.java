package be.cytomine.common.repository.model;

import java.time.LocalDateTime;
import java.util.Optional;

public interface HasLocaleDateTimeCUD {
    LocalDateTime updated();

    Optional<LocalDateTime> deleted();

    LocalDateTime created();
}
