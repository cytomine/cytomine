<<<<<<<< HEAD:common/src/main/java/be/cytomine/common/repository/model/command/payload/response/TermResponse.java
package be.cytomine.common.repository.model.command.payload.response;
========
package be.cytomine.common.repository.model.term.payload;
>>>>>>>> origin/main:common/src/main/java/be/cytomine/common/repository/model/term/payload/TermResponse.java

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.command.DataType;

public record TermResponse(long id, String name, String color, long ontologyId, LocalDateTime created,
                           LocalDateTime updated, Optional<LocalDateTime> deleted, String comment,
                           Set<TermResponse> children) implements ApplyCommandResponse {
    public TermResponse {
        if (children == null) {
            children = Set.of();
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }
}
