package be.cytomine.common.repository.model.term.payload;

import java.util.Optional;

public record UpdateTerm(Optional<String> name, Optional<String> color) {

}
