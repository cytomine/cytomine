package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.HasLocaleDateTimeCUD;
import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.ontology.payload.OntologyUser;

public record OntologyResponse(String name, long id, Set<TermResponse> terms, LocalDateTime created,
                               LocalDateTime updated, Optional<LocalDateTime> deleted, OntologyUser user)
    implements ApplyCommandResponse, HasLocaleDateTimeCUD {
    public OntologyResponse {
        if (terms == null) {
            terms = new HashSet<>();
        }
        if (deleted == null) {
            deleted = Optional.empty();
        }
    }


    @Override
    public DataType getDataType() {
        return DataType.ONTOLOGY;
    }
}
