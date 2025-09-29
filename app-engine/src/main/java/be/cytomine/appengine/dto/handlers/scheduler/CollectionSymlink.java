package be.cytomine.appengine.dto.handlers.scheduler;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CollectionSymlink extends Symlink {
    private String parameterName;
}
