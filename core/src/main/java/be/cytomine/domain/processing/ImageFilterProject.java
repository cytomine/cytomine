package be.cytomine.domain.processing;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.utils.JsonObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Entity
@Getter
@Setter
public class ImageFilterProject extends CytomineDomain {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "image_filter_id", nullable = false)
    private ImageFilter imageFilter;


    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        ImageFilterProject processingServer = (ImageFilterProject)this;

        try {
            processingServer.id = json.getJSONAttrLong("id",null);
            processingServer.imageFilter = (ImageFilter) json.getJSONAttrDomain(entityManager, "imageFilter", new ImageFilter(), false);
            processingServer.project = (Project) json.getJSONAttrDomain(entityManager, "project", new Project(), false);
        } catch (Exception e) {
            processingServer.imageFilter = (ImageFilter) json.getJSONObject("imageFilter").getJSONAttrDomain(entityManager, "id", new ImageFilter(), false);
            processingServer.project = (Project) json.getJSONObject("project").getJSONAttrDomain(entityManager, "id", new Project(), false);
        }

        processingServer.created = json.getJSONAttrDate("created");
        processingServer.updated = json.getJSONAttrDate("updated");
        return processingServer;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        ImageFilterProject processingServer = (ImageFilterProject)domain;

        returnArray.put("imageFilter", Optional.ofNullable(processingServer.getImageFilter()).map(CytomineDomain::getId).orElse(null));
        returnArray.put("project", Optional.ofNullable(processingServer.getProject()).map(CytomineDomain::getId).orElse(null));

        returnArray.put("baseUrl", Optional.ofNullable(processingServer.getImageFilter()).map(ImageFilter::getBaseUrl).orElse(null));
        returnArray.put("name", Optional.ofNullable(processingServer.getImageFilter()).map(ImageFilter::getName).orElse(null));
        returnArray.put("method", Optional.ofNullable(processingServer.getImageFilter()).map(ImageFilter::getMethod).orElse(null));
        returnArray.put("available", Optional.ofNullable(processingServer.getImageFilter()).map(ImageFilter::getAvailable).orElse(null));

        return returnArray;
    }

    @Override
    public String toJSON() {
        return toJsonObject().toJsonString();
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return this.project;
    }

}
