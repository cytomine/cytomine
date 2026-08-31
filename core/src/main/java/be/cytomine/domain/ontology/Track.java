package be.cytomine.domain.ontology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class Track extends CytomineDomain {

    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String name;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = true)
    private ImageInstance image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    private Project project;


    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        Track track = this;
        track.id = json.getJSONAttrLong("id", null);
        track.name = json.getJSONAttrStr("name");
        track.color = json.getJSONAttrStr("color");

        track.image = (ImageInstance) json.getJSONAttrDomain(entityManager, "image", new ImageInstance(), true);
        track.project = (Project) json.getJSONAttrDomain(entityManager, "project", new Project(), true);

        track.created = json.getJSONAttrDate("created");
        track.updated = json.getJSONAttrDate("updated");
        return track;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        Track track = (Track) domain;
        returnArray.put("name", track.getName());
        returnArray.put("color", track.getColor());
        returnArray.put("image", track.getImage() != null ? track.getImage().getId() : null);
        returnArray.put("project", track.getProject() != null ? track.getProject().getId() : null);
        return returnArray;
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return image.container();
    }
}
