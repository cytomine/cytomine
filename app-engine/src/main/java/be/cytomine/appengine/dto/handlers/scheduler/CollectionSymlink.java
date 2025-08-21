package be.cytomine.appengine.dto.handlers.scheduler;

import lombok.Data;

import java.util.Map;

@Data
public class CollectionSymlink extends Symlink {
    private String parameterName;
    private Map<String, String> Symlinks;
}
