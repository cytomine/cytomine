package be.cytomine.appengine.dto.handlers.scheduler;

import lombok.Data;

import java.util.Map;

@Data
public class Symlink {
    private Map<String, String> Symlinks; // "{parameterName}[index] : {path}
}
