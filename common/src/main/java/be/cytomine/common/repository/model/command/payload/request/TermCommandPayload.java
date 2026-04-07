<<<<<<<< HEAD:common/src/main/java/be/cytomine/common/repository/model/command/payload/request/TermCommandPayload.java
package be.cytomine.common.repository.model.command.payload.request;
========
package be.cytomine.common.repository.model.command.payload.term;
>>>>>>>> origin/main:common/src/main/java/be/cytomine/common/repository/model/command/payload/term/TermCommandPayload.java

import java.util.Optional;

public record TermCommandPayload(Optional<Long> parent, long id, String name, String color, String created,
                                 String updated, String comment, long ontology) {
}
