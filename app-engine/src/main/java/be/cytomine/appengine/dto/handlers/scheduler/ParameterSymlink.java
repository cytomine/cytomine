package be.cytomine.appengine.dto.handlers.scheduler;

import lombok.Data;

import java.util.Map;

@Data
public class ParameterSymlink extends Symlink {
    private String parameterName;
    private String Symlink;
}
