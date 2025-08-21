package be.cytomine.appengine.dto.handlers.scheduler;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParameterSymlink extends Symlink {
    private String parameterName;
    private String symlink;
}
