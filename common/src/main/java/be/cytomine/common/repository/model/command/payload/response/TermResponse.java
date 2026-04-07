<<<<<<<< HEAD:common/src/main/java/be/cytomine/common/repository/model/command/payload/response/TermResponse.java
package be.cytomine.common.repository.model.command.payload.response;
========
package be.cytomine.common.repository.model.term.payload;
>>>>>>>> origin/main:common/src/main/java/be/cytomine/common/repository/model/term/payload/TermResponse.java

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public record TermResponse(long id, String name, String color, long ontologyId,
                           LocalDateTime created, LocalDateTime updated, Optional<LocalDateTime> deleted,
<<<<<<<< HEAD:common/src/main/java/be/cytomine/common/repository/model/command/payload/response/TermResponse.java
                           String comment, Set<TermResponse> children) implements ApplyCommandResponse {
========
                           String comment, Set<TermResponse> children) {
>>>>>>>> origin/main:common/src/main/java/be/cytomine/common/repository/model/term/payload/TermResponse.java
    public TermResponse {
        if (children == null) {
            children = Set.of();
        }
    }
}
