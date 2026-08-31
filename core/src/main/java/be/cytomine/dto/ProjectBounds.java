package be.cytomine.dto;

import java.util.Date;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import be.cytomine.utils.JsonObject;
import be.cytomine.utils.MinMax;

@Slf4j
@Getter
public class ProjectBounds extends AbstractBounds {

    private MinMax<Date> created = new MinMax<>();

    private MinMax<Date> updated = new MinMax<>();

    private MinMax<String> mode = new MinMax<>();

    private MinMax<String> name = new MinMax<>();

    private MinMax<Long> numberOfAnnotations = new MinMax<>();

    private MinMax<Long> numberOfJobAnnotations = new MinMax<>();

    private MinMax<Long> numberOfReviewedAnnotations = new MinMax<>();

    private MinMax<Long> numberOfImages = new MinMax<>();

    private MinMax<Long> members = new MinMax<>();

    public void submit(JsonObject project) {
        log.debug(project.toJsonString());
        updateMinMax(created, project.getJSONAttrDate("created"));
        updateMinMax(updated, project.getJSONAttrDate("updated"));

        updateMinMax(mode, project.getJSONAttrStr("mode"));
        updateMinMax(name, project.getJSONAttrStr("name"));
        updateMinMax(numberOfAnnotations, project.getJSONAttrLong("numberOfAnnotations"));
        updateMinMax(numberOfJobAnnotations, project.getJSONAttrLong("numberOfJobAnnotations"));

        updateMinMax(numberOfReviewedAnnotations, project.getJSONAttrLong("numberOfReviewedAnnotations"));
        updateMinMax(numberOfImages, project.getJSONAttrLong("numberOfImages"));
        updateMinMax(members, project.getJSONAttrLong("membersCount"));
    }
}
