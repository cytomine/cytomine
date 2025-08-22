package be.cytomine.appengine.dto.handlers.scheduler;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import be.cytomine.appengine.models.task.Run;

@Data
public class Schedule {
    private Run run;
    private List<Symlink> links = new ArrayList<>();
}
