package be.cytomine.domain.ontology;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.project.Project;
import be.cytomine.utils.JsonObject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
public class AnnotationGroup extends CytomineDomain {
    @Id
    @GeneratedValue
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    ImageGroup imageGroup;

    String type;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        AnnotationGroup annotationGroup = this;

        annotationGroup.id = json.getJSONAttrLong("id", null);
        annotationGroup.created = json.getJSONAttrDate("created");
        annotationGroup.updated = json.getJSONAttrDate("updated");

        annotationGroup.project = (Project) json.getJSONAttrDomain(entityManager, "project", new Project(), true);
        annotationGroup.imageGroup = (ImageGroup) json.getJSONAttrDomain(entityManager, "imageGroup", new ImageGroup(), true);
        annotationGroup.type = json.getJSONAttrStr("type", "SAME_OBJECT");

        return annotationGroup;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        AnnotationGroup annotationGroup = (AnnotationGroup) domain;

        returnArray.put("project", annotationGroup.getProject().getId());
        returnArray.put("imageGroup", annotationGroup.getImageGroup().getId());
        returnArray.put("type", annotationGroup.getType());

        return returnArray;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    @Override
    public CytomineDomain container() {
        return imageGroup.container();
    }
}
