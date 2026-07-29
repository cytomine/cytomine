package be.cytomine.domain.project;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.security.User;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class ProjectDefaultLayer extends CytomineDomain {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    boolean hideByDefault;


    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        ProjectDefaultLayer projectDefaultLayer = this;
        projectDefaultLayer.setId(json.getJSONAttrLong("id", null));
        projectDefaultLayer.setProject((Project) json.getJSONAttrDomain(entityManager, "project", new Project(), true));
        projectDefaultLayer.setUser((User) json.getJSONAttrDomain(entityManager, "user", new User(), true));
        projectDefaultLayer.setHideByDefault(json.getJSONAttrBoolean("hideByDefault", false));
        return projectDefaultLayer;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        ProjectDefaultLayer projectDefaultLayer = (ProjectDefaultLayer) domain;
        returnArray.put("project", projectDefaultLayer.getProject().getId());
        returnArray.put("user", projectDefaultLayer.getUser().getId());
        returnArray.put("hideByDefault", projectDefaultLayer.isHideByDefault());
        return returnArray;
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return project.container();
    }
}
