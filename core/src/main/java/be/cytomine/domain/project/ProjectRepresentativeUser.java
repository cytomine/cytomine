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
public class ProjectRepresentativeUser extends CytomineDomain {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        ProjectRepresentativeUser projectRepresentativeUser = (ProjectRepresentativeUser) this;
        projectRepresentativeUser.setId(json.getJSONAttrLong("id", null));
        projectRepresentativeUser.setProject((Project) json.getJSONAttrDomain(
            entityManager,
            "project",
            new Project(),
            true
        ));
        projectRepresentativeUser.setUser((User) json.getJSONAttrDomain(entityManager, "user", new User(), true));
        return projectRepresentativeUser;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        ProjectRepresentativeUser projectRepresentativeUser = (ProjectRepresentativeUser) domain;
        returnArray.put("project", projectRepresentativeUser.getProject().getId());
        returnArray.put("user", projectRepresentativeUser.getUser().getId());
        return returnArray;
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return project;
    }
}
