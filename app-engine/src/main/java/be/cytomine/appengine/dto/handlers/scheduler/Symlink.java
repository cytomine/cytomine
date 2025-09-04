package be.cytomine.appengine.dto.handlers.scheduler;

import java.util.Map;

import lombok.Data;

@Data
public class Symlink {
    private Map<String, String> symlinks;
}
