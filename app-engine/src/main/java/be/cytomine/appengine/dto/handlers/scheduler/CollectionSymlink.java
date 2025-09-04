package be.cytomine.appengine.dto.handlers.scheduler;

import java.util.Map;

import lombok.Data;

@Data
@EqualsAndHashCode(callSuper=false)
public class CollectionSymlink extends Symlink {
    private String parameterName;
    private Map<String, String> symlinks;
}
